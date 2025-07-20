package com.games.modules.tictactoe;

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
 * TicTacToe game module implementation.
 * Demonstrates how to create a game module that integrates with the main application.
 *
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class TicTacToeModule implements GameModule {
    
    private static final String GAME_ID = "tictactoe";
    private static final String GAME_NAME = "Tic Tac Toe";
    private static final String GAME_DESCRIPTION = "Classic 3x3 grid game for two players";
    
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
        return 2;
    }
    
    @Override
    public int getMaxPlayers() {
        return 2;
    }
    
    @Override
    public int getEstimatedDuration() {
        return 5; // 5 minutes
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
        return true; // vs AI
    }
    
    @Override
    public String getGameFxmlPath() {
        return "/games/tictactoe/fxml/tictactoe.fxml";
    }
    
    @Override
    public String getGameCssPath() {
        return "/games/tictactoe/css/tictactoe.css";
    }
    
    @Override
    public String getGameIconPath() {
        return "/games/tictactoe/icons/tic_tac_toe_icon.png";
    }
    
    @Override
    public void onGameClose() {
        Logging.info("🔄 " + getGameName() + " closing - cleaning up resources");
    }
    
    @Override
    public GameState getGameState() {
        // This would be implemented to save the current game state
        // For now, return a basic game state
        GameOptions options = new GameOptions();
        options.setOption("boardSize", 3);
        options.setOption("aiDifficulty", "medium");
        options.setOption("timeLimit", 30);
        
        GameState gameState = new GameState(GAME_ID, GAME_NAME, GameMode.LOCAL_MULTIPLAYER, 2, options);
        
        // Add game-specific state data
        gameState.setStateValue("currentPlayer", "X");
        gameState.setStateValue("movesCount", 0);
        gameState.setStateValue("gameBoard", new String[3][3]); // 3x3 board
        
        return gameState;
    }
    
    @Override
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions) {
        Logging.info("🎮 Launching " + getGameName() + " with mode: " + gameMode.getDisplayName() + ", players: " + playerCount);
        
        try {
            // Load the game's FXML using the correct resource path
            String fxmlPath = getGameFxmlPath();
            Logging.info("📁 Loading FXML from: " + fxmlPath);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            
            // Create the scene
            Scene scene = new Scene(loader.load());
            
            // Apply CSS if available
            String cssPath = getGameCssPath();
            if (cssPath != null && !cssPath.isEmpty()) {
                try {
                    String cssUrl = getClass().getResource(cssPath).toExternalForm();
                    scene.getStylesheets().add(cssUrl);
                    Logging.info("🎨 Applied CSS: " + cssUrl);
                } catch (Exception e) {
                    Logging.warning("⚠️ Could not load CSS: " + cssPath + " - " + e.getMessage());
                }
            }
            
            // Get the controller and initialize it
            Object controller = loader.getController();
            if (controller != null) {
                if (controller instanceof TicTacToeController) {
                    TicTacToeController ticTacToeController = (TicTacToeController) controller;
                    ticTacToeController.initializeGame(gameMode, playerCount, gameOptions);
                }
            } else {
                Logging.error("❌ Controller is null for " + getGameName());
            }
            
            Logging.info("✅ " + getGameName() + " launched successfully");
            return scene;
            
        } catch (Exception e) {
            Logging.error("❌ Failed to launch " + getGameName() + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Creates a TicTacToeScreenLoadable for integration with the main app's screen system.
     * This method allows TicTacToe to be launched using the main application's ScreenManager
     * while keeping all game-specific resources and configuration within the module.
     * 
     * @param gameMode The game mode
     * @param playerCount Number of players
     * @param gameOptions Game-specific options
     * @return A TicTacToeScreenLoadable instance
     */
    public TicTacToeScreenLoadable createScreenLoadable(GameMode gameMode, int playerCount, GameOptions gameOptions) {
        Logging.info("🎮 Creating TicTacToeScreenLoadable for " + getGameName());
        return new TicTacToeScreenLoadable(gameMode, playerCount, gameOptions);
    }
    
    /**
     * Loads the TicTacToe screen using the module's own screen loader.
     * This method provides a way to load the game screen with proper initialization
     * while keeping all game-specific logic within the module.
     * 
     * @param gameMode The game mode
     * @param playerCount Number of players
     * @param gameOptions Game-specific options
     * @return The screen load result
     */
    public com.core.screens.ScreenLoadResult<TicTacToeController> loadGameScreen(GameMode gameMode, int playerCount, GameOptions gameOptions) {
        Logging.info("🎮 Loading TicTacToe game screen using module's screen loader");
        
        TicTacToeScreenLoadable screenLoadable = createScreenLoadable(gameMode, playerCount, gameOptions);
        TicTacToeScreenLoader screenLoader = new TicTacToeScreenLoader();
        
        return screenLoader.loadTicTacToeScreen(screenLoadable);
    }
    
    @Override
    public void loadGameState(GameState gameState) {
        Logging.info("📂 Loading TicTacToe game state");
        
        if (gameState != null) {
            // Load game-specific state data
            String currentPlayer = gameState.getStringStateValue("currentPlayer", "X");
            int movesCount = gameState.getIntStateValue("movesCount", 0);
            
            Logging.info("📊 Loaded game state - Current player: " + currentPlayer + ", Moves: " + movesCount);
        }
    }
} 