package crdiscordbot.commands;

import crdiscordbot.ClashRoyalClanAPI;
import crdiscordbot.model.CurrentRiverRace;
import crdiscordbot.model.RiverRaceClan;
import crdiscordbot.model.RiverRaceParticipant;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Component that handles the "enemies" slash command.
 */
@Component
public class EnemiesCommand implements SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnemiesCommand.class);

    /**
     * Returns the name of the command ("enemies").
     *
     * @return the name of the command
     */
    @Override
    public String getName() {
        return "enemies";
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
        Optional<ApplicationCommandInteractionOption> clanIdOpt = event.getOption("clanid");
        String clanTag;
        if (clanIdOpt.isPresent()) {
            clanTag = clanIdOpt
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString).orElse(System.getenv("CLAN_ID"));
        } else {
            LOGGER.info("ClanID not present, using env-variable");
            clanTag = System.getenv("CLAN_ID");
        }

        String result = "No result";
        if (clanTag.isEmpty()) {
            result = "No clan given, either set CLAN_ID system-variable for bot or use a parameter clanTag.";
        } else {
            LOGGER.info("Searched for currentRiverRace of clan: {}", clanTag);
            CurrentRiverRace currentRiverRace = royalRestClanAPI.getCurrentRiverRace(clanTag);
            result = getResult(clanTag, result, currentRiverRace);
        }
        // Reply to the slash command, with the name the user supplied
        return event.reply()
                .withEphemeral(true)
                .withContent(result);
    }

    /**
     * Gets the result string for the given clan tag and current river race.
     *
     * @param clanTag the clan tag
     * @param result the initial result string
     * @param currentRiverRace the current river race
     * @return the result string
     */
    private String getResult(String clanTag, String result, CurrentRiverRace currentRiverRace) {
        if (null != currentRiverRace) {
            LOGGER.info("Found river-race, ends {}", currentRiverRace.getWarEndTime());
            result = createAnswer(currentRiverRace);
        } else {
            LOGGER.warn("No currentRiverRace found with tag {}", clanTag);
        }
        return result;
    }

    /**
     * Creates the answer string for the given current river race.
     *
     * @param currentRiverRace the current river race
     * @return the answer string
     */
    private String createAnswer(CurrentRiverRace currentRiverRace) {
        String result;
        List<RiverRaceClan> clans = currentRiverRace.getClans();
        RiverRaceClan currentRiverRaceClan = currentRiverRace.getClan();
        String clanTagFromResult = currentRiverRaceClan.getTag();
        String otherClans = clans.stream().
                sorted((clan2, clan1) -> clan1.getClanScore().compareTo(clan2.getClanScore())).
                filter(clan -> !clanTagFromResult.equals(clan.getTag())).
                map(EnemiesCommand::createEnemyString).
                collect(Collectors.joining());
        // delete last ";"
        otherClans = otherClans.substring(0, otherClans.length() - 2);
        result = MessageFormat.format("#{0} {1}, {2} ({3} participants) against {4}",
                currentRiverRaceClan.getFame(),
                currentRiverRaceClan.getName(),
                currentRiverRaceClan.getPeriodPoints(),
                currentRiverRaceClan.getParticipants().size(),
                otherClans);
        return result;
    }

    /**
     * Creates a string representation of an enemy clan.
     *
     * @param clan the enemy clan
     * @return the string representation of the enemy clan
     */
    private static String createEnemyString(RiverRaceClan clan) {
        String name = clan.getName();
        if (!StringUtils.hasLength(name)) {
            name = "<noname>";
        }
        int clanFame = Optional.ofNullable(clan.getFame()).orElse(0);
        int periodPoints = Optional.ofNullable(clan.getPeriodPoints()).orElse(0);
        List<RiverRaceParticipant> participants = clan.getParticipants();
        int nrOfParticipants = 0;
        if (null != participants) {
            nrOfParticipants = participants.size();
        }
        return MessageFormat.format(" #{0} {1}, {2} ({3} participants); ", clanFame, name, periodPoints, nrOfParticipants);
    }
}