package crdiscordbot;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

import java.util.List;

@Client(id = "state", path = "/state")
public interface StateClient {

    @Get
    List<StateResult.ServiceResult> findAll();

}
