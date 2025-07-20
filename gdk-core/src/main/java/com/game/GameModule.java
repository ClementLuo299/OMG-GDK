package com.game;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Interface for game modules with single-point communication.
 * 
 * Core Communication:
 * - Games communicate with GDK through GameEventHandler only
 * - All game events sent via eventHandler.handleGameEvent(GameEvent)
 * - GDK controls games through the launchGame method
 * 
 * Default Implementations:
 * - Most methods have sensible defaults
 * - Games can override defaults or communicate via events
 * - Minimal interface requirements for new games
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 20, 2025
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
     * Launches the game with single-point communication.
     * 
     * @param primaryStage The primary JavaFX stage
     * @param gameMode The game mode (SINGLE_PLAYER, LOCAL_MULTIPLAYER, ONLINE_MULTIPLAYER)
     * @param playerCount Number of players
     * @param gameOptions Additional game-specific options
     * @param eventHandler Single communication channel for game events
     * @return The game scene
     */
    Scene launchGame(Stage primaryStage, com.game.enums.GameMode gameMode, int playerCount, GameOptions gameOptions, GameEventHandler eventHandler);
    
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
     * Gets the difficulty level of the game.
     * @return Game difficulty
     */
    default com.game.enums.GameDifficulty getDifficulty() {
        return com.game.enums.GameDifficulty.MEDIUM;
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
    
    /**
     * Gets the current game state for saving/loading.
     * @return Game state object
     */
    default GameState getGameState() {
        return new GameState(getGameId(), getGameName(), com.game.enums.GameMode.LOCAL_MULTIPLAYER, 2, new GameOptions());
    }
    
    /**
     * Loads a saved game state.
     * @param gameState The saved game state
     */
    default void loadGameState(GameState gameState) {
        // Default empty implementation
    }
} 