package gdk.api;

import java.util.Map;

/**
 * Core contract for all game modules in the GDK.
 * <p>
 * This interface is intentionally minimal and framework-agnostic.
 * It defines three universal responsibilities:
 * launching the game, stopping it, and handling messages.
 * <p>
 * Games should use the messaging protocol for all metadata,
 * control commands, and runtime communication.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited November 10, 2025
 * @since Beta 1.0
 */
public interface GameModule {

    /**
     * Launches the game and returns a platform-specific view object.
     * <p>
     * The returned {@link GameView} can represent any visual or logical entry point —
     * a JavaFX Scene, a Canvas, an HTML component, or a custom rendering object.
     *
     * @param context Optional environment or platform context (may be null)
     * @return A GameView object representing the game's visual or logical entry point
     */
    GameView launchGame(Object context);

    /**
     * Stops the game and cleans up resources.
     * Implementations should gracefully shut down logic, UI, and networking.
     */
    default void stopGame() {}

    /**
     * Handles an incoming message from the GDK, launcher, or another module.
     * <p>
     * The message format is flexible; all communication (metadata, state, commands)
     * should be done through this mechanism.
     *
     * @param message A Map representing the message data
     * @return Optional response data, or null if no response is needed
     */
    default Map<String, Object> handleMessage(Map<String, Object> message) { return null; }
}
