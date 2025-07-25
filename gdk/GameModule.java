package gdk;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Core interface for all game modules.
 * Defines the contract that all games must implement to integrate with the GDK.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 24, 2025
 * @since 1.0
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
     * Use this to clean up resources.
     */
    default void onGameClose() {
        // Default empty implementation
    }
} 