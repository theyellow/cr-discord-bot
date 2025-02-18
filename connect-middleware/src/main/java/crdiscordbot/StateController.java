package crdiscordbot;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for handling state-related requests.
 */
@Controller("/state")
public class StateController {

    @Inject
    private EmbeddedServer server;

    @Inject
    private RouterServer routerServer;

    @Inject
    private PayloadServer payloadServer;

    @Inject
    private ShardCoordinatorServer shardCoordinatorServer;

    /**
     * Endpoint to retrieve the state of various services.
     *
     * @return StateResult containing the status of different services.
     */
    @Get
    public StateResult findAll() {
        List<StateResult.ServiceResult> services = new ArrayList<>();
        services.add(new StateResult.ServiceResult("netty", server != null && server.isRunning() ? "is running" : "is NOT running"));
        services.add(new StateResult.ServiceResult("payload-server", payloadServer.isExisting() ? "exists" : "is NOT available"));
        services.add(new StateResult.ServiceResult("router-server", shardCoordinatorServer.isExisting() ? "exists" : "is NOT available"));
        return new StateResult(services);
    }

}