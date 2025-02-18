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
import discord4j.store.api.service.StoreService;
import discord4j.store.redis.RedisStoreService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.net.InetSocketAddress;

/**
* Main class for the Clash Royal Discord Leader application.
* This class initializes the Spring Boot application.
*/
@SpringBootApplication
public class ClashRoyalDiscordLeader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClashRoyalDiscordLeader.class);

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Start spring application
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

    /**
     * The <code>leaderBuilder</code> method in the <code>ClashRoyalDiscordLeader</code> class is responsible for setting up
     * and initializing various components required for the Discord bot to function.
     *
     * <p>The method performs the following tasks:</p>
     *
     * <ol>
     *   <li><strong>Global Router Server (GRS) and Shard Coordinator Server (SCS) Setup:</strong>
     *     <ul>
     *       <li>Defines the addresses for the Global Router Server and Shard Coordinator Server.</li>
     *     </ul>
     *   </li>
     *   <li><strong>Redis Setup:</strong>
     *     <ul>
     *       <li>Configures the Redis server used as an entity cache.</li>
     *       <li>Attempts to connect to the Redis server, retrying if the connection fails.</li>
     *     </ul>
     *   </li>
     *   <li><strong>Jackson Resources:</strong>
     *     <ul>
     *       <li>Creates a default factory for working with Jackson for JSON processing.</li>
     *     </ul>
     *   </li>
     *   <li><strong>Sharding Strategy:</strong>
     *     <ul>
     *       <li>Defines the sharding strategy for the Discord bot.</li>
     *     </ul>
     *   </li>
     *   <li><strong>RabbitMQ Setup:</strong>
     *     <ul>
     *       <li>Configures RabbitMQ settings and initializes RabbitMQ components for message handling.</li>
     *     </ul>
     *   </li>
     *   <li><strong>Redis Store Service:</strong>
     *     <ul>
     *       <li>Sets up the Redis store service and retries connection if it fails.</li>
     *     </ul>
     *   </li>
     *   <li><strong>Discord Client Initialization:</strong>
     *     <ul>
     *       <li>Builds and configures the <code>GatewayDiscordClient</code> with various options including rate limiting, sharding, intents, and event mapping.</li>
     *       <li>Logs in the client and starts the <code>LogoutHttpServer</code>.</li>
     *     </ul>
     *   </li>
     *   <li><strong>Utility Method:</strong>
     *     <ul>
     *       <li>Contains a utility method <code>sleep</code> to pause execution for a specified duration.</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p>This method ensures that all necessary components are properly configured and initialized for the Discord bot to operate smoothly.</p>
     */
    private static void leaderBuilder() {
        // Define the location of the Global Router Server (GRS)
        InetSocketAddress globalRouterServerAddress = new InetSocketAddress(Constants.GLOBAL_ROUTER_SERVER_HOST, Constants.GLOBAL_ROUTER_SERVER_PORT);

        // Define the location of the Shard Coordinator Server (SCS)
        InetSocketAddress coordinatorServerAddress = new InetSocketAddress(Constants.SHARD_COORDINATOR_SERVER_HOST, Constants.SHARD_COORDINATOR_SERVER_PORT);

        // Define the redis server that will be used as entity cache
        LOGGER.debug("Waiting 3s for connection to redis...");
        sleep(3000, "sleeping before initialization of redis got interrupted-exception");
        RedisURI redisURI = RedisURI.builder().withHost(Constants.REDIS_CLIENT_HOST).withPort(Constants.REDIS_CLIENT_PORT).withSsl(true).build();
        RedisClient redisClient = RedisClient.create(redisURI);

        // Create a default factory for working with Jackson
        JacksonResources jackson = JacksonResources.create();

        // Define the sharding strategy
        ShardingStrategy shardingStrategy = ShardingStrategy.recommended();

        // Define the key resources for working with RabbitMQ
        ConnectRabbitMQ rabbitMQ;
        if (!Constants.RABBITMQ_HOST.isEmpty()) {
            ConnectRabbitMQSettings settings = ConnectRabbitMQSettings.create().withAddress(Constants.RABBITMQ_HOST, Constants.RABBITMQ_PORT);
            rabbitMQ = ConnectRabbitMQ.createFromSettings(settings);
        } else {
            rabbitMQ = ConnectRabbitMQ.createDefault();
        }
        RabbitMQSinkMapper sink = RabbitMQSinkMapper.createBinarySinkToDirect("payload");
        RabbitMQSourceMapper source = RabbitMQSourceMapper.createBinarySource();

        StoreService redisStoreService = null;
        Store redisStore = null;
        while (redisStoreService == null) {
            try {
                redisStoreService = RedisStoreService.builder()
                        .redisClient(redisClient)
                        .useSharedConnection(false)
                        .build();
                redisStore = Store.fromLayout(LegacyStoreLayout.of(redisStoreService));
            } catch (RedisConnectionException connectionException) {
                LOGGER.warn("Connection to redis failed, try again in 3s...");
                sleep(3000, "sleeping while waiting for connection to redis got interrupted-exception");
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
                .setStore(redisStore)
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

    /**
     * Utility method to sleep for a specified duration.
     *
     * @param millisToSleep Duration in milliseconds to sleep
     * @param interruptionText Text to log if the sleep is interrupted
     */
    private static void sleep(long millisToSleep, String interruptionText) {
        try {
            Thread.sleep(millisToSleep);
        } catch (InterruptedException e) {
            LOGGER.warn(interruptionText);
            Thread.currentThread().interrupt();
        }
    }

}