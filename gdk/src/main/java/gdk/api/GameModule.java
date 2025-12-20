package gdk.api;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Core interface for all game modules.
 * Defines the contract that all games must implement to integrate with the GDK.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited November 10, 2025
 * @since Beta 1.0
 */
public interface GameModule {

    /**
     * Launches the game.
     *
     * @param primaryStage The primary JavaFX stage
     * @return The game scene
     */
    Scene launchGame(Stage primaryStage);

    /**
     * Called when the game is being closed.
     * Use this to stop the game and clean up resources.
     */
    default void stopGame() {
        // Default empty implementation
    }

    /**
     * Handles messages sent to the game module.
     * 
     * @param message The message data as a Map
     * @return Response data as a Map, or null if no response needed
     */
    default Map<String, Object> handleMessage(Map<String, Object> message) {
        // Default empty implementation
        return null;
    }

    /**
     * Returns the metadata for this game module.
     * 
     * @return The game metadata describing the module's configuration
     */
    GameMetadata getMetadata();
}
