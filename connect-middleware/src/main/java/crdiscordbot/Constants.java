package crdiscordbot;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The Constants class holds various static configuration constants used throughout the application.
 */
public final class Constants {

    /**
     * The hostname for the local machine, retrieved from InetAddress.getLocalHost().getHostName().
     */
    private static String LOCALHOST;
    static {
        try {
            LOCALHOST = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOCALHOST = "0.0.0.0";
        }
    }

    /**
     * The hostname for the RSocket router server, retrieved from the environment variable RSOCKET_ROUTER_HOST.
     */
    private static final String RSOCKET_ROUTER_HOST = System.getenv("RSOCKET_ROUTER_HOST");

    /**
     * The hostname for the RSocket shard coordinator server, retrieved from the environment variable RSOCKET_SHARD_COORDINATOR_HOST.
     */
    private static final String RSOCKET_SHARD_COORDINATOR_HOST = System.getenv("RSOCKET_SHARD_COORDINATOR_HOST");

    /**
     * The hostname for the RSocket payload server, retrieved from the environment variable RSOCKET_PAYLOAD_HOST.
     */
    private static final String RSOCKET_PAYLOAD_HOST = System.getenv("RSOCKET_PAYLOAD_HOST");

    /**
     * The global router server host, defaults to LOCALHOST if RSOCKET_ROUTER_HOST is not set.
     */
    public static String GLOBAL_ROUTER_SERVER_HOST = (null == RSOCKET_ROUTER_HOST || RSOCKET_ROUTER_HOST.isEmpty()) ? LOCALHOST : RSOCKET_ROUTER_HOST;

    /**
     * The shard coordinator server host, defaults to LOCALHOST if RSOCKET_SHARD_COORDINATOR_HOST is not set.
     */
    public static String SHARD_COORDINATOR_SERVER_HOST = (null == RSOCKET_SHARD_COORDINATOR_HOST || RSOCKET_SHARD_COORDINATOR_HOST.isEmpty()) ? LOCALHOST : RSOCKET_SHARD_COORDINATOR_HOST;

    /**
     * The payload server host, defaults to LOCALHOST if RSOCKET_PAYLOAD_HOST is not set.
     */
    public static String PAYLOAD_SERVER_HOST = (null == RSOCKET_PAYLOAD_HOST || RSOCKET_PAYLOAD_HOST.isEmpty()) ? LOCALHOST : RSOCKET_PAYLOAD_HOST;

    /**
     * The port number for the global router server.
     */
    public static int GLOBAL_ROUTER_SERVER_PORT = 33331;

    /**
     * The port number for the shard coordinator server.
     */
    public static int SHARD_COORDINATOR_SERVER_PORT = 33332;

    /**
     * The port number for the payload server.
     */
    public static int PAYLOAD_SERVER_PORT = 33333;

    // redis variables
    /**
     * The URI for the Redis client, retrieved from the environment variable REDIS_CLIENT_URI.
     */
    private static final String CLIENT_URI = System.getenv("REDIS_CLIENT_URI");

    /**
     * The Redis client URI, defaults to "redis://localhost:6379" if REDIS_CLIENT_URI is not set.
     */
    public static String REDIS_CLIENT_URI = (null == CLIENT_URI || CLIENT_URI.isEmpty()) ? "redis://localhost:6379" : CLIENT_URI;

    /**
     * Private constructor to prevent instantiation.
     */
    private Constants() {
    }
}