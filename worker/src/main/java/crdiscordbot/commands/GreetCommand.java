package crdiscordbot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * A Spring component that handles the "greet" slash command.
 */
@Component
public class GreetCommand implements SlashCommand {

    /**
     * Returns the name of the slash command.
     *
     * @return the name of the command, which is "greet".
     */
    @Override
    public String getName() {
        return "greet";
    }

    /**
     * Handles the slash command interaction event.
     *
     * @param event the ChatInputInteractionEvent that contains the interaction details.
     * @return a Mono that completes when the reply is sent.
     */
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        // Extract the "name" option from the event, defaulting to "Mr. X" if not provided
        String name = event.getOption("name")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("Mr. X");

        // Reply to the slash command with a greeting message
        return event.reply()
                .withEphemeral(true)
                .withContent("Hello, " + name);
    }
}