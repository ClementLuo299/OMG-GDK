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
 * Example game module to demonstrate the dynamic game discovery system.
 * This shows how easy it is to add new games without modifying the GUI.
 *
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class ExampleGameModule implements GameModule {
    
    private static final String GAME_ID = "example-game";
    private static final String GAME_NAME = "Example Game";
    private static final String GAME_DESCRIPTION = "A simple example game to demonstrate dynamic discovery";
    
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
        return 15; // 15 minutes
    }
    
    @Override
    public GameDifficulty getDifficulty() {
        return GameDifficulty.MEDIUM;
    }
    
    @Override
    public String getGameCategory() {
        return "Puzzle";
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
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions) {
        Logging.info("🎮 Launching " + getGameName() + " with mode: " + gameMode.getDisplayName() + ", players: " + playerCount);
        
        try {
            // Load the game's FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(getGameFxmlPath()));
            
            // Create the scene
            Scene scene = new Scene(loader.load());
            
            // Apply CSS if available
            String cssPath = getGameCssPath();
            if (cssPath != null && !cssPath.isEmpty()) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
            
            // Get the controller and initialize it
            Object controller = loader.getController();
            if (controller != null && controller instanceof ExampleGameController) {
                ExampleGameController exampleController = (ExampleGameController) controller;
                exampleController.initializeGame(gameMode, playerCount, gameOptions);
            }
            
            Logging.info("✅ " + getGameName() + " launched successfully");
            return scene;
            
        } catch (Exception e) {
            Logging.error("❌ Failed to launch " + getGameName() + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public String getGameIconPath() {
        return "/games/example/icons/example_icon.png";
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
    public void onGameClose() {
        Logging.info("🔄 " + getGameName() + " closing - cleaning up resources");
    }
    
    @Override
    public GameState getGameState() {
        GameOptions options = new GameOptions();
        options.setOption("exampleOption", "exampleValue");
        options.setOption("difficulty", "medium");
        
        GameState gameState = new GameState(GAME_ID, GAME_NAME, GameMode.LOCAL_MULTIPLAYER, 2, options);
        
        // Add example state data
        gameState.setStateValue("exampleData", "This is example game data");
        gameState.setStateValue("score", 0);
        
        return gameState;
    }
    
    @Override
    public void loadGameState(GameState gameState) {
        Logging.info("📂 Loading Example Game state");
        
        if (gameState != null) {
            String exampleData = gameState.getStringStateValue("exampleData", "No data");
            int score = gameState.getIntStateValue("score", 0);
            
            Logging.info("📊 Loaded example game state - Data: " + exampleData + ", Score: " + score);
        }
    }
    
    /**
     * Example game controller (placeholder).
     * In a real implementation, this would be a separate controller class.
     */
    public static class ExampleGameController {
        
        public void initializeGame(GameMode gameMode, int playerCount, GameOptions gameOptions) {
            Logging.info("🎯 Initializing Example Game with " + playerCount + " players");
            Logging.info("🎮 Game mode: " + gameMode.getDisplayName());
            
            // Example game initialization logic would go here
            String exampleOption = gameOptions.getStringOption("exampleOption", "default");
            Logging.info("⚙️ Example option: " + exampleOption);
        }
    }
} 