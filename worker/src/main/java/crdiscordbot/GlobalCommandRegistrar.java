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

@Component
public class GlobalCommandRegistrar implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalCommandRegistrar.class);

    private final RestClient discordRestClient;

    //Use the rest client provided by our Bean "discordRestClient"
    public GlobalCommandRegistrar(RestClient discordRestClient) {
        this.discordRestClient = discordRestClient;
    }

    //This method will run only once on each start up and is automatically called with Spring so blocking is okay.
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

        //Check if any  commands have been deleted or changed.
        checkForDeletedOrChangedCommands(applicationService, applicationId, discordCommands, commands);
    }

    private void checkForDeletedOrChangedCommands(ApplicationService applicationService, long applicationId, Map<String, ApplicationCommandData> discordCommands, Map<String, ApplicationCommandRequest> commands) {
        for (ApplicationCommandData discordCommand : discordCommands.values()) {
            long discordCommandId = Long.parseLong(discordCommand.id().asString());

            ApplicationCommandRequest command = commands.get(discordCommand.name());

            if (command == null) {
                //Removed command.json, delete global command
                applicationService.deleteGlobalApplicationCommand(applicationId, discordCommandId).block();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Deleted global command: {}", discordCommand.name());
                }
                continue; //Skip further processing on this command.
            }

            //Check if the command has been changed and needs to be updated.
            if (hasChanged(discordCommand, command)) {
                applicationService.modifyGlobalApplicationCommand(applicationId, discordCommandId, command).block();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Updated global command: {}", command.name());
                }
            }
        }
    }

    /**
     * Get our commands out of *.json-files. If there are new local commands, that aren't globally registered,
     * the local ones get globally registered.
     *
     * @param applicationId applicationId of application from which to get commands
     * @param discordCommands known local discord commands
     * @return registered commands (including new ones)
     */
    private Map<String, ApplicationCommandRequest> getCommandsAndRegisterForApplication(long applicationId, Map<String, ApplicationCommandData> discordCommands) throws IOException {
        final ApplicationService applicationService = discordRestClient.getApplicationService();
        //Get our commands json from resources as command data
        Map<String, ApplicationCommandRequest> commands = new HashMap<>();
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
        //Create an ObjectMapper that supported Discord4J classes
        final JacksonResources d4jMapper = JacksonResources.create();
        for (Resource resource : matcher.getResources("commands/*.json")) {
            ApplicationCommandRequest request = d4jMapper.getObjectMapper()
                .readValue(resource.getInputStream(), ApplicationCommandRequest.class);

            commands.put(request.name(), request);

            //Check if this is a new command that has not already been registered.
            if (!discordCommands.containsKey(request.name())) {
                //Not yet created with discord, lets do it now.
                applicationService.createGlobalApplicationCommand(applicationId, request).block();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Created global command: {}", request.name());
                }
            }
        }
        return commands;
    }

    private boolean hasChanged(ApplicationCommandData discordCommand, ApplicationCommandRequest command) {
        // Compare types
        if (!discordCommand.type().toOptional().orElse(1).equals(command.type().toOptional().orElse(1))) return true;

        //Check if description has changed.
        if (!discordCommand.description().equals(command.description().toOptional().orElse(""))) return true;

        //Check if default permissions have changed
        boolean discordCommandDefaultPermission = discordCommand.defaultPermission().toOptional().orElse(true);
        boolean commandDefaultPermission = command.defaultPermission().toOptional().orElse(true);

        if (discordCommandDefaultPermission != commandDefaultPermission) return true;

        //Check and return if options have changed.
        return !discordCommand.options().equals(command.options());
    }
}

