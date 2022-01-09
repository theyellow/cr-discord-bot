package crdiscordbot;

import crdiscordbot.model.Clan;
import crdiscordbot.model.ClanSearchResult;
import crdiscordbot.model.SearchResultClan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;

@Component
public class ClassRoyalAPI {


    private static String ROYAL_API = "https://api.clashroyale.com/v1/";

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

    public String getForUrl(String url) {
        return royalRestClient.getForObject(url, String.class);
    }

    public Clan getClanForUrl(String url) {
        URI realUrl = null;
        try {
            realUrl = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        System.out.println("RestTemplate uses URI: " + realUrl.toString());
        return royalRestClient.getForObject(realUrl, Clan.class);
    }

    public String getWithCommand(String command, String argument) {
        return getForUrl(createUrl(command, argument));
    }

    public Clan getClan(String clanTag) {
        return getClanForUrl(createUrl("clans", clanTag));
    }

    public ClanSearchResult getAllClansForName(String name) {
        return getClanSearchForUrl(ROYAL_API + "clans?name=" + name + "&limit=50&minScore=15000");
    }

    private ClanSearchResult getClanSearchForUrl(String url) {
        return royalRestClient.getForObject(url, ClanSearchResult.class);
    }

    private String createUrl(String command, String argument) {
        String url = ROYAL_API + command + "/" + argument;
        System.out.println("API-URL: " + url);
        try {
            argument = URLEncoder.encode(argument, "utf8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            System.out.println("Should never happen ISO-8859-15 is not supported by your java");
        }
        url = ROYAL_API + command + "/" + argument;
        System.out.println("API-Call (encoded): " + url);
        return url;
    }

}
