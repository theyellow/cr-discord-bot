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

/**
 * Service class for interacting with the Clash Royale Clan API.
 */
@Service
public class ClashRoyalClanAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClashRoyalClanAPI.class);

    @Autowired
    private ClashRoyalAPI clashRoyalAPI;

    /**
     * Retrieves information about a specific clan by its tag.
     *
     * @param clanTag the tag of the clan to retrieve
     * @return the Clan object containing the clan's information
     */
    public Clan getClan(String clanTag) {
        return clashRoyalAPI.getWithCommand("clans", clanTag, Clan.class);
    }

    /**
     * Retrieves a list of clans that match the given name.
     *
     * @param name the name of the clans to search for
     * @return a list of Clan objects that match the given name
     */
    public List<Clan> getAllClansForName(String name) {
        LOGGER.debug("getAllClansFor({})", name);
        return clashRoyalAPI.getForUrl(ClashRoyalAPI.ROYAL_API + "clans?name=" + name + "&limit=50&minScore=15000",
                List.class);
    }

    /**
     * Retrieves the current river race information for a specific clan by its tag.
     *
     * @param clanTag the tag of the clan to retrieve the current river race information for
     * @return the CurrentRiverRace object containing the current river race information
     */
    public CurrentRiverRace getCurrentRiverRace(String clanTag) {
        LOGGER.debug("getCurrentRiverRace({})", clanTag);
        return clashRoyalAPI.getForUrl(ClashRoyalAPI.ROYAL_API + "clans/" + clashRoyalAPI.urlEncodeArgument(clanTag) + "/currentriverrace",
                CurrentRiverRace.class);
    }

    /**
     * Starts an asynchronous watch for enemies.
     *
     * @return a CompletableFuture that completes with a Boolean value indicating the success of the operation
     */
    @Async
    public CompletableFuture<Boolean> startAsyncEnemiesWatch() {
        return CompletableFuture.completedFuture(true);
    }

}