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

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Command to show clans based on a given name.
 */
@Component
public class ShowClansCommand implements SlashCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShowClansCommand.class);

    /**
     * Returns the name of the command ("showclans").
     * @return the name of the command.
     */
     @Override
     public String getName() {
     return "showclans";
     }

     @Autowired
     private ClashRoyalClanAPI royalRestClanAPI;

     /**
      * Handles the slash command interaction event.
      *
      * @param event the chat input interaction event.
     * @return a Mono that completes when the reply is sent.
     */
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        // Get the value of the "name" option as a String.
        Optional<ApplicationCommandInteractionOption> clanIdOpt = event.getOption("name");
        String name;
        if (clanIdOpt.isPresent()) {
            name = clanIdOpt
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString)
                    .orElse("Test"); // Default value if not present
        } else {
            name = "Test";
        }

        String result;
        if (name.isEmpty() || "Test".equals(name)) {
            result = "No name given, either set CLAN_ID system-variable for bot or use a parameter name.";
        } else {
            LOGGER.info("Clan name to search for: {}", name);
            List<Clan> clans = royalRestClanAPI.getAllClansForName(name);
            result = clans
                    .stream()
                    .sorted((clan1, clan2) ->
                            compareNullSafe(clan2.getClanScore(), clan1.getClanScore()))
                    .map(ShowClansCommand::createClanText)
                    .collect(Collectors.joining());
        }

        if (result.length() > 1999) {
            result = result.substring(0, 1999) + "...";
        }

        // Reply to the slash command with the result.
        return event.reply()
                .withEphemeral(true)
                .withContent(result);
    }

    /**
     * Creates a formatted string for a clan.
     *
     * @param clan the clan to format.
     * @return the formatted clan string.
     */
    private static String createClanText(Clan clan) {
        return MessageFormat.format("{0}({1}) : {2} /\\  ", clan.getName(), clan.getClanScore(), clan.getTag());
    }

    /**
     * Compares two Integer values, handling null values safely.
     *
     * @param clanScore1 the first clan score.
     * @param clanScore2 the second clan score.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
     */
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