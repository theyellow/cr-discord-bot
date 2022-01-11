package crdiscordbot;

import crdiscordbot.model.Clan;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        return royalRestClient.getForObject(createUri(url), returnClass);
    }

    public <T> T getForUrl(URI uri, Class<T> returnClass) {
        return royalRestClient.getForObject(uri, returnClass);
    }

    public <T> T  getWithCommand(String command, String argument, Class<T> returnClass) {
        return getForUrl(createUrl(command, argument), returnClass);
    }

    private String createUrl(String command, String argument) {
        String url = ROYAL_API + command + "/" + argument;
        try {
            argument = URLEncoder.encode(argument, "utf8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            logger.warn("Should never happen ISO-8859-15 is not supported by your java");
        }
        url = ROYAL_API + command + "/" + argument;
        return url;
    }

    private URI createUri(String url) {
        URI realUrl = null;
        try {
            realUrl = new URI(url);
        } catch (URISyntaxException e) {
            logger.warn("URL {} is not valid: {}", url, e.getMessage());
        }
        return realUrl;
    }

}
