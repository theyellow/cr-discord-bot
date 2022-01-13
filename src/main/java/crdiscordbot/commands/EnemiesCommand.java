package crdiscordbot.commands;

import crdiscordbot.ClashRoyalClanAPI;
import crdiscordbot.model.Clan;
import crdiscordbot.model.CurrentRiverRace;
import crdiscordbot.model.RiverClan;
import crdiscordbot.model.RiverParticipant;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        /*
        Since slash command options are optional according to discord, we will wrap it into the following function
        that gets the value of our option as a String without chaining several .get() on all the optional values

        In this case, there is no fear it will return empty/null as this is marked "required: true" in our json.
         */
        Optional<ApplicationCommandInteractionOption> clanIdOpt = event.getOption("clanid");
        String clanTag = null;
        if (clanIdOpt.isPresent() && !clanIdOpt.isEmpty()) {
            clanTag = clanIdOpt
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString)
                    .get(); //This is warning us that we didn't check if its present, we can ignore this on required options
        } else {
            clanTag = System.getenv("CLAN_ID");
        }


        String result = "No result";
        if (clanTag.isEmpty()) {
            result = "No clan given, either set CLAN_ID system-variable for bot or use a parameter clanTag.";
        } else {
            LOGGER.info("Searched for currentRiverRace of clan: " + clanTag);
            CurrentRiverRace currentRiverRace = royalRestClanAPI.getCurrentRiverRace(clanTag);
            if (null != currentRiverRace) {
                LOGGER.info("Found river-race, ends {}", currentRiverRace.getWarEndTime());
                List<RiverClan> clans = currentRiverRace.getClans();
                RiverClan currentRiverRaceClan = currentRiverRace.getClan();
                String clanTagFromResult = currentRiverRaceClan.getTag();
                String otherClans = clans.stream().sorted((clan2, clan1) -> clan1.getClanScore().compareTo(clan2.getClanScore())).map(clan ->
                {
                    if (clanTagFromResult.equals(clan.getTag())) {
                        return " ";
                    }
                    // output per enemy:
                    String name = clan.getName();
                    Integer clanFame = clan.getFame();
                    Integer periodPoints = clan.getPeriodPoints();
                    List<RiverParticipant> participants = clan.getParticipants();
                    String participantsString = "";
                    if (null != participants) {
                        participantsString = participants.stream().map(participant -> participant.getName() + ",").collect(Collectors.joining());
                        // delete last ","
                        participantsString = participantsString.substring(0,participantsString.length() - 1);
                    }
                    int nrOfParticipants = participants.size();
                    //String enemies = name + " #" + clanFame + ", " + periodPoints + ", (" + participantsString + ");";
                    String enemies = " #" + clanFame + " " + name + ", " + periodPoints + " (" + nrOfParticipants + " participants); ";
                    return enemies;
                }
                ).collect(Collectors.joining());
                // delete last ";"
                otherClans = otherClans.substring(0, otherClans.length() - 2);
                result = "#" + currentRiverRaceClan.getFame() + " " + currentRiverRaceClan.getName() + ", " + + currentRiverRaceClan.getPeriodPoints() +" (" + currentRiverRaceClan.getParticipants().size() + " participants) against " + otherClans;
            } else {
                LOGGER.warn("No currentRiverRace found with tag {}", clanTag);
            }
        }

        //Reply to the slash command, with the name the user supplied
        return  event.reply()
            .withEphemeral(true)
            .withContent(result);
    }
}