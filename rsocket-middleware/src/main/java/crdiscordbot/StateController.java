package crdiscordbot;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

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

    @Get
    public StateResult findAll() {
        List<StateResult.ServiceResult> services = new ArrayList<>();
        services.add(new StateResult.ServiceResult("netty", server != null && server.isRunning() ? "is running" : "is NOT running"));
        services.add(new StateResult.ServiceResult("payload-server", payloadServer.isExisting() ? "exists" : "is NOT available"));
        services.add(new StateResult.ServiceResult("router-server", shardCoordinatorServer.isExisting() ? "exists" : "is NOT available"));
        StateResult result = new StateResult(services);
        return result;
    }

}
