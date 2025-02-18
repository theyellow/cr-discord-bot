package crdiscordbot;

import io.micronaut.runtime.Micronaut;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * The CrDiscordRSocketMiddleware class serves as the entry point for the Micronaut application.
 */
public class CrDiscordRSocketMiddleware {

    // Logger instance for logging events in this class
    private static final Logger log = Loggers.getLogger(CrDiscordRSocketMiddleware.class);

    /**
     * The main method which serves as the entry point for the application.
     * It builds and starts the Micronaut application.
     *
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        Micronaut.build(args)
                .eagerInitSingletons(true) // Eagerly initialize singletons
                .mainClass(CrDiscordRSocketMiddleware.class) // Set the main class
                .start(); // Start the Micronaut application
    }

}