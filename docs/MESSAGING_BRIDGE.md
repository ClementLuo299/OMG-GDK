# MessagingBridge Documentation

## Overview

The `MessagingBridge` is a thread-safe publish/subscribe (pub/sub) communication system that enables inter-module communication within the OMG-GDK framework. It provides a lightweight event system where game modules, the launcher, and other components can publish and subscribe to generic metadata messages without tight coupling.

## Architecture

### Design Principles

- **Decoupled Communication**: Components communicate through a central bridge without direct dependencies
- **Thread Safety**: Uses `CopyOnWriteArrayList` to ensure safe concurrent access
- **Lightweight**: Minimal overhead with simple message passing using `Map<String, Object>`
- **Fault Tolerant**: Exceptions in one consumer do not affect other consumers
- **Global State Management**: Provides a centralized callback for returning to the lobby

### Core Components

1. **Message Consumers**: Components that subscribe to receive messages
2. **Message Publishers**: Components that broadcast messages to all subscribers
3. **Subscription Handle**: Allows explicit unregistration of consumers
4. **Lobby Return Callback**: Global callback mechanism for navigation

## API Reference

### Consumer Management

#### `addConsumer(Consumer<Map<String, Object>> consumer)`

Registers a new consumer that will receive all published messages.

**Parameters:**
- `consumer`: A `Consumer<Map<String, Object>>` that processes incoming messages

**Returns:**
- `Subscription`: A handle that can be used to unsubscribe later, or `null` if the consumer is null

**Example:**
```java
Subscription subscription = MessagingBridge.addConsumer(msg -> {
    String function = (String) msg.get("function");
    if ("message".equals(function)) {
        // Process message
        System.out.println("Received: " + msg.get("text"));
    }
});
```

#### `removeConsumer(Consumer<Map<String, Object>> consumer)`

Removes a specific consumer from the message list. Useful when modules are unloaded.

**Parameters:**
- `consumer`: The consumer to remove

#### `Subscription.unsubscribe()`

Convenience method to unsubscribe using the subscription handle.

**Example:**
```java
Subscription subscription = MessagingBridge.addConsumer(msg -> { /* ... */ });
// Later...
subscription.unsubscribe();
```

### Message Publishing

#### `publish(Map<String, Object> message)`

Broadcasts a message to all registered consumers. Messages are delivered synchronously, and exceptions in one consumer are caught to prevent affecting others.

**Parameters:**
- `message`: A `Map<String, Object>` containing the message data

**Message Structure:**
Messages typically include:
- `function`: String indicating the message type (e.g., "message", "end", "close_server_simulator")
- Additional fields specific to the message type
- `timestamp`: ISO-8601 timestamp (optional but recommended)

**Example:**
```java
Map<String, Object> message = new HashMap<>();
message.put("function", "message");
message.put("from", "Player1");
message.put("text", "Hello, world!");
message.put("timestamp", Instant.now().toString());
MessagingBridge.publish(message);
```

### Lobby Return Functionality

#### `setLobbyReturnCallback(LobbyReturnCallback callback)`

Installs a callback that will be invoked when `returnToLobby()` is called. This allows game modules to trigger navigation back to the lobby without direct dependencies on the launcher.

**Parameters:**
- `callback`: A `LobbyReturnCallback` that implements `void returnToLobby()`

**Example:**
```java
MessagingBridge.setLobbyReturnCallback(() -> {
    // Switch scene to lobby
    primaryStage.setScene(lobbyScene);
});
```

#### `returnToLobby()`

Triggers the registered lobby return callback, if one is set. Safe to call even if no callback is registered.

**Example:**
```java
// In a game module
MessagingBridge.returnToLobby();
```

## Thread Safety

The `MessagingBridge` is designed to be thread-safe:

- **Consumer List**: Uses `CopyOnWriteArrayList`, which provides thread-safe iteration and modification
- **Exception Handling**: Exceptions in consumer callbacks are caught and logged, preventing one failing consumer from affecting others
- **Concurrent Access**: Multiple threads can safely publish messages and register/unregister consumers simultaneously

## Usage Patterns

### 1. Game Module Publishing Messages

Game modules publish messages to communicate with the launcher and server simulator:

```java
// Publish a chat message
Map<String, Object> chatMessage = new HashMap<>();
chatMessage.put("function", "message");
chatMessage.put("from", "Player1");
chatMessage.put("text", "Hello!");
chatMessage.put("timestamp", Instant.now().toString());
MessagingBridge.publish(chatMessage);

// Publish game end event
Map<String, Object> endMessage = new HashMap<>();
endMessage.put("function", "end");
endMessage.put("reason", "game_completed");
endMessage.put("timestamp", Instant.now().toString());
MessagingBridge.publish(endMessage);

// Request server simulator closure
Map<String, Object> closeMsg = new HashMap<>();
closeMsg.put("function", "close_server_simulator");
closeMsg.put("reason", "single_player_mode");
MessagingBridge.publish(closeMsg);
```

### 2. Launcher Subscribing to Messages

