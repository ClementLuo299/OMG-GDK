package gdk.infrastructure;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * MessagingBridge is a lightweight, thread-safe publish/subscribe (pub/sub) system
 * designed for communication between loosely coupled components in the GDK infrastructure.
 *
 * <p>It allows any part of the system—such as the UI, networking, or game modules—
 * to broadcast simple metadata messages represented as {@code Map<String, Object>}.
 * Other parts of the system can register "consumers" that listen for these messages
 * and respond accordingly.</p>
 *
 * <p>Additionally, the bridge provides a single "lobby return" callback
 * that any module (such as a game) can invoke to request a return
 * to the main lobby or menu screen.</p>
 *
 * <p>This class is entirely static and non-instantiable.
 * It is intentionally simple, with minimal dependencies,
 * to keep it suitable for use across different runtime layers (UI, games, etc.).</p>
 *
 * @author Clement Luo
 * @date August 8, 2025
 * @edited November 9, 2025
 * @since 1.0
 */
public final class MessagingBridge {

    /**
     * The global list of message consumers (subscribers).
     * <p>
     * Each consumer is a function (lambda or method reference)
     * that accepts a {@code Map<String, Object>} message.
     * When a message is published, every registered consumer will be called.
     * </p>
     *
     * <p>We use a {@link CopyOnWriteArrayList} here because:
     * <ul>
     *   <li>It is thread-safe for concurrent reads/writes.</li>
     *   <li>Iteration is safe even if another thread adds or removes consumers.</li>
     *   <li>It is optimized for scenarios with many reads (publish events)
     *       and few writes (consumer registration).</li>
     * </ul>
     * </p>
     */
    private static final CopyOnWriteArrayList<Consumer<Map<String, Object>>> consumers =
            new CopyOnWriteArrayList<>();

    /**
     * Private constructor to prevent instantiation.
     * This class is a static utility.
     */
    private MessagingBridge() {}

    // ==================== CONSUMER MANAGEMENT ====================

    /**
     * Replace all existing consumers with a single one.
     * <p>
     * This is primarily a compatibility or legacy helper for systems
     * that expect only one global listener for messages.
     * Newer code should prefer {@link #addConsumer(Consumer)} to allow
     * multiple observers.
     * </p>
     *
     * @param consumer the new consumer to install; if null, all consumers are cleared
     */
    public static void setConsumer(Consumer<Map<String, Object>> consumer) {
        consumers.clear();
        if (consumer != null) {
            consumers.add(consumer);
        }
    }

    /**
     * Add a new consumer to the message bus without removing existing ones.
     * <p>
     * This allows multiple systems to observe messages simultaneously.
     * For example, the UI, analytics module, and logging module
     * can all listen to the same published events.
     * </p>
     *
     * @param consumer the message consumer to add; ignored if null
     */
    public static void addConsumer(Consumer<Map<String, Object>> consumer) {
        if (consumer != null) {
            consumers.add(consumer);
        }
    }

    // ==================== MESSAGE PUBLISHING ====================

    /**
     * Publish a message to all registered consumers.
     * <p>
     * Every consumer in the list will be invoked with the same {@code message} map.
     * Exceptions thrown by one consumer are caught and ignored to ensure
     * that later consumers still receive the message.
     * </p>
     *
     * <p>
     * Example usage:
     * <pre>{@code
     * MessagingBridge.publish(Map.of(
     *     "event", "GAME_OVER",
     *     "winner", "Player 1",
     *     "durationMinutes", 12
     * ));
     * }</pre>
     * </p>
     *
     * <p>Typical use cases include:
     * <ul>
     *   <li>Games broadcasting status updates ("GAME_STARTED", "MOVE_MADE").</li>
     *   <li>UI modules receiving notifications to update labels or scores.</li>
     *   <li>Infrastructure components logging game events or metrics.</li>
     * </ul>
     * </p>
     *
     * @param message a key-value map representing metadata about the event
     */
    public static void publish(Map<String, Object> message) {
        for (Consumer<Map<String, Object>> c : consumers) {
            try {
                c.accept(message);
            } catch (Exception ignored) {
                // We intentionally suppress exceptions from one consumer
                // so that other listeners still receive the message.
            }
        }
    }

    // ==================== LOBBY RETURN FUNCTIONALITY ====================

    /**
     * A single callback used to return the user to the "lobby" or main menu.
     * <p>
     * This is a global one-to-one hook intended to allow
     * any game or subsystem to trigger a lobby return without
     * knowing how the UI is implemented.
     * </p>
     */
    private static LobbyReturnCallback lobbyReturnCallback = null;

    /**
     * Functional interface defining the contract for returning to the lobby.
     * <p>
     * This can be implemented as a lambda or anonymous class:
     * <pre>{@code
     * MessagingBridge.setLobbyReturnCallback(() -> uiController.showLobbyScene());
     * }</pre>
     * </p>
     */
    public interface LobbyReturnCallback {
        void returnToLobby();
    }

    /**
     * Install a single callback that can be invoked when the system
     * should navigate back to the lobby.
     * <p>
     * Typically, the main application or UI controller installs this callback
     * when initializing the game environment.
     * </p>
     *
     * @param callback the callback to register; replaces any existing one
     */
    public static void setLobbyReturnCallback(LobbyReturnCallback callback) {
        lobbyReturnCallback = callback;
    }

    /**
     * Attempt to return to the lobby by invoking the registered callback.
     * <p>
     * This method is "best effort"—if no callback is set, it does nothing.
     * Any exceptions thrown by the callback are caught and logged to stderr
     * to avoid crashing the game.
     * </p>
     *
     * <p>Example usage from within a game module:</p>
     * <pre>{@code
     * MessagingBridge.returnToLobby();
     * }</pre>
     *
     * <p>
     * Typical use cases:
     * <ul>
     *   <li>Called after a game session ends.</li>
     *   <li>Used by error handlers to exit back to a safe state.</li>
     *   <li>Invoked by UI buttons like "Return to Main Menu".</li>
     * </ul>
     * </p>
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
