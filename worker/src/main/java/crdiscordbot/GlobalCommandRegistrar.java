package crdiscordbot;

import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Component responsible for registering global commands with Discord on application startup.
 */
@Component
public class GlobalCommandRegistrar implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalCommandRegistrar.class);

    private final RestClient discordRestClient;

    /**
     * Constructor to initialize the GlobalCommandRegistrar with a RestClient.
     *
     * @param discordRestClient the RestClient to interact with Discord's API
     */
    public GlobalCommandRegistrar(RestClient discordRestClient) {
        this.discordRestClient = discordRestClient;
    }

    /**
     * This method is called once on application startup to register global commands with Discord.
     *
     * @param args the application arguments
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void run(ApplicationArguments args) throws IOException {
        // Convenience variables for the sake of easier to read code below.
        final ApplicationService applicationService = discordRestClient.getApplicationService();
        final Long applicationId = discordRestClient.getApplicationId().block();
        if (null == applicationId) {
            LOGGER.error("No application id found.");
            return;
        }
        //These are commands already registered with discord from previous runs of the bot.
        Map<String, ApplicationCommandData> discordCommands = applicationService
                .getGlobalApplicationCommands(applicationId)
                .collectMap(ApplicationCommandData::name)
                .block();
        if (null == discordCommands) {
            LOGGER.error("No discord commands found.");
            return;
        }
        Map<String, ApplicationCommandRequest> commands = getCommandsAndRegisterForApplication(applicationId, discordCommands);
        checkForDeletedOrChangedCommands(applicationService, applicationId, discordCommands, commands);
    }

    /**
     * Checks for deleted or changed commands and updates or deletes them accordingly.
     *
     * @param applicationService the application service to interact with Discord's API
     * @param applicationId the application ID
     * @param discordCommands the currently registered commands on Discord
     * @param commands the local commands to be registered
     */
    private void checkForDeletedOrChangedCommands(ApplicationService applicationService, long applicationId, Map<String, ApplicationCommandData> discordCommands, Map<String, ApplicationCommandRequest> commands) {
        for (ApplicationCommandData discordCommand : discordCommands.values()) {
            long discordCommandId = Long.parseLong(discordCommand.id().asString());
            ApplicationCommandRequest command = commands.get(discordCommand.name());
            if (command == null) {
                applicationService.deleteGlobalApplicationCommand(applicationId, discordCommandId).block();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Deleted global command: {}", discordCommand.name());
                }
                continue;
            }
            if (hasChanged(discordCommand, command)) {
                applicationService.modifyGlobalApplicationCommand(applicationId, discordCommandId, command).block();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Updated global command: {}", command.name());
                }
            }
        }
    }

    /**
     * Retrieves commands from JSON files and registers new commands with Discord.
     *
     * @param applicationId the application ID
     * @param discordCommands the currently registered commands on Discord
     * @return a map of command names to command requests
     * @throws IOException if an I/O error occurs
     */
    private Map<String, ApplicationCommandRequest> getCommandsAndRegisterForApplication(long applicationId, Map<String, ApplicationCommandData> discordCommands) throws IOException {
        final ApplicationService applicationService = discordRestClient.getApplicationService();
        Map<String, ApplicationCommandRequest> commands = new HashMap<>();
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
        final JacksonResources d4jMapper = JacksonResources.create();
        for (Resource resource : matcher.getResources("commands/*.json")) {
            ApplicationCommandRequest request = d4jMapper.getObjectMapper()
                    .readValue(resource.getInputStream(), ApplicationCommandRequest.class);
            commands.put(request.name(), request);
            if (!discordCommands.containsKey(request.name())) {
                applicationService.createGlobalApplicationCommand(applicationId, request).block();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Created global command: {}", request.name());
                }
            }
        }
        return commands;
    }

    /**
     * Checks if a command has changed by comparing its properties.
     *
     * @param discordCommand the command data from Discord
     * @param command the local command request
     * @return true if the command has changed, false otherwise
     */
    private boolean hasChanged(ApplicationCommandData discordCommand, ApplicationCommandRequest command) {
        if (!discordCommand.type().toOptional().orElse(1).equals(command.type().toOptional().orElse(1))) return true;
        if (!discordCommand.description().equals(command.description().toOptional().orElse(""))) return true;
        boolean discordCommandDefaultPermission = discordCommand.defaultPermission().toOptional().orElse(true);
        boolean commandDefaultPermission = command.defaultPermission().toOptional().orElse(true);
        if (discordCommandDefaultPermission != commandDefaultPermission) return true;
        return !discordCommand.options().equals(command.options());
    }
}