The launcher sets up consumers to receive messages from game modules:

```java
// Set up server simulator consumer
serverSimulatorSubscription = MessagingBridge.addConsumer(msg -> {
    try {
        String function = (String) msg.get("function");
        
        if ("close_server_simulator".equals(function)) {
            closeServerSimulator();
            return;
        }
        
        // Display message in server simulator
        ObjectMapper mapper = new ObjectMapper();
        String pretty = mapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(msg);
        serverSimulatorController.addReceivedMessageToDisplay(pretty);
    } catch (Exception e) {
        // Error handling
    }
});

// Set up transcript recording consumer
transcriptSubscription = MessagingBridge.addConsumer(msg -> {
    try {
        TranscriptRecorder.recordFromGame(msg);
    } catch (Exception ignored) {}
});
```

### 3. Managing Subscriptions

Always unsubscribe when components are no longer needed to prevent memory leaks:

```java
public class GameController {
    private MessagingBridge.Subscription subscription;
    
    public void initialize() {
        subscription = MessagingBridge.addConsumer(this::handleMessage);
    }
    
    public void cleanup() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }
    
    private void handleMessage(Map<String, Object> msg) {
        // Process message
    }
}
```

### 4. Returning to Lobby

Game modules can trigger a return to the lobby without direct dependencies:

```java
// In launcher initialization
MessagingBridge.setLobbyReturnCallback(() -> {
    returnToLobby();
});

// In game module
backButton.setOnAction(e -> {
    Map<String, Object> endMessage = new HashMap<>();
    endMessage.put("function", "end");
    endMessage.put("reason", "user_returned_to_lobby");
    endMessage.put("timestamp", Instant.now().toString());
    MessagingBridge.publish(endMessage);
    stopGame();
    MessagingBridge.returnToLobby();
});
```

## Common Message Types

### Message Functions

The framework uses several standard message function types:

- **`"message"`**: Chat or communication messages
  - Fields: `from`, `text`, `timestamp`
  
- **`"end"`**: Game end events
  - Fields: `reason`, `playerId` (optional), `timestamp`
  
- **`"close_server_simulator"`**: Request to close server simulator
  - Fields: `reason`
  
- **`"ack"`**: Acknowledgment messages (sent back to games)
  - Fields: `status`, `of`, `timestamp`

## Best Practices

1. **Always Unsubscribe**: Clean up subscriptions when components are destroyed to prevent memory leaks

2. **Use Consistent Message Format**: Include a `function` field and `timestamp` in all messages for consistency

3. **Handle Exceptions**: Wrap message processing in try-catch blocks to prevent errors from propagating

4. **Filter Messages**: Check the `function` field early in consumers to quickly ignore irrelevant messages

5. **Avoid Blocking Operations**: Keep message processing lightweight to avoid blocking the publishing thread

6. **Document Message Types**: When creating new message types, document their structure and purpose

7. **Thread Safety**: The bridge is thread-safe, but ensure your message processing code is also thread-safe if needed

## Real-World Examples

### Chatroom Module

The Chatroom module publishes messages for chat communication:

```java
// Publishing user messages
Map<String, Object> out = new HashMap<>();
out.put("function", "message");
out.put("from", sender);
out.put("text", text);
out.put("timestamp", Instant.now().toString());
MessagingBridge.publish(out);

// Publishing AI responses in single-player mode
Map<String, Object> aiMessage = new HashMap<>();
aiMessage.put("function", "message");
aiMessage.put("from", "AI");
aiMessage.put("text", aiResponse);
aiMessage.put("timestamp", Instant.now().toString());
MessagingBridge.publish(aiMessage);
```

### GDKViewModel (Launcher)

The launcher sets up multiple consumers for different purposes:

```java
// Server simulator consumer
serverSimulatorSubscription = MessagingBridge.addConsumer(msg -> {
    // Handle messages for server simulator display
});

// Transcript recording consumer
transcriptSubscription = MessagingBridge.addConsumer(msg -> {
    TranscriptRecorder.recordFromGame(msg);
});

// Lobby return callback
MessagingBridge.setLobbyReturnCallback(this::returnToLobby);
```

## Limitations and Considerations

1. **Synchronous Delivery**: Messages are delivered synchronously, so slow consumers can delay message delivery to other consumers

2. **No Message Filtering**: All consumers receive all messages; filtering must be done in the consumer code

3. **No Message Persistence**: Messages are not stored; only active consumers receive messages

4. **Global State**: The lobby return callback is global; only one callback can be registered at a time

5. **Memory Considerations**: Consumers hold references to the message data; ensure proper cleanup

## Future Enhancements

Potential improvements to consider:

- Message filtering/topics for more efficient message routing
- Asynchronous message delivery options
- Message queuing for offline consumers
- Message history/playback capabilities
- Type-safe message interfaces

## See Also

- `GameModule`: Interface for game modules that use MessagingBridge
- `GDKViewModel`: Launcher implementation using MessagingBridge
- Module implementations: `chatroom`, `tictactoe` for usage examples

