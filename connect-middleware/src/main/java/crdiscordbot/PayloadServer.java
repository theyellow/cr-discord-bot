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

import discord4j.connect.rsocket.gateway.RSocketPayloadServer;
import jakarta.inject.Singleton;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class PayloadServer {

    private static final Logger log = Loggers.getLogger(PayloadServer.class);
    private boolean payloadServerStarted;
    private final ExecutorService pool = Executors.newFixedThreadPool(1);

    public void startPayloadServer() {
        pool.execute(() -> {
            final Logger log = Loggers.getLogger(PayloadServer.class);
            RSocketPayloadServer payloadServer = new RSocketPayloadServer(new InetSocketAddress(Constants.PAYLOAD_SERVER_PORT));
            payloadServer
                    .start()
                    .doOnNext(cc -> log.info("Started payload server at {}", cc.address()))
                    .blockOptional()
                    .orElseThrow(RuntimeException::new)
                    .onClose()
                    .block();
        });
        payloadServerStarted = true;
    }

    public boolean isExisting() {
        return payloadServerStarted;
    }

}
