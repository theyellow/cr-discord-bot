package crdiscordbot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

/**
 * A simple interface defining our slash command class contract.
 *
 * <p>This interface requires implementing classes to provide:</p>
 *
 * <ul>
 *   <li><code>getName()</code> method to provide the case-sensitive name of the command.</li>
 *   <li><code>handle()</code> method which will house all the logic for processing each command.</li>
 * </ul>
 */
public interface SlashCommand {

    /**
     * Gets the name of the slash command.
     *
     * @return the case-sensitive name of the command
     */
    String getName();

    /**
     * Handles the slash command interaction event.
     *
     * @param event the event representing the slash command interaction
     * @return a Mono that completes when the command handling is done
     */
    Mono<Void> handle(ChatInputInteractionEvent event);
}