package crdiscordbot;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;

@Controller("/state")
public class StateController {

    @Inject
    private RouterServer routerServer;

    @Inject
    private PayloadServer payloadServer;

    @Inject
    private ShardCoordinatorServer shardCoordinatorServer;

    @Get(produces = MediaType.TEXT_PLAIN)
    public String index() {
        StringBuilder result = new StringBuilder();
        result.append(payloadServer.isExisting() ? "payload-server exists" : "no payload-server available");
        result.append(", ");
        result.append(routerServer.isExisting() ? "router-server exists" : "no router-server available");
        result.append(", ");
        result.append(shardCoordinatorServer.isExisting() ? "shardCoordinator-server exists" : "no shardCoordinator-server available");
        return result.toString();
    }

}
