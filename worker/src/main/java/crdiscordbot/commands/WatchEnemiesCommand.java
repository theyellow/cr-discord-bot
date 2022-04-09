package crdiscordbot.commands;

import crdiscordbot.ClashRoyalClanAPI;
import crdiscordbot.StandardUtils;
import crdiscordbot.model.*;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.Interaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class WatchEnemiesCommand implements SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatchEnemiesCommand.class);

    @Override
    public String getName() {
        return "watchenemies";
    }

    @Autowired
    private ClashRoyalClanAPI royalRestClanAPI;

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        /*
        Since slash command options are optional according to discord, we will wrap it into the following function
        that gets the value of our option as a String without chaining several .get() on all the optional values
         */
        Optional<ApplicationCommandInteractionOption> clanIdOpt = event.getOption("clanid");
        String clanTag;
        if (clanIdOpt.isPresent()) {
            clanTag = clanIdOpt
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString).orElse(""); //This is warning us that we didn't check if its present, we can ignore this on required options
        } else {
            clanTag = System.getenv("CLAN_ID");
        }
        String result;
        if (clanTag.isEmpty()) {
            result = "No clan given, either set CLAN_ID system-variable for bot or use a parameter clanTag.";
        } else {
            LOGGER.info("Searched for clan tag: {}", clanTag);
            RiverRace currentRiverRace = royalRestClanAPI.getCurrentRiverRace(clanTag);
            result = createAnswer(event.getInteraction(), currentRiverRace);

            List<RiverRaceClan> otherClans = currentRiverRace.getClans();
            otherClans.removeIf(clan -> clan.getTag().equals(clanTag));
            StringBuilder resultBuilder = new StringBuilder(result);
            for (RiverRaceClan clan: otherClans) {
                resultBuilder.append(getInformationForRiverRace(royalRestClanAPI.getCurrentRiverRace(clan.getTag())));
            }
            result = resultBuilder.toString();
//            Clan clan = royalRestClanAPI.getClan(clanTag);
//            if (null != clan) {
//                LOGGER.info("Found it!");
//                result = clan.getName() + " : " + clan.getDescription() + " : " + clan.getClanChestStatus() + " : " + clan.getClanScore();
//            } else {
//                LOGGER.warn("No clan found with tag {}", clanTag);
//                result = "No result";
//            }
        }

        //Reply to the slash command, with the name the user supplied
        return  event.reply()
            .withEphemeral(true)
            .withContent(result);
    }

    private static String getInformationForRiverRace(RiverRace currentRiverRace) {
        StringBuilder resultBuilder = new StringBuilder();

        RiverRaceClan currentClan = currentRiverRace.getClan();


        // end of time for collection
        String collectionEndTime = currentRiverRace.getCollectionEndTime();

        // end of war
        String warEndTime = currentRiverRace.getWarEndTime();

        // current state of river race
        String riverRaceState = currentRiverRace.getState();


        // participants of own clan
        List<WarParticipant> participants = currentRiverRace.getParticipants();

        // battles played by own clan
        Integer battlesPlayed = getWarParticipantsIntField(participants, WarParticipant::getBattlesPlayed);

        // battles won by own clan
        Integer wins = getWarParticipantsIntField(participants, WarParticipant::getWins);

        // all battles of own clan
        Integer numberOfBattles = getWarParticipantsIntField(participants, WarParticipant::getNumberOfBattles);

        resultBuilder
                .append("So far there are ")
                .append(wins)
                .append(" battles won by ")
                .append(currentClan.getName()).append(", ")
                .append(battlesPlayed)
                .append(" battles were played and ")
                .append(numberOfBattles - battlesPlayed)
                .append(" were not played yet. \n\n");

        return resultBuilder.toString();
    }

    private static String createAnswer(Interaction interaction, RiverRace currentRiverRace) {
        StringBuilder sb = new StringBuilder();

        RiverRaceClan currentClan = currentRiverRace.getClan();


        // end of time for collection
        String collectionEndTime = currentRiverRace.getCollectionEndTime();

        // end of war
        String warEndTime = currentRiverRace.getWarEndTime();

        // current state of river race
        String riverRaceState = currentRiverRace.getState();


        // participants of own clan
        List<WarParticipant> participants = currentRiverRace.getParticipants();

        // battles played by own clan
        Integer battlesPlayed = getWarParticipantsIntField(participants, WarParticipant::getBattlesPlayed);

        // battles won by own clan
        Integer wins = getWarParticipantsIntField(participants, WarParticipant::getWins);

        // all battles of own clan
        Integer numberOfBattles = getWarParticipantsIntField(participants, WarParticipant::getNumberOfBattles);


        sb.append("So far there are ");
        sb.append(wins);
        sb.append(" battles won by our clan, ");
        sb.append(battlesPlayed);
        sb.append(" battles were played and ");
        sb.append(numberOfBattles - battlesPlayed);
        sb.append(" were not played yet. \n\n");


        // create clan distinction between own clan and other clans
        List<RiverRaceClan> otherClans = currentRiverRace.getClans();
        otherClans.removeIf(clan -> clan.getTag().equals(currentClan.getTag()));
        // Sort by (river-race) fame
        otherClans.sort((c,d) -> {
            if (null == c.getFame() || null == d.getFame()) {
                return 0;
            }
            return Integer.compare(c.getFame(),d.getFame());
        });

        // List of other teams participants
        List<List<RiverRaceClanParticipant>> otherParticipants = otherClans.stream().map(RiverRaceClan::getParticipants).collect(Collectors.toList());

        // List of fame by other teams
        List<Integer> fame = getOtherWarParticipantsIntField(otherParticipants, RiverRaceClanParticipant::getFame);

        // List of boat attacks played by other teams
        List<Integer> boatAttacks = getOtherWarParticipantsIntField(otherParticipants, RiverRaceClanParticipant::getBoatAttacks);

        // Get more information about other teams
        //otherClans.stream().map(x -> x.getTag()).map(x -> getInformationAboutClan(x)).collect(Collectors.toList());


//        String clanTagFromResult = currentRiverRaceClan.getTag();
//                    .stream().map(y -> y.getParticipants()).map(x -> x.stream().map(y -> y.getBoatAttacks()).)

        sb.append(" Other clans have following fame and boat-attacks: ");
        sb.append("```");
        for (int i = 0; i < fame.size(); i++) {
            sb
                    .append(otherClans.get(i).getName())
                    .append(" : ")
                    .append(fame.get(i))
                    .append(" points and ")
                    .append(boatAttacks.get(i))
                    .append(" attacks (")
                    .append(otherParticipants.get(i).size())
                    .append(" participants so far) \n");
        }
        sb.append("```");

        return sb.toString();
    }

    private static List<Integer> getOtherWarParticipantsIntField(List<List<RiverRaceClanParticipant>> clans, Function<RiverRaceClanParticipant, Integer> extractionFunc) {
        return clans
                .stream().map(
                        participants ->
                                getFieldWithDefault(participants, extractionFunc, StandardUtils::addStatic, 0))
                .collect(Collectors.toList());
    }

    private static int getWarParticipantsIntField(List<WarParticipant> participants, Function<WarParticipant, Integer> extractionFunc) {
        return getWarParticipantsFieldWithDefault(participants, extractionFunc, StandardUtils::addStatic,0);
    }


    private static <T> T getWarParticipantsField(List<WarParticipant> participants, Function<WarParticipant, T> extractionFunc, BiFunction<T, T, T> reducerFunc) {
        return getWarParticipantsFieldWithDefault(participants, extractionFunc, reducerFunc,null);
    }

    private static <T> T getWarParticipantsFieldWithDefault (List<WarParticipant> participants, Function<WarParticipant, T> extractionFunc, BiFunction<T, T, T> reducerFunc, T defaultValue) {
        return getFieldWithDefault(participants, extractionFunc, reducerFunc, defaultValue);
    }

    public static <T, U> T getFieldWithDefault (List<U> incomingList, Function<U, T> extractionFunc, BiFunction<T, T, T> reducerFunc, T defaultValue) {
        if (null == incomingList) {
            return defaultValue;
        } else {
            return incomingList.stream().map(extractionFunc).reduce(reducerFunc::apply).orElse(defaultValue);
        }
    }
}
