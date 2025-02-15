## Communication

all is about communication in this distributed bot. Workers need to be coordinated and work, leaders have the control and get input-orders by discord. 
They have to coordinate everything. Different kind of data uses different paths. The following picture tries to illustrate this:

![](http://www.plantuml.com/plantuml/proxy?idx=0&src=https://raw.githubusercontent.com/theyellow/cr-discord-bot/main/communication.puml?)

Examples of communication follows:

### Data Types and Examples for Each Connection

1. **ClashRoyaleAPI --> Worker**:
   - **Data Type**: HTTP/REST requests and responses
   - **Examples**: Player and clan data, such as player statistics, clan information
   - **Example Code**:
     ```java
     // HTTP GET request to the Clash Royale API
     ResponseEntity<PlayerData> response = restTemplate.getForEntity("https://api.clashroyale.com/v1/players/{playerTag}", PlayerData.class, playerTag);
     PlayerData playerData = response.getBody();
     ```

2. **Worker <--> ConnectMiddleware**:
   - **Data Type**: RSocket messages and HTTP/REST requests
   - **Examples**: Tasks, status updates, results
   - **Example Code (RSocket)**:
     ```java
     // RSocket Request-Response
     Mono<Payload> response = rSocket.requestResponse(DefaultPayload.create("Task"));
     response.subscribe(result -> {
         System.out.println("Result from Worker: " + result.getDataUtf8());
     });
     ```
   - **Example Code (HTTP/REST)**:
     ```java
     // HTTP POST request to the middleware
     ResponseEntity<String> response = restTemplate.postForEntity("http://middleware/api/task", task, String.class);
     ```

3. **Worker --> RabbitMQ**:
   - **Data Type**: Messaging
   - **Examples**: Tasks, results
   - **Example Code**:
     ```java
     // Send message to RabbitMQ
     rabbitTemplate.convertAndSend("taskQueue", task);
     ```

4. **Worker --> Redis**:
   - **Data Type**: Read accesses
   - **Examples**: Cached API responses, temporary data
   - **Example Code**:
     ```java
     // Read data from Redis
     String cachedData = redisTemplate.opsForValue().get("playerData:" + playerTag);
     ```

5. **ConnectMiddleware <--> Leader**:
   - **Data Type**: RSocket messages and HTTP/REST requests
   - **Examples**: Tasks, status updates, results
   - **Example Code (RSocket)**:
     ```java
     // RSocket Fire-and-Forget
     rSocket.fireAndForget(DefaultPayload.create("Notification")).subscribe();
     ```
   - **Example Code (HTTP/REST)**:
     ```java
     // HTTP GET request to the leader
     ResponseEntity<Status> response = restTemplate.getForEntity("http://leader/api/status", Status.class);
     ```

6. **ConnectMiddleware --> Connect**:
   - **Data Type**: Library calls
   - **Examples**: Using the Connect library for communication with the Discord API
   - **Example Code**:
     ```java
     // Using the Connect library
     connect.sendMessage(channelId, "Message to Discord");
     ```

7. **Leader --> RabbitMQ**:
   - **Data Type**: Messaging
   - **Examples**: Tasks, results
   - **Example Code**:
     ```java
     // Send message to RabbitMQ
     rabbitTemplate.convertAndSend("taskQueue", task);
     ```

8. **Leader --> Redis**:
   - **Data Type**: Caching
   - **Examples**: Cached API responses, temporary data
   - **Example Code**:
     ```java
     // Store data in Redis
     redisTemplate.opsForValue().set("taskResult:" + taskId, result);
     ```

9. **Connect <--> DiscordAPI**:
   - **Data Type**: WebSocket messages
   - **Examples**: Receiving and sending messages, events
   - **Example Code**:
     ```java
     // Send WebSocket message
     discordClient.sendMessage(channelId, "Message to Discord");
     ```

10. **RabbitMQ --> Queue**:
    - **Data Type**: Messaging
    - **Examples**: Tasks, results
    - **Example Code**:
      ```java
      // Put message in the queue
      rabbitTemplate.convertAndSend("taskQueue", task);
      ```
