package com.games.modules.example;

import com.game.GameModule;
import com.game.enums.GameDifficulty;
import com.game.enums.GameMode;
import com.game.GameOptions;
import com.game.GameState;
import com.utils.error_handling.Logging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
    public Scene launchGame(Stage stage, GameMode mode, int playerCount, GameOptions options, com.game.GameEventHandler eventHandler) {
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

} 