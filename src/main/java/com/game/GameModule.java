package com.game;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Interface for game modules that can be launched from the main application.
 * Each game should implement this interface to integrate with the game launcher.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 19, 2025
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
     * Gets a brief description of the game.
     * @return Game description
     */
    String getGameDescription();
    
    /**
     * Gets the minimum number of players required.
     * @return Minimum player count
     */
    int getMinPlayers();
    
    /**
     * Gets the maximum number of players supported.
     * @return Maximum player count
     */
    int getMaxPlayers();
    
    /**
     * Gets the estimated game duration in minutes.
     * @return Estimated duration
     */
    int getEstimatedDuration();
    
    /**
     * Gets the difficulty level of the game.
     * @return Game difficulty
     */
    com.game.enums.GameDifficulty getDifficulty();
    
    /**
     * Gets the category of the game.
     * @return Game category (e.g., "Strategy", "Puzzle", "Card", "Classic")
     */
    String getGameCategory();
    
    /**
     * Checks if the game supports online multiplayer.
     * @return true if online multiplayer is supported
     */
    boolean supportsOnlineMultiplayer();
    
    /**
     * Checks if the game supports local multiplayer.
     * @return true if local multiplayer is supported
     */
    boolean supportsLocalMultiplayer();
    
    /**
     * Checks if the game supports single player (vs AI).
     * @return true if single player is supported
     */
    boolean supportsSinglePlayer();
    
    /**
     * Launches the game with the specified parameters.
     * 
     * @param primaryStage The primary JavaFX stage
     * @param gameMode The game mode (SINGLE_PLAYER, LOCAL_MULTIPLAYER, ONLINE_MULTIPLAYER)
     * @param playerCount Number of players
     * @param gameOptions Additional game-specific options
     * @return The game scene
     */
    Scene launchGame(Stage primaryStage, com.game.enums.GameMode gameMode, int playerCount, GameOptions gameOptions);
    
    /**
     * Gets the game's icon path (relative to resources).
     * @return Path to the game icon
     */
    String getGameIconPath();
    
    /**
     * Gets the game's FXML path (relative to resources).
     * @return Path to the game's FXML file
     */
    String getGameFxmlPath();
    
    /**
     * Gets the game's CSS path (relative to resources).
     * @return Path to the game's CSS file
     */
    String getGameCssPath();
    
    /**
     * Called when the game is being closed.
     * Use this to clean up resources, save game state, etc.
     */
    void onGameClose();
    
    /**
     * Gets the current game state for saving/loading.
     * @return Game state object
     */
    GameState getGameState();
    
    /**
     * Loads a saved game state.
     * @param gameState The saved game state
     */
    void loadGameState(GameState gameState);
} 