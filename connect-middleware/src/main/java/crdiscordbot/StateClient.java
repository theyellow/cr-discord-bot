package crdiscordbot;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

import java.util.List;

/**
 * StateClient is a Micronaut HTTP client interface for interacting with the state service.
 * It provides a method to retrieve all state results.
 */
@Client(id = "state", path = "/state")
public interface StateClient {

    /**
     * Sends a GET request to the /state endpoint to retrieve all state results.
     *
     * @return a list of StateResult.ServiceResult objects representing the state results.
     */
    @Get
    List<StateResult.ServiceResult> findAll();

}