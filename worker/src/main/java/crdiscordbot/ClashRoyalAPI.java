package crdiscordbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

/**
 * Component for interacting with the Clash Royale API.
 */
@Component
public class ClashRoyalAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClashRoyalAPI.class);

    public static final String ROYAL_API = "https://api.clashroyale.com/v1/";

    @Autowired
    public RestTemplate royalRestClient;

    /**
     * Creates an HttpEntity with the given JSON request body.
     *
     * @param requestJson the JSON request body
     * @return the created HttpEntity
     */
    private HttpEntity<String> createEntityWithAuthorization(String requestJson) {
        return new HttpEntity<>(requestJson);
    }

    /**
     * Sends a POST request to the given URL with the provided JSON request body.
     *
     * @param url the URL to send the request to
     * @param requestJson the JSON request body
     * @return the response as a JSON string
     */
    public String postForJson(String url, String requestJson) {
        return royalRestClient.postForObject(url, createEntityWithAuthorization(requestJson), String.class);
    }

    /**
     * Sends a POST request to the Clash Royale API with the given command and JSON request body.
     *
     * @param command the API command
     * @param requestJson the JSON request body
     * @return the response as a JSON string
     */
    public String postCommandForJson(String command, String requestJson) {
        return postForJson(ROYAL_API + command, requestJson);
    }

    /**
     * Sends a GET request to the given URL and returns the response as an object of the specified type.
     *
     * @param url the URL to send the request to
     * @param returnClass the class of the response object
     * @param <T> the type of the response object
     * @return the response object
     */
    public <T> T getForUrl(String url, Class<T> returnClass) {
        URI uri = createUri(url);
        if (null == uri) {
            return null;
        }
        return royalRestClient.getForObject(uri, returnClass);
    }

    /**
     * Sends a GET request to the given URI and returns the response as an object of the specified type.
     *
     * @param uri the URI to send the request to
     * @param returnClass the class of the response object
     * @param <T> the type of the response object
     * @return the response object
     */
    public <T> T getForUrl(URI uri, Class<T> returnClass) {
        return royalRestClient.getForObject(uri, returnClass);
    }

    /**
     * Sends a GET request to the Clash Royale API with the given command and argument, and returns the response as an object of the specified type.
     *
     * @param command the API command
     * @param argument the argument for the command
     * @param returnClass the class of the response object
     * @param <T> the type of the response object
     * @return the response object
     */
    public <T> T getWithCommand(String command, String argument, Class<T> returnClass) {
        return getForUrl(createUrl(command, argument), returnClass);
    }

    /**
     * Creates a URL for the given command and argument.
     *
     * @param command the API command
     * @param argument the argument for the command
     * @return the created URL
     */
    private String createUrl(String command, String argument) {
        return MessageFormat.format("{}{}/{}", ROYAL_API, command, urlEncodeArgument(argument));
    }

    /**
     * URL-encodes the given argument.
     *
     * @param argument the argument to encode
     * @return the URL-encoded argument
     */
    public String urlEncodeArgument(String argument) {
        argument = URLEncoder.encode(argument, StandardCharsets.UTF_8).replace("\\+", "%20");
        return argument;
    }

    /**
     * Creates a URI from the given URL string.
     *
     * @param url the URL string
     * @return the created URI, or null if the URL is invalid
     */
    private URI createUri(String url) {
        URI realUrl = null;
        try {
            realUrl = new URI(url);
        } catch (URISyntaxException e) {
            LOGGER.warn("URL {} is not valid: {}", url, e.getMessage());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Created URI toString(): {}", realUrl);
        }
        return realUrl;
    }

}