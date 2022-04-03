package crdiscordbot;

import crdiscordbot.connect.Constants;
import crdiscordbot.connect.LogoutHttpServer;
import crdiscordbot.listeners.SlashCommandListener;
import discord4j.common.JacksonResources;
import discord4j.common.store.Store;
import discord4j.common.store.legacy.LegacyStoreLayout;
import discord4j.connect.common.ConnectGatewayOptions;
import discord4j.connect.common.DownstreamGatewayClient;
import discord4j.connect.rabbitmq.ConnectRabbitMQ;
import discord4j.connect.rabbitmq.ConnectRabbitMQSettings;
import discord4j.connect.rabbitmq.gateway.RabbitMQPayloadSink;
import discord4j.connect.rabbitmq.gateway.RabbitMQPayloadSource;
import discord4j.connect.rabbitmq.gateway.RabbitMQSinkMapper;
import discord4j.connect.rabbitmq.gateway.RabbitMQSourceMapper;
import discord4j.connect.rsocket.global.RSocketGlobalRateLimiter;
import discord4j.connect.rsocket.router.RSocketRouter;
import discord4j.connect.rsocket.router.RSocketRouterOptions;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.shard.MemberRequestFilter;
import discord4j.core.shard.ShardingStrategy;
import discord4j.rest.RestClient;
import discord4j.store.api.readonly.ReadOnlyStoreService;
import discord4j.store.redis.RedisStoreService;
import io.lettuce.core.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@SpringBootApplication
@EnableAsync
public class ClashRoyalDiscordBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClashRoyalDiscordBot.class);

    public static void main(String[] args) {
        //Start spring application
        ApplicationContext springContext = new SpringApplicationBuilder(ClashRoyalDiscordBot.class)
            .build()
            .run(args);
        SlashCommandListener slashCommandListener = new SlashCommandListener(springContext);
        createWorker(slashCommandListener);

    }

    private static void createWorker(SlashCommandListener slashCommandListener) {
        /*
         * Define the location of the Global Router Server (GRS). A GRS combines coordinated routing across API
         * requests while also dealing with the global rate limits.
         *
         * We will use RSocket GRS in this example: see ExampleRSocketGlobalRouterServer
         */
        InetSocketAddress globalRouterServerAddress = new InetSocketAddress(Constants.GLOBAL_ROUTER_SERVER_HOST, Constants.GLOBAL_ROUTER_SERVER_PORT);

        /*
         * Define the redis server that will be used as entity cache.
         */
        RedisClient redisClient = RedisClient.create(Constants.REDIS_CLIENT_URI);

        /*
         * Create a default factory for working with Jackson, this can be reused across the application.
         */
        JacksonResources jackson = JacksonResources.create();

        /*
         * Define the sharding strategy. Workers in this "stateless" configuration should use the single factory.
         * This saves bootstrap efforts by grouping all inbound payloads into one entity (when using
         * DownstreamGatewayClient)
         */
        ShardingStrategy shardingStrategy = ShardingStrategy.single();

        /*
         * Define the key resources for working with RabbitMQ.
         * - ConnectRabbitMQ defines the parameters to a server
         * - RabbitMQSinkMapper will be used to PRODUCE payloads to other nodes
         *      - "createBinarySinkToDirect" will create binary messages, sent to the "gateway" queue directly.
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
        RabbitMQSinkMapper sinkMapper = RabbitMQSinkMapper.createBinarySinkToDirect("gateway");
        RabbitMQSourceMapper sourceMapper = RabbitMQSourceMapper.createBinarySource();

        GatewayDiscordClient client = DiscordClient.builder(System.getenv("BOT_TOKEN"))
                .setJacksonResources(jackson)
                .setGlobalRateLimiter(RSocketGlobalRateLimiter.createWithServerAddress(globalRouterServerAddress))
                .setExtraOptions(o -> new RSocketRouterOptions(o, request -> globalRouterServerAddress))
                .build(RSocketRouter::new)
                .gateway()
                .setSharding(shardingStrategy)
                // Gateway member requests are handled upstream, so disable them here
                .setMemberRequestFilter(MemberRequestFilter.none())
                // Set the entity cache, but wrap with a read-only decorator
                .setStore(Store.fromLayout(LegacyStoreLayout.of(new ReadOnlyStoreService(RedisStoreService.builder()
                        .redisClient(redisClient)
                        .build()))))
                // Turn this gateway into a RabbitMQ-based one
                .setExtraOptions(o -> new ConnectGatewayOptions(o,
                        RabbitMQPayloadSink.create(sinkMapper, rabbitMQ),
                        RabbitMQPayloadSource.create(sourceMapper, rabbitMQ, "payload")))
                // DownstreamGatewayClient does not connect to Gateway and receives payloads from other nodes
                .login(DownstreamGatewayClient::new)
                .blockOptional()
                .orElseThrow(RuntimeException::new);

        LogoutHttpServer.startAsync(client);

        Mono.when(client.on(ChatInputInteractionEvent.class, slashCommandListener::handle)
                .then()).block();
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
