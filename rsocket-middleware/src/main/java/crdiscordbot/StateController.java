package crdiscordbot;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;

@Controller("/state")
public class StateController {

    @Inject
    private EmbeddedServer server; //

    @Inject
    private RouterServer routerServer;

    @Inject
    private PayloadServer payloadServer;

    @Inject
    private ShardCoordinatorServer shardCoordinatorServer;

    @Get(produces = MediaType.TEXT_PLAIN)
    public Mono<String> index() {
        StringBuilder result = new StringBuilder();
        result.append(server != null && server.isRunning() ? "netty is running"  : "netty is NOT running");
        result.append(", ");
        result.append(payloadServer.isExisting() ? "payload-server exists" : "NO payload-server available");
        result.append(", ");
        result.append(routerServer.isExisting() ? "router-server exists" : "NO router-server available");
        result.append(", ");
        result.append(shardCoordinatorServer.isExisting() ? "shardCoordinator-server exists" : "NO shardCoordinator-server available");
        return Mono.just(result.toString());
    }


}
