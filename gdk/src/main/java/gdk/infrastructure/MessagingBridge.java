package gdk.infrastructure;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Thread-safe publish/subscribe bridge for inter-module communication.
 *
 * Provides a lightweight event system where components can publish and subscribe
 * to generic metadata messages ({@code Map<String, Object>}), as well as a global
 * "return to lobby" callback.
 */
public final class MessagingBridge {

    // ==================== INTERNAL STATE ====================

    /** List of all registered message consumers. Thread-safe via CopyOnWriteArrayList. */
    private static final CopyOnWriteArrayList<Consumer<Map<String, Object>>> consumers =
            new CopyOnWriteArrayList<>();

    /** Private constructor — static utility only. */
    private MessagingBridge() {}

    // ==================== CONSUMER MANAGEMENT ====================

    /**
     * Register a new consumer that will receive all published messages.
     * Returns a {@link Subscription} handle that can be used to unregister later.
     *
     * @param consumer the message consumer to add
     * @return a Subscription object that can be used to remove the consumer later
     */
    public static Subscription addConsumer(Consumer<Map<String, Object>> consumer) {
        if (consumer != null) {
            consumers.add(consumer);
            return new Subscription(consumer);
        }
        return null;
    }

    /**
     * Remove a specific consumer from the message list.
     * Useful when modules are unloaded or no longer need to receive messages.
     *
     * @param consumer the consumer to remove
     */
    public static void removeConsumer(Consumer<Map<String, Object>> consumer) {
        if (consumer != null) {
            consumers.remove(consumer);
        }
    }

    /**
     * Represents a handle to a consumer subscription.
     * Allows explicit unregistration without tracking the original lambda.
     */
    public static final class Subscription {
        private final Consumer<Map<String, Object>> consumer;

        private Subscription(Consumer<Map<String, Object>> consumer) {
            this.consumer = consumer;
        }

        /** Unsubscribe this consumer from the message bridge. */
        public void unsubscribe() {
            consumers.remove(consumer);
        }
    }

    // ==================== MESSAGE PUBLISHING ====================

    /**
     * Broadcast a message to all registered consumers.
     *
     * @param message key-value map representing the message metadata
     */
    public static void publish(Map<String, Object> message) {
        for (Consumer<Map<String, Object>> c : consumers) {
            try {
                c.accept(message);
            } catch (Exception ignored) {
                // Avoid breaking other listeners if one fails
            }
        }
    }

    // ==================== LOBBY RETURN FUNCTIONALITY ====================

    private static LobbyReturnCallback lobbyReturnCallback = null;

    public interface LobbyReturnCallback {
        void returnToLobby();
    }

    /** Install a callback for returning to the lobby screen. */
    public static void setLobbyReturnCallback(LobbyReturnCallback callback) {
        lobbyReturnCallback = callback;
    }

    /** Trigger a return to the lobby, if a callback is set. */
    public static void returnToLobby() {
        if (lobbyReturnCallback != null) {
            try {
                lobbyReturnCallback.returnToLobby();
            } catch (Exception e) {
                System.err.println("Error returning to lobby: " + e.getMessage());
            }
        }
    }
}
