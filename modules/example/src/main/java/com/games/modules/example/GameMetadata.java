package com.games.modules.example;

import com.gdk.shared.game.GameModule;
import com.gdk.shared.game.GameMode;
import com.gdk.shared.game.GameDifficulty;
import com.gdk.shared.game.GameOptions;
import com.gdk.shared.game.GameState;
import com.gdk.shared.game.GameEventHandler;
import com.gdk.shared.settings.GameSettings;
import com.gdk.shared.utils.error_handling.Logging;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Map;
import java.util.HashMap;

/**
 * Game metadata and configuration for the Example Game module.
 * Contains all the game information, supported modes, difficulties, and player counts.
 * This class is responsible for providing metadata to the GDK launcher.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class GameMetadata implements GameModule {
    
    // ==================== GAME METADATA ====================
    
    private static final String GAME_ID = "example";
    private static final String GAME_NAME = "Example Game";
    private static final String GAME_DESCRIPTION = "Template game for development and testing";
    private static final String GAME_CATEGORY = "Classic";
    private static final int MIN_PLAYERS = 1;
    private static final int MAX_PLAYERS = 4;
    private static final int ESTIMATED_DURATION = 10; // 10 minutes
    
    // ==================== GAME MODULE INTERFACE ====================
    
    @Override
    public String getGameId() {
        return GAME_ID;
    }
    
    @Override
    public String getGameName() {
        return GAME_NAME;
    }
    
    @Override
    public String getGameDescription() {
        return GAME_DESCRIPTION;
    }
    
    @Override
    public int getMinPlayers() {
        return MIN_PLAYERS;
    }
    
    @Override
    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }
    
    @Override
    public int getEstimatedDuration() {
        return ESTIMATED_DURATION;
    }
    
    @Override
    public GameDifficulty getDifficulty() {
        return GameDifficulty.EASY;
    }
    
    @Override
    public String getGameCategory() {
        return GAME_CATEGORY;
    }
    
    @Override
    public boolean supportsOnlineMultiplayer() {
        return true;
    }
    
    @Override
    public boolean supportsLocalMultiplayer() {
        return true;
    }
    
    @Override
    public boolean supportsSinglePlayer() {
        return true;
    }
    
    @Override
    public String getGameFxmlPath() {
        return "/games/example/fxml/example.fxml";
    }
    
    @Override
    public String getGameCssPath() {
        return "/games/example/css/example.css";
    }
    
    @Override
    public String getGameIconPath() {
        return "/games/example/icons/example_icon.png";
    }
    
    // ==================== ENHANCED SUPPORT METHODS ====================
    
    @Override
    public GameMode[] getSupportedGameModes() {
        return new GameMode[] {
            GameMode.SINGLE_PLAYER,
            GameMode.PRACTICE,
            GameMode.LOCAL_MULTIPLAYER,
            GameMode.HOT_SEAT,
            GameMode.PUZZLE,
            GameMode.CREATIVE,
            GameMode.AI_VERSUS,
            GameMode.AI_COOP
        };
    }
    
    @Override
    public GameDifficulty[] getSupportedDifficulties() {
        return new GameDifficulty[] {
            GameDifficulty.EASY,
            GameDifficulty.MEDIUM,
            GameDifficulty.HARD,
            GameDifficulty.EXPERT,
            GameDifficulty.NIGHTMARE
        };
    }
    
    @Override
    public Map<GameMode, int[]> getSupportedPlayerCounts() {
        Map<GameMode, int[]> playerCounts = new HashMap<>();
        
        // Single player modes
        playerCounts.put(GameMode.SINGLE_PLAYER, new int[]{1});
        playerCounts.put(GameMode.PRACTICE, new int[]{1});
        playerCounts.put(GameMode.PUZZLE, new int[]{1});
        playerCounts.put(GameMode.CREATIVE, new int[]{1});
        
        // Multiplayer modes
        playerCounts.put(GameMode.LOCAL_MULTIPLAYER, new int[]{2, 3, 4});
        playerCounts.put(GameMode.HOT_SEAT, new int[]{2, 3, 4});
        playerCounts.put(GameMode.AI_VERSUS, new int[]{1, 2});
        playerCounts.put(GameMode.AI_COOP, new int[]{1, 2, 3, 4});
        
        return playerCounts;
    }
    
    @Override
    public GameMode getDefaultGameMode() {
        return GameMode.SINGLE_PLAYER;
    }
    
    @Override
    public GameDifficulty getDefaultDifficulty() {
        return GameDifficulty.EASY;
    }
    
    @Override
    public int getDefaultPlayerCount(GameMode gameMode) {
        Map<GameMode, int[]> playerCounts = getSupportedPlayerCounts();
        int[] counts = playerCounts.get(gameMode);
        return counts != null && counts.length > 0 ? counts[0] : 1;
    }
    
    @Override
    public boolean supportsGameMode(GameMode gameMode) {
        GameMode[] supportedModes = getSupportedGameModes();
        for (GameMode mode : supportedModes) {
            if (mode.equals(gameMode)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean supportsDifficulty(GameDifficulty difficulty) {
        GameDifficulty[] supportedDifficulties = getSupportedDifficulties();
        for (GameDifficulty diff : supportedDifficulties) {
            if (diff.equals(difficulty)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean supportsPlayerCount(GameMode gameMode, int playerCount) {
        Map<GameMode, int[]> supportedCounts = getSupportedPlayerCounts();
        int[] counts = supportedCounts.get(gameMode);
        if (counts != null) {
            for (int count : counts) {
                if (count == playerCount) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean hasCustomSettings() {
        return false;
    }
    
    @Override
    public GameSettings getCustomSettings() {
        return null;
    }
    
    // ==================== GAME EXECUTION (DELEGATED) ====================
    
    @Override
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions, GameEventHandler eventHandler) {
        // Delegate to the actual game implementation
        ExampleGameModule gameModule = new ExampleGameModule();
        return gameModule.launchGame(primaryStage, gameMode, playerCount, gameOptions, eventHandler);
    }
    
    @Override
    public GameState getGameState() {
        return new GameState(GAME_ID, "Not Started", GameMode.SINGLE_PLAYER, 1, new GameOptions());
    }
    
    @Override
    public void loadGameState(GameState gameState) {
        Logging.info("📂 Loading game state for " + GAME_NAME);
    }
    
    @Override
    public void onGameClose() {
        Logging.info("🔒 " + GAME_NAME + " closing - cleaning up resources");
    }
} 