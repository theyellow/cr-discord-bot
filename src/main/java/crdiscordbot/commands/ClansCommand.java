package crdiscordbot.commands;

import crdiscordbot.ClashRoyalClanAPI;
import crdiscordbot.model.Clan;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class ClansCommand implements SlashCommand {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getName() {
        return "clans";
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

            /**if (clanTag.startsWith("#")) {
                clanTag = "%23" + clanTag.substring(1);
            } else {
                clanTag = "%23" + clanTag;
            }*/
            logger.info("Searched for clan tag: " + clanTag);
            Clan clan = royalRestClanAPI.getClan(clanTag);
            if (null != clan) {
                logger.info("Found it!");
                result = clan.getName() + " : " + clan.getDescription() + " : " + clan.getClanChestStatus() + " : " + clan.getClanScore();
            } else {
                logger.warn("No clan found with tag {}", clanTag);
            }
        }

        //Reply to the slash command, with the name the user supplied
        return  event.reply()
            .withEphemeral(true)
            .withContent(result);
    }
}
