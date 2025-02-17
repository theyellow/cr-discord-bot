package crdiscordbot.listeners;

import crdiscordbot.commands.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Listener for handling slash commands in a Discord bot.
 */
public class SlashCommandListener {
    private final Collection<SlashCommand> commands;

    /**
     * Constructs a new SlashCommandListener.
     *
     * @param applicationContext the Spring application context used to retrieve SlashCommand beans
     */
    public SlashCommandListener(ApplicationContext applicationContext) {
        // Get all classes that implement our SlashCommand interface and are annotated with @Component
        commands = applicationContext.getBeansOfType(SlashCommand.class).values();
    }

    /**
     *
     * @param event the ChatInputInteractionEvent to handle
     * @return a Mono that completes when the command handling is done
     */
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        // Convert our list to a flux that we can iterate through
        return Flux.fromIterable(commands)
            // Filter out all commands that don't match the name this event is for
            .filter(command -> command.getName().equals(event.getCommandName()))
            // Get the first (and only) item in the flux that matches our filter
            .next()
            // Have our command class handle all logic related to its specific command.
            .flatMap(command -> command.handle(event));
    }
}