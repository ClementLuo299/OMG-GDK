package gdk.infrastructure;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple pub/sub bridge that lets infrastructure components share maps of metadata.
 * Also provides a best-effort hook for returning to the lobby from deeper flows.
 *
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 9, 2025
 * @since 1.0
 */
public final class MessagingBridge {
    private static final CopyOnWriteArrayList<Consumer<Map<String, Object>>> consumers = new CopyOnWriteArrayList<>();

    private MessagingBridge() {}

    /** Replace existing consumers with a single consumer (compatibility method). */
    public static void setConsumer(Consumer<Map<String, Object>> consumer) {
        consumers.clear();
        if (consumer != null) {
            consumers.add(consumer);
        }
    }

    /** Register an additional consumer to observe messages without overriding others. */
    public static void addConsumer(Consumer<Map<String, Object>> consumer) {
        if (consumer != null) {
            consumers.add(consumer);
        }
    }

    /**
     * Broadcast a message to every registered consumer.
     * Suppresses individual failures to avoid short-circuiting later consumers.
     */
    public static void publish(Map<String, Object> message) {
        for (Consumer<Map<String, Object>> c : consumers) {
            try {
                c.accept(message);
            } catch (Exception ignored) {
            }
        }
    }
    
    // ==================== LOBBY RETURN FUNCTIONALITY ====================
    
    private static LobbyReturnCallback lobbyReturnCallback = null;
    
    public interface LobbyReturnCallback {
        void returnToLobby();
    }
    
    /** Install a single callback that the UI/game can invoke to jump back to the lobby. */
    public static void setLobbyReturnCallback(LobbyReturnCallback callback) {
        lobbyReturnCallback = callback;
    }
    
    /**
     * Invoke the lobby return callback if one was provided.
     * Logs and swallows errors because the call is best-effort from infrastructure POV.
     */
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
