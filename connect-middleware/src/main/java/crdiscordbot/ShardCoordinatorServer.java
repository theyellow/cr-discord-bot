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
import io.rsocket.transport.netty.server.CloseableChannel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * ShardCoordinatorServer is responsible for managing the lifecycle of the shard coordinator server.
 * It starts the server and provides a method to check if the server is running.
 */
@Singleton
public class ShardCoordinatorServer {

    private static final Logger log = Loggers.getLogger(ShardCoordinatorServer.class);
    private boolean coordinatorServerStarted;
    private final ExecutorService pool = Executors.newFixedThreadPool(1);

    @Inject
    private StateClient stateClient;

    /**
     * Starts the shard coordinator server in a separate thread.
     * It initializes the RSocketShardCoordinatorServer and starts it.
     */
    public void startShardCoordinator() {
        pool.execute(() -> {
            final Logger log = Loggers.getLogger(ShardCoordinatorServer.class);
            log.info("Create shard coordinator...");
            RSocketShardCoordinatorServer coordinatorServer = new RSocketShardCoordinatorServer(new InetSocketAddress(Constants.SHARD_COORDINATOR_SERVER_PORT));
            log.info("Created shard coordinator!");
            Mono<CloseableChannel> channelMono = coordinatorServer.start();
            log.info("Starting shard coordinator channel...");
            channelMono
                    .doOnNext(cc -> log.info("Started shard coordinator server at {}", cc.address()))
                    .blockOptional()
                    .orElseThrow(RuntimeException::new)
                    .onClose()
                    .block();
            log.info("Channel of shard coordinator unblocked");
        });
        coordinatorServerStarted = true;
    }

    /**
     * Checks if the shard coordinator server is already started.
     *
     * @return true if the server is started, false otherwise.
     */
    public boolean isExisting() {
        return coordinatorServerStarted;
    }

}