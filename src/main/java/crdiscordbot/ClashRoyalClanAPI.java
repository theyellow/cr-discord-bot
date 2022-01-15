package crdiscordbot;

import crdiscordbot.model.Clan;
import crdiscordbot.model.ClanSearchResult;
import crdiscordbot.model.RiverRace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClashRoyalClanAPI {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClashRoyalClanAPI.class);

    @Autowired
    private ClashRoyalAPI clashRoyalAPI;

    public Clan getClan(String clanTag) {
        return clashRoyalAPI.getWithCommand("clans", clanTag, Clan.class);
    }

    public ClanSearchResult getAllClansForName(String name) {
        LOGGER.debug("getAllClansFor({})", name);
        return clashRoyalAPI.getForUrl(clashRoyalAPI.ROYAL_API + "clans?name=" + name + "&limit=50&minScore=15000",
                ClanSearchResult.class);
    }

    public RiverRace getCurrentRiverRace(String clanTag) {
        LOGGER.debug("getCurrentRiverRace({})", clanTag);
        return clashRoyalAPI.getForUrl(clashRoyalAPI.ROYAL_API + "clans/" + clashRoyalAPI.urlEncodeArgument(clanTag) + "/currentriverrace",
                RiverRace.class);
    }

}
