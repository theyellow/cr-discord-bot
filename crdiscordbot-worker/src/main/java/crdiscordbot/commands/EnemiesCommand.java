package crdiscordbot.commands;

import crdiscordbot.ClashRoyalClanAPI;
import crdiscordbot.model.RiverRace;
import crdiscordbot.model.RiverRaceClan;
import crdiscordbot.model.RiverRaceClanParticipant;
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

@Component
public class EnemiesCommand implements SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnemiesCommand.class);

    @Override
    public String getName() {
        return "enemies";
    }

    @Autowired
    private ClashRoyalClanAPI royalRestClanAPI;

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
            RiverRace currentRiverRace = royalRestClanAPI.getCurrentRiverRace(clanTag);
            result = getResult(clanTag, result, currentRiverRace);
        }
        //Reply to the slash command, with the name the user supplied
        return  event.reply()
            .withEphemeral(true)
            .withContent(result);
    }

    private String getResult(String clanTag, String result, RiverRace currentRiverRace) {
        if (null != currentRiverRace) {
            LOGGER.info("Found river-race, ends {}", currentRiverRace.getWarEndTime());
            result = createAnswer(currentRiverRace);
        } else {
            LOGGER.warn("No currentRiverRace found with tag {}", clanTag);
        }
        return result;
    }

    private String createAnswer(RiverRace currentRiverRace) {
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

    private static String createEnemyString(RiverRaceClan clan) {
        String name = clan.getName();
        if (!StringUtils.hasLength(name)) {
            name = "<noname>";
        }
        int clanFame = Optional.ofNullable(clan.getFame()).orElse(0);
        int periodPoints = Optional.ofNullable(clan.getPeriodPoints()).orElse(0);
        List<RiverRaceClanParticipant> participants = clan.getParticipants();
        int nrOfParticipants = 0;
        if (null != participants) {
            nrOfParticipants = participants.size();
        }
        return MessageFormat.format(" #{0} {1}, {2} ({3} participants); ", clanFame, name, periodPoints, nrOfParticipants);
    }
}
