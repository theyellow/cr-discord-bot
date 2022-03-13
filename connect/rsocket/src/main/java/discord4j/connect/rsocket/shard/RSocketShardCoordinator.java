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

package discord4j.connect.rsocket.shard;

import discord4j.common.retry.ReconnectOptions;
import discord4j.connect.rsocket.ConnectRSocket;
import discord4j.core.shard.ShardCoordinator;
import discord4j.gateway.SessionInfo;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.limiter.PayloadTransformer;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.InetSocketAddress;

public class RSocketShardCoordinator implements ShardCoordinator {

    private static final Logger log = Loggers.getLogger(RSocketShardCoordinator.class);

    private final ConnectRSocket socket;

    @Deprecated
    public RSocketShardCoordinator(InetSocketAddress socketAddress) {
        this.socket = new ConnectRSocket("coordinator", socketAddress, ctx -> true, ReconnectOptions.create());
    }

    public static RSocketShardCoordinator createWithServerAddress(InetSocketAddress socketAddress) {
        return new RSocketShardCoordinator(socketAddress);
    }

    @Override
    public PayloadTransformer getIdentifyLimiter(ShardInfo shardInfo, int shardingFactor) {
        int key = shardInfo.getIndex() % shardingFactor;
        return sequence -> Flux.from(sequence)
                .flatMap(buf -> socket.withSocket(rSocket ->
                        rSocket.requestResponse(DefaultPayload.create("identify." + key))
                                .doOnNext(payload -> log.debug(">: {}", payload.getDataUtf8())))
                        .then(Mono.just(buf)));
    }

    @Override
    public Mono<Void> publishConnected(ShardInfo shard) {
        return socket.withSocket(rSocket -> rSocket.fireAndForget(DefaultPayload.create("notify.connected"))).then();
    }

    @Override
    public Mono<Void> publishDisconnected(ShardInfo shard, SessionInfo session) {
        return socket.withSocket(rSocket -> rSocket.fireAndForget(DefaultPayload.create("notify.disconnected"))).then();
    }

    @Override
    public Mono<Integer> getConnectedCount() {
        return socket.withSocket(rSocket -> rSocket.requestResponse(DefaultPayload.create("request.connected"))
                .map(payload -> Integer.parseInt(payload.getDataUtf8())))
                .next();
    }
}

