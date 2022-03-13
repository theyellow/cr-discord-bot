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

import discord4j.connect.rsocket.shard.RSocketShardCoordinatorServer;
import io.micronaut.discovery.event.ServiceReadyEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.InetSocketAddress;
import java.util.concurrent.Semaphore;

@Singleton
//@Controller("/shardcoordinator")
public class ShardCoordinatorServer {

    private static final Logger log = Loggers.getLogger(ShardCoordinatorServer.class);
    private static RSocketShardCoordinatorServer coordinatorServer;
    private final Semaphore mutex = new Semaphore(1);

    @Inject
    private PayloadServer payloadServer;

    @EventListener
    @Async
    public void startShardCoordinator(ServiceReadyEvent event) throws InterruptedException {
        while(!payloadServer.isExisting()) {
            Thread.sleep(2000);
        }
        mutex.acquire();
        if (coordinatorServer == null) {
            coordinatorServer = new RSocketShardCoordinatorServer(new InetSocketAddress(Constants.SHARD_COORDINATOR_SERVER_PORT));
            mutex.release();
            coordinatorServer
                    .start()
                    .doOnNext(cc -> log.info("Started shard coordinator server at {}", cc.address()))
                    .blockOptional()
                    .orElseThrow(RuntimeException::new)
                    .onClose()
                    .block();
        } else {
            mutex.release();
            log.info("shardcoordinator is existing, so no start will be done");
        }
    }

    public boolean isExisting() {
        return null != coordinatorServer;
    }

}
