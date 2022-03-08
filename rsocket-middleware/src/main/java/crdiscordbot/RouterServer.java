package crdiscordbot;

import crdiscordbot.connect.Constants;
import discord4j.connect.rsocket.global.RSocketGlobalRouterServer;
import discord4j.rest.request.BucketGlobalRateLimiter;
import discord4j.rest.request.RequestQueueFactory;
import io.rsocket.transport.netty.server.CloseableChannel;
import jakarta.inject.Singleton;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * An example of an {@link RSocketGlobalRouterServer}, capable of providing permits to coordinate REST requests across
 * multiple nodes and also handle global rate limits.
 */

@Singleton
public class RouterServer {

    private static final Logger log = Loggers.getLogger(CrDiscordRSocketMiddleware.class);
    private static final RSocketGlobalRouterServer ROUTER_SERVER = new RSocketGlobalRouterServer(new InetSocketAddress(Constants.GLOBAL_ROUTER_SERVER_PORT),
            BucketGlobalRateLimiter.create(), Schedulers.parallel(), RequestQueueFactory.buffering());


    @PostConstruct
    public void afterConstruction() {
        ROUTER_SERVER.start()
                .doOnNext(cc -> log.info("Started global router server at {}", cc.address()))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1)).maxBackoff(Duration.ofMinutes(1)))
                .flatMap(CloseableChannel::onClose)
                .block();
    }

    public boolean isExisting() {
        return null != ROUTER_SERVER;
    }

}
