package crdiscordbot;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import crdiscordbot.connect.Constants;
import crdiscordbot.connect.LogoutHttpServer;
import crdiscordbot.listeners.SlashCommandListener;
import crdiscordbot.model.CurrentClanWar;
import crdiscordbot.model.CurrentRiverRace;
import crdiscordbot.model.Match;
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
import io.lettuce.core.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

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


    class RaceStateToEnumConverter implements GenericHttpMessageConverter<CurrentRiverRace.StateEnum> {

        public boolean canRead(Class<?> aClass, MediaType mediaType) {
            return aClass== CurrentRiverRace.StateEnum.class;
        }

        @Override
        public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
            return canRead(contextClass, mediaType);
        }

        @Override
        public boolean canWrite(Class<?> clazz, MediaType mediaType) {
            return false;
        }

        public boolean canWrite(Type type, Class<?> aClass, MediaType mediaType) {
            return canWrite(aClass, mediaType);
        }


        @Override
        public List<MediaType> getSupportedMediaTypes() {
            return new LinkedList<>();
        }

        @Override
        public List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
            return GenericHttpMessageConverter.super.getSupportedMediaTypes(clazz);
        }

        @Override
        public CurrentRiverRace.StateEnum read(Class<? extends CurrentRiverRace.StateEnum> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = inputMessage.getBody().read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            String string = result.toString(StandardCharsets.UTF_8);
            return CurrentRiverRace.StateEnum.valueOf(string);
        }

        public CurrentRiverRace.StateEnum read(Type type, Class<?> aClass,
                                    HttpInputMessage httpInputMessage)
                throws IOException, HttpMessageNotReadableException {
            return read(CurrentRiverRace.StateEnum.class, httpInputMessage);
        }

        @Override
        public void write(CurrentRiverRace.StateEnum stateEnum, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        }

        public void write(CurrentRiverRace.StateEnum value, Type type, MediaType mediaType,
                          HttpOutputMessage httpOutputMessage)
                throws IOException, HttpMessageNotWritableException {
        }
    }

    class ClanwarStateToEnumConverter implements GenericHttpMessageConverter<CurrentClanWar.StateEnum> {

        public boolean canRead(Class<?> aClass, MediaType mediaType) {
            return aClass== CurrentClanWar.StateEnum.class;
        }

        @Override
        public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
            return canRead(contextClass, mediaType);
        }

        @Override
        public boolean canWrite(Class<?> clazz, MediaType mediaType) {
            return false;
        }

        public boolean canWrite(Type type, Class<?> aClass, MediaType mediaType) {
            return canWrite(aClass, mediaType);
        }


        @Override
        public List<MediaType> getSupportedMediaTypes() {
            return new LinkedList<>();
        }

        @Override
        public List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
            return GenericHttpMessageConverter.super.getSupportedMediaTypes(clazz);
        }

        @Override
        public CurrentClanWar.StateEnum read(Class<? extends CurrentClanWar.StateEnum> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = inputMessage.getBody().read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            String string = result.toString(StandardCharsets.UTF_8);
            return CurrentClanWar.StateEnum.valueOf(string);
        }

        public CurrentClanWar.StateEnum read(Type type, Class<?> aClass,
                                             HttpInputMessage httpInputMessage)
                throws IOException, HttpMessageNotReadableException {
            return read(CurrentClanWar.StateEnum.class, httpInputMessage);
        }

        @Override
        public void write(CurrentClanWar.StateEnum stateEnum, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        }

        public void write(CurrentClanWar.StateEnum value, Type type, MediaType mediaType,
                          HttpOutputMessage httpOutputMessage)
                throws IOException, HttpMessageNotWritableException {
        }
    }

    class MatchStateToEnumConverter implements GenericHttpMessageConverter<Match.StateEnum> {

        public boolean canRead(Class<?> aClass, MediaType mediaType) {
            return aClass== Match.StateEnum.class;
        }

        @Override
        public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
            return canRead(contextClass, mediaType);
        }

        @Override
        public boolean canWrite(Class<?> clazz, MediaType mediaType) {
            return false;
        }

        public boolean canWrite(Type type, Class<?> aClass, MediaType mediaType) {
            return canWrite(aClass, mediaType);
        }


        @Override
        public List<MediaType> getSupportedMediaTypes() {
            return new LinkedList<>();
        }

        @Override
        public List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
            return GenericHttpMessageConverter.super.getSupportedMediaTypes(clazz);
        }

        @Override
        public Match.StateEnum read(Class<? extends Match.StateEnum> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = inputMessage.getBody().read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            String string = result.toString(StandardCharsets.UTF_8);
            return Match.StateEnum.valueOf(string);
        }

        public Match.StateEnum read(Type type, Class<?> aClass,
                                             HttpInputMessage httpInputMessage)
                throws IOException, HttpMessageNotReadableException {
            return read(Match.StateEnum.class, httpInputMessage);
        }

        @Override
        public void write(Match.StateEnum stateEnum, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        }

        public void write(Match.StateEnum value, Type type, MediaType mediaType,
                          HttpOutputMessage httpOutputMessage)
                throws IOException, HttpMessageNotWritableException {
        }
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
        RedisURI redisURI = RedisURI.builder().withHost(Constants.REDIS_CLIENT_HOST).withPort(Constants.REDIS_CLIENT_PORT).build();
        RedisClient redisClient = RedisClient.create(redisURI);

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
        RestTemplate restTemplate = new RestTemplateBuilder(rt-> {
            rt.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().clear();
                request.getHeaders().add("Authorization", "Bearer " + apiToken);
                return execution.execute(request, body);
            });
            rt.getMessageConverters().add(new ClanwarStateToEnumConverter());
            rt.getMessageConverters().add(new RaceStateToEnumConverter());
            rt.getMessageConverters().add(new MatchStateToEnumConverter());
        }).build();
        LOGGER.info("REST-Engine for CR started...");
        return restTemplate;
    }

}
