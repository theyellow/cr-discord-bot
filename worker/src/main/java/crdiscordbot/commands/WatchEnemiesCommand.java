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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Component that handles the "watchenemies" slash command.
 */
@Component
public class WatchEnemiesCommand implements SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatchEnemiesCommand.class);

    /**
     * Returns the name of the slash command.
     *
     * @return the name of the command
     */
    @Override
    public String getName() {
        return "watchenemies";
    }

    @Autowired
    private ClashRoyalClanAPI royalRestClanAPI;

    /**
     * Handles the slash command interaction event.
     *
     * @param event the chat input interaction event
     * @return a Mono that completes when the reply is sent
     */
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        // Get the clan ID from the command option or environment variable
        Optional<ApplicationCommandInteractionOption> clanIdOpt = event.getOption("clanid");
        String clanTag;
        if (clanIdOpt.isPresent()) {
            clanTag = clanIdOpt
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString).orElse(""); // This is warning us that we didn't check if its present, we can ignore this on required options
        } else {
            clanTag = System.getenv("CLAN_ID");
        }

        String result;
        if (clanTag.isEmpty()) {
            result = "No clan given, either set CLAN_ID system-variable for bot or use a parameter clanTag.";
        } else {
            LOGGER.info("Searched for clan tag: {}", clanTag);
            CurrentRiverRace currentRiverRace = royalRestClanAPI.getCurrentRiverRace(clanTag);
            result = createAnswer(event.getInteraction(), currentRiverRace);

            List<RiverRaceClan> otherClans = currentRiverRace.getClans();
            otherClans.removeIf(clan -> clan.getTag().equals(clanTag));
            StringBuilder resultBuilder = new StringBuilder(result);
            for (RiverRaceClan clan : otherClans) {
                resultBuilder.append(getInformationForRiverRace(royalRestClanAPI.getCurrentRiverRace(clan.getTag())));
            }
            result = resultBuilder.toString();
        }

        // Reply to the slash command with the result
        return event.reply()
                .withEphemeral(true)
                .withContent(result);
    }

    /**
     * Retrieves information for a given river race.
     *
     * @param currentRiverRace the current river race
     * @return a string containing information about the river race
     */
    private static String getInformationForRiverRace(CurrentRiverRace currentRiverRace) {
        StringBuilder resultBuilder = new StringBuilder();

        RiverRaceClan currentClan = currentRiverRace.getClan();

        // End of time for collection
        String collectionEndTime = currentRiverRace.getCollectionEndTime();

        // End of war
        String warEndTime = currentRiverRace.getWarEndTime();

        // Participants of own clan
        List<RiverRaceParticipant> participants = currentRiverRace.getClan().getParticipants();

        // Battles played by own clan
        Integer boatAttacks = getFieldWithDefault(participants, RiverRaceParticipant::getBoatAttacks, StandardUtils::addStatic, 0);

        // Battles won by own clan
        Integer decksUsed = getFieldWithDefault(participants, RiverRaceParticipant::getDecksUsed, StandardUtils::addStatic, 0);

        // All battles of own clan
        Integer decksUsedToday = getFieldWithDefault(participants, RiverRaceParticipant::getDecksUsedToday, StandardUtils::addStatic, 0);

        resultBuilder
                .append("So far there are altogether ")
                .append(decksUsed)
                .append(" decks used by ")
                .append(currentClan.getName()).append(", ")
                .append(boatAttacks)
                .append(" boat-attacks were played and ")
                .append(decksUsedToday)
                .append(" decks were used today. \n");

        return resultBuilder.toString();
    }

    /**
     * Creates an answer string for the interaction based on the current river race.
     *
     * @param interaction      the interaction
     * @param currentRiverRace the current river race
     * @return a string containing the answer
     */
    private static String createAnswer(Interaction interaction, CurrentRiverRace currentRiverRace) {
        StringBuilder sb = new StringBuilder();

        RiverRaceClan currentClan = currentRiverRace.getClan();

        // End of time for collection
        String collectionEndTime = currentRiverRace.getCollectionEndTime();

        // End of war
        String warEndTime = currentRiverRace.getWarEndTime();

        // Participants of own clan
        List<RiverRaceParticipant> participants = currentRiverRace.getClan().getParticipants();

        // Battles played by own clan
        Integer boatAttacks = getFieldWithDefault(participants, RiverRaceParticipant::getBoatAttacks, StandardUtils::addStatic, 0);

        // Battles won by own clan
        Integer decksUsed = getFieldWithDefault(participants, RiverRaceParticipant::getDecksUsed, StandardUtils::addStatic, 0);

        // All battles of own clan
        Integer decksUsedToday = getFieldWithDefault(participants, RiverRaceParticipant::getDecksUsedToday, StandardUtils::addStatic, 0);

        sb
                .append("So far there are altogether ")
                .append(decksUsed)
                .append(" decks used by ")
                .append(currentClan.getName()).append(", ")
                .append(boatAttacks)
                .append(" boat-attacks were played and ")
                .append(decksUsedToday)
                .append(" decks were used today. \n\n");

        // Create clan distinction between own clan and other clans
        List<RiverRaceClan> allClans = Collections.unmodifiableList(currentRiverRace.getClans());
        List<RiverRaceClan> ownClan = new ArrayList<>(allClans);
        ownClan.removeIf(clan -> !clan.getTag().equals(currentClan.getTag()));

        List<RiverRaceClan> otherClans = new ArrayList<>(allClans);
        otherClans.removeIf(clan -> clan.getTag().equals(currentClan.getTag()));

        // Sort by (river-race) fame
        otherClans.sort((c, d) -> {
            if (null == c.getFame() || null == d.getFame()) {
                return 0;
            }
            return Integer.compare(c.getFame(), d.getFame());
        });

        // List of other teams participants
        List<List<RiverRaceParticipant>> otherParticipants = otherClans.stream().map(RiverRaceClan::getParticipants).collect(Collectors.toList());

        // List of fame by other teams
        List<Integer> fame = getOtherWarParticipantsIntField(otherParticipants, RiverRaceParticipant::getFame);

        // List of boat attacks played by other teams
        List<Integer> boatAttacks2 = getOtherWarParticipantsIntField(otherParticipants, RiverRaceParticipant::getBoatAttacks);

        sb.append(" Other clans have following fame and boat-attacks: ");
        sb.append("```");
        for (int i = 0; i < fame.size(); i++) {
            sb
                    .append(otherClans.get(i).getName())
                    .append(" : ")
                    .append(fame.get(i))
                    .append(" points and ")
                    .append(boatAttacks2.get(i))
                    .append(" attacks (")
                    .append(otherParticipants.get(i).size())
                    .append(" participants so far) \n");
        }
        sb.append("```");

        return sb.toString();
    }

    /**
     * Retrieves a list of integer fields from other war participants.
     *
     * @param clans          the list of clans
     * @param extractionFunc the function to extract the field
     * @return a list of integer fields
     */
    private static List<Integer> getOtherWarParticipantsIntField(List<List<RiverRaceParticipant>> clans, Function<RiverRaceParticipant, Integer> extractionFunc) {
        return clans
                .stream().map(
                        participants ->
                                getFieldWithDefault(participants, extractionFunc, StandardUtils::addStatic, 0))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves an integer field from war participants.
     *
     * @param participants   the list of participants
     * @param extractionFunc the function to extract the field
     * @return the integer field
     */
    private static int getWarParticipantsIntField(List<RiverRaceParticipant> participants, Function<RiverRaceParticipant, Integer> extractionFunc) {
        return getFieldWithDefault(participants, extractionFunc, StandardUtils::addStatic, 0);
    }

    /**
     * Retrieves a field from war participants.
     *
     * @param participants   the list of participants
     * @param extractionFunc the function to extract the field
     * @param reducerFunc    the function to reduce the field
     * @param <T>            the type of the field
     * @return the field
     */
    private static <T> T getWarParticipantsField(List<RiverRaceParticipant> participants, Function<RiverRaceParticipant, T> extractionFunc, BiFunction<T, T, T> reducerFunc) {
        return getFieldWithDefault(participants, extractionFunc, reducerFunc, null);
    }

    /**
     * Retrieves a field with a default value from a list.
     *
     * @param incomingList   the list of items
     * @param extractionFunc the function to extract the field
     * @param reducerFunc    the function to reduce the field
     * @param defaultValue   the default value
     * @param <T>            the type of the field
     * @param <U>            the type of the items in the list
     * @return the field with the default value
     */
    public static <T, U> T getFieldWithDefault(List<U> incomingList, Function<U, T> extractionFunc, BiFunction<T, T, T> reducerFunc, T defaultValue) {
        if (null == incomingList) {
            return defaultValue;
        } else {
            return incomingList.stream().map(extractionFunc).reduce(reducerFunc::apply).orElse(defaultValue);
        }
    }
}