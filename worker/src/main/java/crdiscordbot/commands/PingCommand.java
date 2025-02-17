package crdiscordbot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * A command that responds to the "ping" command with "Pong!".
 */
@Component
public class PingCommand implements SlashCommand {

    /**
     * Returns the name of the command.
     *
     * @return the name of the command, which is "ping".
     */
    @Override
    public String getName() {
        return "ping";
    }

    /**
     * Handles the "ping" command by replying with "Pong!".
     *
     * @param event the event representing the command interaction.
     * @return a Mono that completes when the reply is sent.
     */
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        // We reply to the command with "Pong!" and make sure it is ephemeral (only the command user can see it)
        return event.reply()
                .withEphemeral(true)
                .withContent("Pong!");
    }
}