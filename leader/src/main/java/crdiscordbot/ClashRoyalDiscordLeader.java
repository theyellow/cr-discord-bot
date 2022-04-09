package crdiscordbot;

import crdiscordbot.connect.Constants;
import crdiscordbot.connect.LogoutHttpServer;
import discord4j.common.JacksonResources;
import discord4j.common.store.Store;
import discord4j.common.store.legacy.LegacyStoreLayout;
import discord4j.connect.common.ConnectGatewayOptions;
import discord4j.connect.common.UpstreamGatewayClient;
import discord4j.connect.rabbitmq.ConnectRabbitMQ;
import discord4j.connect.rabbitmq.ConnectRabbitMQSettings;
import discord4j.connect.rabbitmq.gateway.RabbitMQPayloadSink;
import discord4j.connect.rabbitmq.gateway.RabbitMQPayloadSource;
import discord4j.connect.rabbitmq.gateway.RabbitMQSinkMapper;
import discord4j.connect.rabbitmq.gateway.RabbitMQSourceMapper;
import discord4j.connect.rsocket.global.RSocketGlobalRateLimiter;
import discord4j.connect.rsocket.router.RSocketRouter;
import discord4j.connect.rsocket.router.RSocketRouterOptions;
import discord4j.connect.rsocket.shard.RSocketShardCoordinator;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.dispatch.DispatchEventMapper;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.shard.ShardingStrategy;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.RestClient;
import discord4j.store.api.service.StoreService;
import discord4j.store.redis.RedisStoreService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;

@SpringBootApplication
public class ClashRoyalDiscordLeader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClashRoyalDiscordLeader.class);

    public static void main(String[] args) {
        //Start spring application
        ApplicationContext springContext = new SpringApplicationBuilder(ClashRoyalDiscordLeader.class)
            .build()
            .run(args);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting leader, spring context initialized with {} beans", springContext.getBeanDefinitionNames().length);
        }
        leaderBuilder();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Started leader");
        }
    }

    private static void leaderBuilder() {
        /*
         * Define the location of the Global Router Server (GRS). A GRS combines coordinated routing across API
         * requests while also dealing with the global rate limits.
         *
         * We will use RSocket GRS in this example: see ExampleRSocketGlobalRouterServer
         */
        InetSocketAddress globalRouterServerAddress = new InetSocketAddress(Constants.GLOBAL_ROUTER_SERVER_HOST, Constants.GLOBAL_ROUTER_SERVER_PORT);

        /*
         * Define the location of the Shard Coordinator Server (SCS). An SCS establishes predictable ordering across
         * multiple leaders attempting to connect to the Gateway.
         *
         * We will use RSocket SCS in this example: see ExampleRSocket
         */
        InetSocketAddress coordinatorServerAddress = new InetSocketAddress(Constants.SHARD_COORDINATOR_SERVER_HOST, Constants.SHARD_COORDINATOR_SERVER_PORT);

        /*
         * Define the redis server that will be used as entity cache.
         */
        RedisClient redisClient = RedisClient.create(Constants.REDIS_CLIENT_URI);

        /*
         * Create a default factory for working with Jackson, this can be reused across the application.
         */
        JacksonResources jackson = JacksonResources.create();

        /*
         * Define the sharding strategy. Refer to the class docs for more details or options.
         */
        ShardingStrategy shardingStrategy = ShardingStrategy.recommended();

        /*
         * Define the key resources for working with RabbitMQ.
         * - ConnectRabbitMQ defines the parameters to a server
         * - RabbitMQSinkMapper will be used to PRODUCE payloads to other nodes
         *      - "createBinarySinkToDirect" will create binary messages, sent to the "payload" queue directly.
         * - RabbitMQSourceMapper will be used to CONSUME payloads from other nodes
         *      - "createBinarySource" will read binary messages
         */
        ConnectRabbitMQ rabbitMQ;
        if (!Constants.RABBITMQ_HOST.isEmpty()) {
            ConnectRabbitMQSettings settings = ConnectRabbitMQSettings.create().withAddress(Constants.RABBITMQ_HOST, Constants.RABBITMQ_PORT);
            rabbitMQ = ConnectRabbitMQ.createFromSettings(settings);
        } else {
            rabbitMQ = ConnectRabbitMQ.createDefault();
        }
        RabbitMQSinkMapper sink = RabbitMQSinkMapper.createBinarySinkToDirect("payload");
        RabbitMQSourceMapper source = RabbitMQSourceMapper.createBinarySource();

        StoreService redisStore = null;
        while (redisStore == null) {
            try {
                redisStore = RedisStoreService.builder()
                        .redisClient(redisClient)
                        .useSharedConnection(false)
                        .build();
            } catch (RedisConnectionException connectionException) {
                LOGGER.warn("Connection to redis failed, try again in 10 s");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    LOGGER.warn("sleeping got interrupted-exception");
                }
            }
        }
        GatewayDiscordClient client = DiscordClient.builder(System.getenv("BOT_TOKEN"))
                .setJacksonResources(jackson)
                .setGlobalRateLimiter(RSocketGlobalRateLimiter.createWithServerAddress(globalRouterServerAddress))
                .setExtraOptions(o -> new RSocketRouterOptions(o, request -> globalRouterServerAddress))
                .build(RSocketRouter::new)
                .gateway()
                .setSharding(shardingStrategy)
                // Properly coordinate IDENTIFY attempts across all shards
                .setShardCoordinator(RSocketShardCoordinator.createWithServerAddress(coordinatorServerAddress))
                .setDisabledIntents(IntentSet.of(
                        Intent.GUILD_PRESENCES,
                        Intent.GUILD_MESSAGE_TYPING,
                        Intent.DIRECT_MESSAGE_TYPING)).setInitialPresence(s -> ClientPresence.invisible())
                // Disable invalidation strategy and event publishing to save memory usage
                //.setInvalidationStrategy(InvalidationStrategy.disable())
                .setEnabledIntents(IntentSet.all())
                .setDispatchEventMapper(DispatchEventMapper.discardEvents())
                // Define the entity cache
                .setStore(Store.fromLayout(LegacyStoreLayout.of(redisStore)))
                // Turn this gateway into a RabbitMQ-based one
                .setExtraOptions(o -> new ConnectGatewayOptions(o,
                        RabbitMQPayloadSink.create(sink, rabbitMQ),
                        RabbitMQPayloadSource.create(source, rabbitMQ, "gateway")))
                // UpstreamGatewayClient connects to Discord Gateway and forwards payloads to other nodes
                .login(UpstreamGatewayClient::new)
                .blockOptional()
                .orElseThrow(RuntimeException::new);

        LogoutHttpServer.startAsync(client);
        client.onDisconnect().block();
        rabbitMQ.close();
    }

    @Bean(name = "discordRestClient")
    public RestClient discordRestClient() {
        return RestClient.create(System.getenv("BOT_TOKEN"));
    }

    @Bean(name = "royalRestClient")
    public RestTemplate royalRestClient() {
        String apiToken = System.getenv("API_TOKEN");
        RestTemplate restTemplate = new RestTemplateBuilder(rt-> rt.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().clear();
            request.getHeaders().add("Authorization", "Bearer " + apiToken);
            return execution.execute(request, body);
        })).build();
        LOGGER.info("REST-Engine for CR started...");
        return restTemplate;
    }

}
