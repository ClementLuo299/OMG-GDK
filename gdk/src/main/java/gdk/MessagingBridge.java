package gdk;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 9, 2025
 * @since 1.0
 */
public final class MessagingBridge {
    private static final CopyOnWriteArrayList<Consumer<Map<String, Object>>> consumers = new CopyOnWriteArrayList<>();

    private MessagingBridge() {}

    /**
     * Replace existing consumers with a single consumer (compatibility method).
     */
    public static void setConsumer(Consumer<Map<String, Object>> consumer) {
        consumers.clear();
        if (consumer != null) {
            consumers.add(consumer);
        }
    }

    /**
     * Add a consumer to receive published messages.
     */
    public static void addConsumer(Consumer<Map<String, Object>> consumer) {
        if (consumer != null) {
            consumers.add(consumer);
        }
    }

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
    
    public static void setLobbyReturnCallback(LobbyReturnCallback callback) {
        lobbyReturnCallback = callback;
    }
    
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