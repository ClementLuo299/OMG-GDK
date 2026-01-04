package gdk.internal;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Thread-safe publish/subscribe bridge for inter-module communication.
 *
 * Provides a lightweight event system where components can publish and subscribe
 * to generic extract_metadata messages ({@code Map<String, Object>}), as well as a global
 * "return to lobby" callback.
 *
 * @authors Clement Luo
 * @date August 8, 2025
 * @edited November 9, 2025
 * @since Beta 1.0
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
        if (consumer == null) {
            Logging.warning("MessagingBridge: Attempted to register null consumer");
            return null;
        }

        if (consumers.contains(consumer)) {
            Logging.warning("MessagingBridge: Duplicate consumer registration attempted");
            return new Subscription(consumer, false);
        }

        consumers.add(consumer);
        Logging.info("MessagingBridge: Consumer added. Total consumers=" + consumers.size());
        return new Subscription(consumer, true);
    }

    /**
     * Remove a specific consumer from the message list.
     * Useful when modules are unloaded or no longer need to receive messages.
     *
     * @param consumer the consumer to remove
     */
    public static void removeConsumer(Consumer<Map<String, Object>> consumer) {
        if (consumer == null) {
            Logging.warning("MessagingBridge: Attempted to remove null consumer");
            return;
        }

        if (consumers.remove(consumer)) {
            Logging.info("MessagingBridge: Consumer removed. Total consumers=" + consumers.size());
        } else {
            Logging.warning("MessagingBridge: Attempted to remove non-registered consumer");
        }
    }

    /**
     * Represents a handle to a consumer subscription.
     * Allows explicit unregistration without tracking the original lambda.
     */
    public static final class Subscription {
        private final Consumer<Map<String, Object>> consumer;
        private volatile boolean isActive;

        private Subscription(Consumer<Map<String, Object>> consumer, boolean active) {
            this.consumer = consumer;
            this.isActive = active;
        }

        /** Unsubscribe this consumer from the message bridge. */
        public void unsubscribe() {
            if (!isActive || consumer == null) {
                return;
            }
            consumers.remove(consumer);
            isActive = false;
        }

        /** Check if this subscription is still active. */
        public boolean isActive() {
            return isActive;
        }
    }

    // ==================== MESSAGE PUBLISHING ====================

    /**
     * Broadcast a message to all registered consumers.
     *
     * @param message key-value map representing the message extract_metadata
     */
    public static void publish(Map<String, Object> message) {
        if (message == null) {
            Logging.warning("MessagingBridge: Attempted to publish null message");
            return;
        }

        if (consumers.isEmpty()) {
            Logging.info("MessagingBridge: No consumers registered; message dropped");
            return;
        }

        for (Consumer<Map<String, Object>> c : consumers) {
            try {
                c.accept(message);
            } catch (Exception e) {
                Logging.error("MessagingBridge: Error in consumer during publish", e);
            }
        }
    }

    /**
     * Helper to validate required message fields for consistency.
     *
     * @param message message Map
     * @param requiredFields required keys
     * @return true if all required fields are present
     */
    public static boolean validateMessage(Map<String, Object> message, String... requiredFields) {
        if (message == null) {
            Logging.warning("MessagingBridge: validateMessage called with null");
            return false;
        }

        if (requiredFields == null || requiredFields.length == 0) {
            return true;
        }

        for (String field : requiredFields) {
            if (!message.containsKey(field)) {
                Logging.warning("MessagingBridge: Message missing required field '" + field + "'");
                return false;
            }
        }
        return true;
    }

    /**
     * Provides number of active consumers (useful for diagnostics).
     */
    public static int getConsumerCount() {
        return consumers.size();
    }

    // ==================== LOBBY RETURN FUNCTIONALITY ====================

    private static LobbyReturnCallback lobbyReturnCallback = null;

    public interface LobbyReturnCallback {
        void returnToLobby();
    }

    /** Install a callback for returning to the lobby screen. */
    public static void setLobbyReturnCallback(LobbyReturnCallback callback) {
        lobbyReturnCallback = callback;
        Logging.info("MessagingBridge: Lobby return callback " + (callback != null ? "registered" : "cleared"));
    }

    /** Trigger a return to the lobby, if a callback is set. */
    public static void returnToLobby() {
        if (lobbyReturnCallback != null) {
            try {
                lobbyReturnCallback.returnToLobby();
            } catch (Exception e) {
                Logging.error("MessagingBridge: Error returning to lobby", e);
            }
        } else {
            Logging.warning("MessagingBridge: returnToLobby() called but no callback is set");
        }
    }
}
