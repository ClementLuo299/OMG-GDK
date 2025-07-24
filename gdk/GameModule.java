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
     * @param gameMode The game mode
     * @param playerCount Number of players
     * @param gameOptions Additional game-specific options
     * @param eventHandler Event handler (can be null)
     * @return The game scene
     */
    Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions, Object eventHandler);
    
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
    
    // ==================== ENHANCED SUPPORT METHODS ====================
    // These methods allow games to specify exactly what they support
    
    /**
     * Gets the list of supported game modes for this game.
     * @return Array of supported game modes
     */
    default GameMode[] getSupportedGameModes() {
        // Default implementation based on the old boolean methods
        java.util.List<GameMode> modes = new java.util.ArrayList<>();
        
        if (supportsSinglePlayer()) {
            modes.add(GameMode.SINGLE_PLAYER);
        }
        if (supportsLocalMultiplayer()) {
            modes.add(GameMode.LOCAL_MULTIPLAYER);
        }
        if (supportsOnlineMultiplayer()) {
            modes.add(GameMode.ONLINE_MULTIPLAYER);
        }
        
        return modes.toArray(new GameMode[0]);
    }
    

    
    /**
     * Gets the supported player count ranges for each game mode.
     * @return Map of game mode to player count range (min, max)
     */
    default java.util.Map<GameMode, int[]> getSupportedPlayerCounts() {
        java.util.Map<GameMode, int[]> playerCounts = new java.util.HashMap<>();
        
        // Default implementation based on min/max players
        int minPlayers = getMinPlayers();
        int maxPlayers = getMaxPlayers();
        
        for (GameMode mode : getSupportedGameModes()) {
            playerCounts.put(mode, new int[]{minPlayers, maxPlayers});
        }
        
        return playerCounts;
    }
    
    /**
     * @return The default game mode
     */
    default GameMode getDefaultGameMode() {
        GameMode[] supportedModes = getSupportedGameModes();
        if (supportedModes.length > 0) {
            return supportedModes[0];
        }
        return GameMode.SINGLE_PLAYER;
    }
    

    
    /**
     * Gets the default player count for a specific game mode.
     * @param gameMode The game mode
     * @return The default player count for that mode
     */
    default int getDefaultPlayerCount(GameMode gameMode) {
        java.util.Map<GameMode, int[]> playerCounts = getSupportedPlayerCounts();
        int[] range = playerCounts.get(gameMode);
        if (range != null) {
            return range[0]; // Return minimum as default
        }
        return getMinPlayers();
    }
    
    /**
     * Checks if a specific game mode is supported.
     * @param gameMode The game mode to check
     * @return true if the game mode is supported
     */
    default boolean supportsGameMode(GameMode gameMode) {
        for (GameMode supportedMode : getSupportedGameModes()) {
            if (supportedMode.equals(gameMode)) {
                return true;
            }
        }
        return false;
    }
    

    
    /**
     * Checks if a specific player count is supported for a game mode.
     * @param gameMode The game mode
     * @param playerCount The player count to check
     * @return true if the player count is supported for that mode
     */
    default boolean supportsPlayerCount(GameMode gameMode, int playerCount) {
        java.util.Map<GameMode, int[]> playerCounts = getSupportedPlayerCounts();
        int[] range = playerCounts.get(gameMode);
        if (range != null) {
            return playerCount >= range[0] && playerCount <= range[1];
        }
        return false;
    }
    
    // ==================== CUSTOM SETTINGS SUPPORT ====================
    
    /**
     * Gets the custom settings for this game.
     * Games can override this to provide custom configuration options.
     * @return The game settings, or null if no custom settings
     */
    default GameSettings getCustomSettings() {
        return null; // Default: no custom settings
    }
    
    /**
     * Checks if this game has custom settings.
     * @return true if the game has custom settings
     */
    default boolean hasCustomSettings() {
        return getCustomSettings() != null;
    }
    
    /**
     * Gets the number of custom settings for this game.
     * @return The number of custom settings
     */
    default int getCustomSettingsCount() {
        GameSettings settings = getCustomSettings();
        return settings != null ? settings.getCustomSettings().size() : 0;
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
        return new GameState(getGameId(), getGameName(), GameMode.LOCAL_MULTIPLAYER, 2, new GameOptions());
    }
    
    /**
     * Loads a saved game state.
     * @param gameState The saved game state
     */
    default void loadGameState(GameState gameState) {
        // Default empty implementation
    }
} 