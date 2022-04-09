package crdiscordbot;

import crdiscordbot.model.Clan;
import crdiscordbot.model.CurrentRiverRace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ClashRoyalClanAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClashRoyalClanAPI.class);

    @Autowired
    private ClashRoyalAPI clashRoyalAPI;

    public Clan getClan(String clanTag) {
        return clashRoyalAPI.getWithCommand("clans", clanTag, Clan.class);
    }

    public List<Clan> getAllClansForName(String name) {
        LOGGER.debug("getAllClansFor({})", name);
        return clashRoyalAPI.getForUrl(ClashRoyalAPI.ROYAL_API + "clans?name=" + name + "&limit=50&minScore=15000",
                List.class);
    }

    public CurrentRiverRace getCurrentRiverRace(String clanTag) {
        LOGGER.debug("getCurrentRiverRace({})", clanTag);
        return clashRoyalAPI.getForUrl(ClashRoyalAPI.ROYAL_API + "clans/" + clashRoyalAPI.urlEncodeArgument(clanTag) + "/currentriverrace",
                CurrentRiverRace.class);
    }

    @Async
    public CompletableFuture<Boolean> startAsyncEnemiesWatch() {

        return CompletableFuture.completedFuture(true);
    }

}
