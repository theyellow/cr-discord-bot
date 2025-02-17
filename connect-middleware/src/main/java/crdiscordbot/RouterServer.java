package crdiscordbot;

import discord4j.connect.rsocket.global.RSocketGlobalRouterServer;
import discord4j.rest.request.BucketGlobalRateLimiter;
import discord4j.rest.request.RequestQueueFactory;
import io.micronaut.discovery.event.ServiceReadyEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.rsocket.transport.netty.server.CloseableChannel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.retry.Retry;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An example of an {@link RSocketGlobalRouterServer}, capable of providing permits to coordinate REST requests across
 * multiple nodes and also handle global rate limits.
 */
@Singleton
public class RouterServer {

    private static final Logger log = Loggers.getLogger(CrDiscordRSocketMiddleware.class);
    private boolean routerServerStarted;
    private final ExecutorService pool = Executors.newFixedThreadPool(1);

    @Inject
    private PayloadServer payloadServer;

    @Inject
    private ShardCoordinatorServer shardCoordinatorServer;

    /**
     * Starts the RSocketGlobalRouterServer, PayloadServer, and ShardCoordinatorServer when the service is ready.
     *
     * @param event the service ready event
     */
    @EventListener
    public void startServer(ServiceReadyEvent event) {
        pool.execute(() -> {
            final Logger log = Loggers.getLogger(CrDiscordRSocketMiddleware.class);
            RSocketGlobalRouterServer routerServer = new RSocketGlobalRouterServer(new InetSocketAddress(Constants.GLOBAL_ROUTER_SERVER_PORT),
                    BucketGlobalRateLimiter.create(), Schedulers.parallel(), RequestQueueFactory.buffering());
            routerServer.start()
                    .doOnNext(cc -> log.info("Started global router server at {}", cc.address()))
                    .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1)).maxBackoff(Duration.ofMinutes(1)))
                    .flatMap(CloseableChannel::onClose)
                    .block();
        });
        routerServerStarted = true;
        log.info("Try to start PayloadServer");
        payloadServer.startPayloadServer();
        log.info("Try to start ShardCoordinator");
        shardCoordinatorServer.startShardCoordinator();
    }

    /**
     * Checks if the router server has been started.
     *
     * @return true if the router server has been started, false otherwise
     */
    public boolean isExisting() {
        return routerServerStarted;
    }

}