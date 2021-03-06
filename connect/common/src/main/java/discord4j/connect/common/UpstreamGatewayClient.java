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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.connect.common;

import discord4j.common.close.CloseStatus;
import discord4j.discordjson.json.gateway.Dispatch;
import discord4j.gateway.*;
import discord4j.gateway.json.GatewayPayload;
import discord4j.gateway.payload.PayloadReader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.reactivestreams.Publisher;
import reactor.core.Scannable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

/**
 * A {@link GatewayClient} implementation that connects to Discord Gateway through {@link DefaultGatewayClient} while
 * also providing a way to route messages through {@link PayloadSink} and {@link PayloadSource}, capable of delivering
 * such messages across multiple nodes.
 */
public class UpstreamGatewayClient implements GatewayClient {

    private static final Logger log = Loggers.getLogger(UpstreamGatewayClient.class);

    private final DefaultGatewayClient delegate;
    private final PayloadSink sink;
    private final PayloadSource source;
    private final ShardInfo shardInfo;
    private final PayloadReader payloadReader;

    public UpstreamGatewayClient(ConnectGatewayOptions gatewayOptions) {
        this.delegate = new DefaultGatewayClient(gatewayOptions);
        this.shardInfo = gatewayOptions.getIdentifyOptions().getShardInfo();
        this.sink = gatewayOptions.getPayloadSink();
        this.source = gatewayOptions.getPayloadSource();
        this.payloadReader = gatewayOptions.getPayloadReader();
    }

    @Override
    public Mono<Void> execute(String gatewayUrl) {
        // Receive from Discord --> Send to downstream
        Mono<Void> senderFuture =
                sink.send(receiver(
                        buf -> Mono.just(toConnectPayload(buf.toString(StandardCharsets.UTF_8)))
                                .doFinally(s -> buf.release())))
                        .subscribeOn(Schedulers.newSingle("payload-sender"))
                        .doOnError(t -> log.error("Sender error", t))
                        .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(2))
                                .maxBackoff(Duration.ofSeconds(30)))
                        .then();

        // Receive from downstream --> Send to Discord
        Mono<Void> receiverFuture = source.receive(payloadProcessor())
                .doOnError(t -> log.error("Receiver error", t))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(2))
                        .maxBackoff(Duration.ofSeconds(30)))
                .then();

        return Mono.zip(senderFuture, receiverFuture, delegate.execute(gatewayUrl)).then();
    }

    private Function<ConnectPayload, Mono<Void>> payloadProcessor() {
        Sinks.Many<GatewayPayload<?>> senderSink = sender();
        return connectPayload -> {
            if (Boolean.TRUE.equals(senderSink.scan(Scannable.Attr.CANCELLED))) {
                return Mono.error(new IllegalStateException("Sender was cancelled"));
            }
            if (connectPayload.getShard().getIndex() != shardInfo.getIndex()) {
                return Mono.empty();
            }
            // TODO: reduce allocations in this area
            return Flux.from(payloadReader.read(Unpooled.wrappedBuffer(connectPayload.getPayload().getBytes(StandardCharsets.UTF_8))))
                    .doOnNext(gatewayPayload -> {
                        while (senderSink.tryEmitNext(gatewayPayload).isFailure()) {
                            LockSupport.parkNanos(10);
                        }})
                    .then();
        };
    }

    private ConnectPayload toConnectPayload(String gatewayPayload) {
        return new ConnectPayload(shardInfo, SessionInfo.create(getSessionId(), getSequence()), gatewayPayload);
    }

    @Override
    public Mono<CloseStatus> close(boolean allowResume) {
        return delegate.close(allowResume);
    }

    @Override
    public Flux<Dispatch> dispatch() {
        return delegate.dispatch();
    }

    @Override
    public Flux<GatewayPayload<?>> receiver() {
        return delegate.receiver();
    }

    @Override
    public <T> Flux<T> receiver(Function<ByteBuf, Publisher<? extends T>> mapper) {
        return delegate.receiver(mapper);
    }

    @Override
    public Sinks.Many<GatewayPayload<?>> sender() {
        return delegate.sender();
    }

    @Override
    public Mono<Void> sendBuffer(Publisher<ByteBuf> publisher) {
        return delegate.sendBuffer(publisher);
    }

    @Override
    public int getShardCount() {
        return shardInfo.getCount();
    }

    @Override
    public String getSessionId() {
        return delegate.getSessionId();
    }

    @Override
    public int getSequence() {
        return delegate.getSequence();
    }

    @Override
    public Mono<Boolean> isConnected() {
        return delegate.isConnected();
    }

    @Override
    public Flux<GatewayConnection.State> stateEvents() {
        return delegate.stateEvents();
    }

    @Override
    public Duration getResponseTime() {
        return delegate.getResponseTime();
    }
}
