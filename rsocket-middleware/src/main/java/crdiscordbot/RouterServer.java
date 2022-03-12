package crdiscordbot;

import crdiscordbot.Constants;
import discord4j.connect.rsocket.global.RSocketGlobalRouterServer;
import discord4j.rest.request.BucketGlobalRateLimiter;
import discord4j.rest.request.RequestQueueFactory;
import io.micronaut.discovery.event.ServiceReadyEvent;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import io.rsocket.transport.netty.server.CloseableChannel;
import jakarta.inject.Singleton;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.retry.Retry;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.Semaphore;

/**
 * An example of an {@link RSocketGlobalRouterServer}, capable of providing permits to coordinate REST requests across
 * multiple nodes and also handle global rate limits.
 */

@Singleton
//@Controller("/routerserver")
public class RouterServer {

    private static final Logger log = Loggers.getLogger(CrDiscordRSocketMiddleware.class);
    private static RSocketGlobalRouterServer routerServer;
    private final Semaphore mutex = new Semaphore(1);

    @EventListener
    @Async
    public void startRouterServer(ServiceReadyEvent event) throws InterruptedException {
        mutex.acquire();
        if (routerServer == null) {
            routerServer = new RSocketGlobalRouterServer(new InetSocketAddress(Constants.GLOBAL_ROUTER_SERVER_PORT),
                    BucketGlobalRateLimiter.create(), Schedulers.parallel(), RequestQueueFactory.buffering());
            mutex.release();
            routerServer.start()
                    .doOnNext(cc -> log.info("Started global router server at {}", cc.address()))
                    .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1)).maxBackoff(Duration.ofMinutes(1)))
                    .flatMap(CloseableChannel::onClose)
                    .block();
        } else {
            mutex.release();
            log.info("routerserver already running, so no start will be performed");
        }
    }

    public boolean isExisting() {
        return null != routerServer;
    }

}
