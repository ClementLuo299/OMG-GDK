package gdk;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Core interface for all game modules.
 * Defines the contract that all games must implement to integrate with the GDK.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 21, 2025
 * @since 1.0
 */
public interface GameModule {
    
    /**
     * Gets the unique identifier for this game module.
     * @return The game ID (e.g., "tictactoe", "checkers")
     */
    String getGameId();
    
    /**
     * Gets the display name of the game.
     * @return The human-readable game name
     */
    String getGameName();
    
    /**
     * Launches the game.
     * 
     * @param primaryStage The primary JavaFX stage
     * @param playerCount Number of players
     * @param eventHandler Event handler (can be null)
     * @return The game scene
     */
    Scene launchGame(Stage primaryStage, int playerCount, Object eventHandler);
    
    /**
     * Called when the game is being closed.
     * Use this to clean up resources.
     */
    default void onGameClose() {
        // Default empty implementation
    }
    
    // ==================== DEFAULT IMPLEMENTATIONS ====================
    // These methods provide default values and can be overridden by games
    // Games can also communicate this information through events if needed
    
    /**
     * Gets a brief description of the game.
     * @return Game description
     */
    default String getGameDescription() {
        return "A game module for the GDK";
    }
    
    /**
     * Gets the minimum number of players required.
     * @return Minimum player count
     */
    default int getMinPlayers() {
        return 1;
    }
    
    /**
     * Gets the maximum number of players supported.
     * @return Maximum player count
     */
    default int getMaxPlayers() {
        return 4;
    }
    
    /**
     * Gets the estimated game duration in minutes.
     * @return Estimated duration
     */
    default int getEstimatedDuration() {
        return 10;
    }
    

    
    /**
     * Gets the category of the game.
     * @return Game category
     */
    default String getGameCategory() {
        return "General";
    }
    
    /**
     * Checks if the game supports online multiplayer.
     * @return true if online multiplayer is supported
     */
    default boolean supportsOnlineMultiplayer() {
        return false;
    }
    
    /**
     * Checks if the game supports local multiplayer.
     * @return true if local multiplayer is supported
     */
    default boolean supportsLocalMultiplayer() {
        return true;
    }
    
    /**
     * Checks if the game supports single player (vs AI).
     * @return true if single player is supported
     */
    default boolean supportsSinglePlayer() {
        return true;
    }
    

    

    

    

    

    

    

    

    

    

    

    
    /**
     * Gets the game's icon path (relative to resources).
     * @return Path to the game icon
     */
    default String getGameIconPath() {
        return "/games/" + getGameId() + "/icons/" + getGameId() + "_icon.png";
    }
    
    /**
     * Gets the game's FXML path (relative to resources).
     * @return Path to the game's FXML file
     */
    default String getGameFxmlPath() {
        return "/games/" + getGameId() + "/fxml/" + getGameId() + ".fxml";
    }
    
    /**
     * Gets the game's CSS path (relative to resources).
     * @return Path to the game's CSS file
     */
    default String getGameCssPath() {
        return "/games/" + getGameId() + "/css/" + getGameId() + ".css";
    }
} 