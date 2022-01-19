package crdiscordbot;

import crdiscordbot.listeners.SlashCommandListener;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ClashRoyalDiscordBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClashRoyalDiscordBot.class);

    public static void main(String[] args) {
        //Start spring application
        ApplicationContext springContext = new SpringApplicationBuilder(ClashRoyalDiscordBot.class)
            .build()
            .run(args);

        //Login
        DiscordClientBuilder.create(System.getenv("BOT_TOKEN")).build()
            .withGateway(gatewayClient -> {
                SlashCommandListener slashCommandListener = new SlashCommandListener(springContext);

                Mono<Void> onSlashCommandMono = gatewayClient
                    .on(ChatInputInteractionEvent.class, slashCommandListener::handle)
                    .then();

                return Mono.when(onSlashCommandMono);
            }).block();
    }

    @Bean(name = "discordRestClient")
    public RestClient discordRestClient() {
        return RestClient.create(System.getenv("BOT_TOKEN"));
    }

    @Bean(name = "royalRestClient")
    public RestTemplate royalRestClient() {
        String apiToken = System.getenv("API_TOKEN");
        RestTemplate restTemplate = new RestTemplateBuilder(rt-> rt.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().clear();
            request.getHeaders().add("Authorization", "Bearer " + apiToken);
            return execution.execute(request, body);
        })).build();
        LOGGER.info("REST-Engine for CR started...");
        return restTemplate;
    }

}
