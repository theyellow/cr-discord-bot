package crdiscordbot;

import crdiscordbot.model.Clan;
import crdiscordbot.model.ClanSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class ClashRoyalClanAPI {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClashRoyalAPI clashRoyalAPI;

    public Clan getClan(String clanTag) {
        return clashRoyalAPI.getWithCommand("clans", clanTag, Clan.class);
    }

    public ClanSearchResult getAllClansForName(String name) {
        return clashRoyalAPI.getForUrl(clashRoyalAPI.ROYAL_API + "clans?name=" + name + "&limit=50&minScore=15000",
                ClanSearchResult.class);
    }


}
