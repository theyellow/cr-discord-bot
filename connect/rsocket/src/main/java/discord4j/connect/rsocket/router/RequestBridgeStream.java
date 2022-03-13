/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.connect.rsocket.router;

import discord4j.rest.request.GlobalRateLimiter;
import discord4j.rest.request.RequestQueue;
import discord4j.rest.request.RequestQueueFactory;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;

/**
 * Represents a queue of requests for a single Discord API bucket, consumed serially so they can be checked against a
 * {@link GlobalRateLimiter} and properly delayed in case a rate limit is in place.
 */
public class RequestBridgeStream {

    private static final Logger log = Loggers.getLogger(RequestBridgeStream.class);

    private final String id;
    private final RequestQueue<RequestBridge<Void>> requestQueue;
    private final GlobalRateLimiter globalRateLimiter;
    private final Scheduler rateLimitScheduler;

    private volatile Duration sleepTime = Duration.ZERO;

    public RequestBridgeStream(String id, GlobalRateLimiter globalRateLimiter, Scheduler rateLimitScheduler,
                               RequestQueueFactory requestQueueFactory) {
        this.id = id;
        this.requestQueue = requestQueueFactory.create();
        this.globalRateLimiter = globalRateLimiter;
        this.rateLimitScheduler = rateLimitScheduler;
    }

    public void setSleepTime(Duration sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void push(RequestBridge<Void> request) {
        requestQueue.push(request);
    }

    public void start() {
        requestQueue.requests().subscribe(new RequestSubscriber());
    }

    private class RequestSubscriber extends BaseSubscriber<RequestBridge<Void>> {

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            request(1);
        }

        @Override
        protected void hookOnNext(RequestBridge<Void> correlation) {
            String request = correlation.getRequest();
            MonoProcessor<Void> acquire = correlation.getAcquire();
            MonoProcessor<Void> release = correlation.getRelease();
            if (log.isDebugEnabled()) {
                log.debug("Accepting request in bucket {}: {}", id, request);
            }

            globalRateLimiter.withLimiter(
                    Mono.defer(() -> release)
                            .doOnSubscribe(s -> acquire.onComplete())
                            .doFinally(this::next))
                    .subscribe(null, t -> log.error("Error while processing {}", request, t));
        }

        private void next(SignalType signal) {
            Mono.delay(sleepTime, rateLimitScheduler).subscribe(l -> {
                if (log.isDebugEnabled()) {
                    log.debug("Ready to consume next request in bucket {} after {}", id, signal);
                }
                sleepTime = Duration.ZERO;
                request(1);
            }, t -> log.error("Error while scheduling next request in bucket {}", id, t));
        }
    }
}
