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

package crdiscordbot;

import crdiscordbot.connect.Constants;
import discord4j.connect.rsocket.shard.RSocketShardCoordinatorServer;
import jakarta.inject.Singleton;
import reactor.util.Logger;
import reactor.util.Loggers;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

@Singleton
public class ShardCoordinatorServer {

    private static final Logger log = Loggers.getLogger(ShardCoordinatorServer.class);
    private static final RSocketShardCoordinatorServer SHARD_COORDINATOR_SERVER = new RSocketShardCoordinatorServer(new InetSocketAddress(Constants.SHARD_COORDINATOR_SERVER_PORT));

    @PostConstruct
    public void startShardCoordinator(String[] args) {
        SHARD_COORDINATOR_SERVER
                .start()
                .doOnNext(cc -> log.info("Started shard coordinator server at {}", cc.address()))
                .blockOptional()
                .orElseThrow(RuntimeException::new)
                .onClose()
                .block();
    }

    public boolean isExisting() {
        return null != SHARD_COORDINATOR_SERVER;
    }

}
