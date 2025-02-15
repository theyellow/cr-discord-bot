## Architecture of the CR Discord Bot, including Maven Subprojects, Dependencies, Communication, and Technologies Used

### Architecture and Components

#### Maven Subprojects
1. **common**: Contains shared utility classes and configurations used by other subprojects.
2. **connect**: A library that enables connection to the Discord API and serves as the communication hub between `leader` and `worker`. This library was forked and adapted from the original Discord4J/connect repository.
3. **connect-middleware**: A middleware service that coordinates communication between the `leader` and the `worker`. Implemented with Micronaut.
4. **leader**: The main application that coordinates and manages the `worker`. Implemented with Spring Boot.
5. **worker**: Executes the actual tasks and commands delegated by the `leader`. Implemented with Spring Boot.

### Dependencies and Communication
- **common**: Used by all other subprojects to provide shared functions and configurations.
- **connect**: Included as a Maven dependency in `leader` and `worker` to enable connection to the Discord API and communication between `leader` and `worker`.
- **connect-middleware**: Mediates communication between `leader` and `worker`. The `leader` sends tasks to the middleware, which then forwards them to the appropriate `worker`.
- **leader**: Manages the `worker` and communicates with the `connect-middleware` to delegate tasks and collect results.
- **worker**: Executes tasks delegated by the `leader` and sends results back via the `connect-middleware`.

### Communication Protocols
- **RSocket**: Used for bidirectional communication between `leader` and `worker`. This enables efficient and reactive data transfer, which is particularly useful for real-time task processing and feedback. RSocket supports various interaction models such as request-response, fire-and-forget, and channel, allowing flexible communication.
- **RabbitMQ**: Serves as a message broker to relay messages between `leader` and `worker`. This ensures reliable message delivery. RabbitMQ is mainly used for task distribution and result collection.
- **HTTP/REST**: Communication between `leader` and `connect-middleware` as well as between `worker` and `connect-middleware` occurs over HTTP/REST. This is used for service management and coordination.
- **WebSocket**: The `connect` library uses WebSocket for bidirectional communication with the Discord API. This enables real-time interaction with Discord.

### Redis Usage
- **Caching**: Redis is used to cache API requests and other temporary data to improve performance and reduce the number of API calls.

### Integration of External APIs
- **Discord API**: The `connect` library uses WebSocket for bidirectional communication with the Discord API to receive events and execute commands.
- **Clash Royale API**: The bot interacts with the Clash Royale API over HTTP/REST to retrieve data on players and clans and provide this information in Discord.

### Management of Workers by the Leader
- **Task Distribution**: The `leader` manages the `worker` by distributing tasks via RabbitMQ as a message broker. This ensures tasks are reliably delivered and executed by the `worker`.
- **Direct Communication**: In addition to using RabbitMQ, the `leader` can also use RSocket for direct, bidirectional communication with the `worker`. This enables efficient and reactive data transfer, which is particularly useful for real-time task processing and feedback.
- **Middleware**: The `connect-middleware` coordinates communication between `leader` and `worker` and ensures tasks are correctly distributed and results collected.

### Example of Using RSocket in the Project

#### Request-Response
```java
// Leader sends a task to the worker and expects a response
RSocket rSocket = RSocketFactory.connect()
    .transport(TcpClientTransport.create("localhost", 7000))
    .start()
    .block();

Payload payload = DefaultPayload.create("Task");
Mono<Payload> response = rSocket.requestResponse(payload);

response.subscribe(result -> {
    System.out.println("Result from Worker: " + result.getDataUtf8());
});
```

#### Fire-and-Forget
```java
// Leader sends a notification to the worker without expecting a response
RSocket rSocket = RSocketFactory.connect()
    .transport(TcpClientTransport.create("localhost", 7000))
    .start()
    .block();

Payload payload = DefaultPayload.create("Notification");
rSocket.fireAndForget(payload).subscribe();
```

#### Channel
```java
// Leader and worker communicate over a bidirectional channel
RSocket rSocket = RSocketFactory.connect()
    .transport(TcpClientTransport.create("localhost", 7000))
    .start()
    .block();

Flux<Payload> channel = Flux.just(DefaultPayload.create("Message 1"), DefaultPayload.create("Message 2"));
rSocket.requestChannel(channel)
    .subscribe(response -> {
        System.out.println("Response from Worker: " + response.getDataUtf8());
    });
```

### Deployment
The CR Discord Bot is deployed using Docker and Kubernetes. Here are the steps in detail:

1. **Dockerfiles**: Each Maven subproject has its own Dockerfile to enable the creation of a Docker image.
2. **GitHub Actions/Workflows**: Automated workflows create Docker images for each merge request and upload them to the GitHub Docker Registry.
3. **Kubernetes**: A folder with Kubernetes YAML definitions enables deployment on a Kubernetes cluster (e.g., microk8s). These definitions include deployments, services, ConfigMaps, and secrets.

### Technologies and Frameworks Used
- **Java**: Programming language for implementing the bot.
- **Maven**: Build tool for managing subprojects and dependencies.
- **Spring Boot**: Framework for implementing the `leader` and `worker` applications.
- **Micronaut**: Framework for implementing the `connect-middleware`.
- **Discord4J**: Library for interacting with the Discord API, included via the `connect` subproject.
- **Docker**: Containerization of individual components.
- **Kubernetes**: Orchestration and management of containers.
- **GitHub Actions**: Automation of the CI/CD pipeline.
- **Redis**: In-memory database and cache to support communication and improve performance.

### Summary
RabbitMQ is used as a message broker to support reliable message delivery and complex routing patterns. RSocket is used for bidirectional, reactive communication between `leader` and `worker`. Both technologies complement each other and together provide a robust and flexible communication infrastructure for the CR Discord Bot.
