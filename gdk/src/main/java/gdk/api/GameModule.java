package gdk.api;

import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Map;

/**
 * Core interface for all game modules.
 * Defines the contract that all games must implement to integrate with the GDK.
 * All game modules must provide metadata information.
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
     * Get the metadata for this game module.
     * 
     * @return The game metadata
     */
    GameMetadata getMetadata();
    
    /**
     * Get the display name of the game.
     * This is the name that will be shown in the UI.
     * 
     * @return The display name of the game
     */
    default String getGameName() {
        return getMetadata().getGameName();
    }
    
    /**
     * Get the version of the game.
     * 
     * @return The game version
     */
    default String getGameVersion() {
        return getMetadata().getGameVersion();
    }
    
    /**
     * Get the description of the game.
     * 
     * @return The game description
     */
    default String getGameDescription() {
        return getMetadata().getGameDescription();
    }
    
    /**
     * Get the author of the game.
     * 
     * @return The game author
     */
    default String getGameAuthor() {
        return getMetadata().getGameAuthor();
    }
} 