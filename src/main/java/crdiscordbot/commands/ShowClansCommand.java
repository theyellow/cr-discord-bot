package crdiscordbot.commands;

import crdiscordbot.ClashRoyalClanAPI;
import crdiscordbot.model.ClanSearchResult;
import crdiscordbot.model.ClanSearchResultClan;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ShowClansCommand implements SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShowClansCommand.class);

    @Override
    public String getName() {
        return "showclans";
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
        Optional<ApplicationCommandInteractionOption> clanIdOpt = event.getOption("name");
        String name = null;
        if (clanIdOpt.isPresent()) {
            name = clanIdOpt
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString)
                    .orElse("Test"); //This is warning us that we didn't check if its present, we can ignore this on required options
        } else {
            name = "Test";
        }


        String result;
        if (name.isEmpty() || "Test".equals(name)) {
            result = "No name given, either set CLAN_ID system-variable for bot or use a parameter name.";
        } else {

            /**if (name.startsWith("#")) {
             name = "%23" + name.substring(1);
             } else {
             name = "%23" + name;
             }*/
            LOGGER.info("Clan name to search for: {}", name);
            ClanSearchResult clans = royalRestClanAPI.getAllClansForName(name);
            result = clans.getItems().
                    stream().
                    sorted((clan1, clan2)->
                            compareNullSafe(clan2.getClanScore(),clan1.getClanScore())).
                    map(ShowClansCommand::createClanText).
                    collect(Collectors.joining());
        }

        if (result.length() > 1999) {
            result = result.substring(0,1999) + "...";
        }

        //Reply to the slash command, with the name the user supplied
        return  event.reply()
            .withEphemeral(true)
            .withContent(result);
    }

    private static String createClanText(ClanSearchResultClan clan) {
        return MessageFormat.format("{0}({1}) : {2} /\\  ", clan.getName(), clan.getClanScore(), clan.getTag());
    }

    private static int compareNullSafe(Integer clanScore1, Integer clanScore2) {
        if (null == clanScore1) {
            if (null == clanScore2) {
                return 0;
            } else {
                return -1;
            }
        } else if (null == clanScore2) {
            return 1;
        } else {
            return Integer.compare(clanScore1, clanScore2);
        }
    }
}
