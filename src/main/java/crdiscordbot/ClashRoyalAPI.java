package crdiscordbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

@Component
public class ClashRoyalAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClashRoyalAPI.class);

    public static String ROYAL_API = "https://api.clashroyale.com/v1/";

    @Autowired
    public RestTemplate royalRestClient;

    private HttpEntity<String> createEntityWithAuthorization(String requestJson) {
        HttpEntity<String> entity = new HttpEntity<>(requestJson);
        return entity;
    }

    public String postForJson(String url, String requestJson) {
        return royalRestClient.postForObject(url, createEntityWithAuthorization(requestJson), String.class);
    }

    public String postCommandForJson(String command, String requestJson) {
        return postForJson(ROYAL_API + command, requestJson);
    }

    public <T> T getForUrl(String url, Class<T> returnClass) {
        URI uri = createUri(url);
        if (null == uri) {
            return null;
        }
        return royalRestClient.getForObject(uri, returnClass);
    }

    public <T> T getForUrl(URI uri, Class<T> returnClass) {
        return royalRestClient.getForObject(uri, returnClass);
    }

    public <T> T  getWithCommand(String command, String argument, Class<T> returnClass) {
        return getForUrl(createUrl(command, argument), returnClass);
    }

    private String createUrl(String command, String argument) {
        String url = String.format("{}{}/{}", ROYAL_API, command, urlEncodeArgument(argument));
        return url;
    }

    public String urlEncodeArgument(String argument) {
        try {
            argument = URLEncoder.encode(argument, "utf8").replace("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Should never happen ISO-8859-15 is not supported by your java");
        }
        return argument;
    }

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
