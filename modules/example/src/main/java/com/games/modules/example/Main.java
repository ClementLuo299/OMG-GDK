package com.games.modules.example;

import com.gdk.shared.game.GameModule;
import com.gdk.shared.game.GameMode;
import com.gdk.shared.game.GameDifficulty;
import com.gdk.shared.game.GameOptions;
import com.gdk.shared.game.GameState;
import com.gdk.shared.game.GameEventHandler;
import com.gdk.shared.settings.GameSettings;
import com.gdk.shared.utils.error_handling.Logging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Map;

/**
 * Example game module - demonstrates the standardized Main class structure.
 * This is the main entry point for the example game module.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    private static final String GAME_ID = "example";
    private static final String GAME_NAME = "Example Game";
    private static final String GAME_DESCRIPTION = "Template game for development and testing";
    
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
        return 1;
    }
    
    @Override
    public int getMaxPlayers() {
        return 4;
    }
    
    @Override
    public int getEstimatedDuration() {
        return 10; // 10 minutes
    }
    
    @Override
    public GameDifficulty getDifficulty() {
        return GameDifficulty.EASY;
    }
    
    @Override
    public String getGameCategory() {
        return "Classic";
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
    
    @Override
    public Scene launchGame(Stage stage, GameMode mode, int playerCount, GameOptions options, GameEventHandler eventHandler) {
        try {
            Logging.info("🎮 Launching " + GAME_NAME + " in " + mode + " mode with " + playerCount + " players");
            
            // Delegate to the ExampleGameModule for the test interface
            ExampleGameModule exampleModule = new ExampleGameModule();
            return exampleModule.launchGame(stage, mode, playerCount, options, eventHandler);
            
        } catch (Exception e) {
            Logging.error("❌ Error launching " + GAME_NAME + ": " + e.getMessage(), e);
            return null;
        }
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
    
    // ==================== ENHANCED SUPPORT METHODS ====================
    
    @Override
    public GameMode[] getSupportedGameModes() {
        // Delegate to ExampleGameModule for consistency
        ExampleGameModule exampleModule = new ExampleGameModule();
        return exampleModule.getSupportedGameModes();
    }
    
    @Override
    public GameDifficulty[] getSupportedDifficulties() {
        // Delegate to ExampleGameModule for consistency
        ExampleGameModule exampleModule = new ExampleGameModule();
        return exampleModule.getSupportedDifficulties();
    }
    
    @Override
    public Map<GameMode, int[]> getSupportedPlayerCounts() {
        // Delegate to ExampleGameModule for consistency
        ExampleGameModule exampleModule = new ExampleGameModule();
        return exampleModule.getSupportedPlayerCounts();
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
        return 1;
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
} 