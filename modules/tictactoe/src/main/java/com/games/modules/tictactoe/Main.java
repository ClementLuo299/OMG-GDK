package com.games.modules.tictactoe;

import com.game.GameModule;
import com.gdk.shared.enums.GameDifficulty;
import com.gdk.shared.enums.GameMode;
import com.game.GameOptions;
import com.game.GameState;
import com.game.GameEventHandler;
import com.utils.error_handling.Logging;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class for the TicTacToe game module.
 * This class serves as the entry point for the module and delegates to TicTacToeModule.
 * It preserves all existing functionality while providing a standardized Main class interface.
 *
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    private final TicTacToeModule ticTacToeModule;
    
    /**
     * Default constructor that initializes the TicTacToe module.
     */
    public Main() {
        this.ticTacToeModule = new TicTacToeModule();
        Logging.info("🎮 TicTacToe Main module initialized");
    }
    
    // ==================== GAME MODULE DELEGATION ====================
    
    @Override
    public String getGameId() {
        return ticTacToeModule.getGameId();
    }
    
    @Override
    public String getGameName() {
        return ticTacToeModule.getGameName();
    }
    
    @Override
    public String getGameDescription() {
        return ticTacToeModule.getGameDescription();
    }
    
    @Override
    public int getMinPlayers() {
        return ticTacToeModule.getMinPlayers();
    }
    
    @Override
    public int getMaxPlayers() {
        return ticTacToeModule.getMaxPlayers();
    }
    
    @Override
    public int getEstimatedDuration() {
        return ticTacToeModule.getEstimatedDuration();
    }
    
    @Override
    public GameDifficulty getDifficulty() {
        return ticTacToeModule.getDifficulty();
    }
    
    @Override
    public String getGameCategory() {
        return ticTacToeModule.getGameCategory();
    }
    
    @Override
    public boolean supportsOnlineMultiplayer() {
        return ticTacToeModule.supportsOnlineMultiplayer();
    }
    
    @Override
    public boolean supportsLocalMultiplayer() {
        return ticTacToeModule.supportsLocalMultiplayer();
    }
    
    @Override
    public boolean supportsSinglePlayer() {
        return ticTacToeModule.supportsSinglePlayer();
    }
    
    @Override
    public String getGameFxmlPath() {
        return ticTacToeModule.getGameFxmlPath();
    }
    
    @Override
    public String getGameCssPath() {
        return ticTacToeModule.getGameCssPath();
    }
    
    @Override
    public String getGameIconPath() {
        return ticTacToeModule.getGameIconPath();
    }
    
    @Override
    public void onGameClose() {
        ticTacToeModule.onGameClose();
    }
    
    @Override
    public GameState getGameState() {
        return ticTacToeModule.getGameState();
    }
    
    @Override
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions, GameEventHandler eventHandler) {
        return ticTacToeModule.launchGame(primaryStage, gameMode, playerCount, gameOptions, eventHandler);
    }
    
    @Override
    public void loadGameState(GameState gameState) {
        ticTacToeModule.loadGameState(gameState);
    }
    
    // ==================== ADDITIONAL FUNCTIONALITY ====================
    
    /**
     * Gets the underlying TicTacToeModule instance.
     * This allows access to any additional functionality not exposed through the GameModule interface.
     * 
     * @return The TicTacToeModule instance
     */
    public TicTacToeModule getTicTacToeModule() {
        return ticTacToeModule;
    }
    
    /**
     * Main method for standalone execution (if needed).
     * This allows the module to be run independently for testing.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Logging.info("🎮 TicTacToe module starting in standalone mode");
        
        try {
            Main main = new Main();
            Logging.info("✅ TicTacToe module initialized successfully");
            Logging.info("🎮 Game: " + main.getGameName());
            Logging.info("📝 Description: " + main.getGameDescription());
            Logging.info("👥 Players: " + main.getMinPlayers() + "-" + main.getMaxPlayers());
            Logging.info("⏱️ Duration: " + main.getEstimatedDuration() + " minutes");
            Logging.info("🎯 Difficulty: " + main.getDifficulty());
            Logging.info("📂 Category: " + main.getGameCategory());
            
        } catch (Exception e) {
            Logging.error("❌ Failed to initialize TicTacToe module: " + e.getMessage(), e);
        }
    }
} 