package tictactoe;

import gdk.GameModule;
import gdk.Logging;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the TicTacToe game module.
 * Handles game start/stop operations and delegates to TicTacToeModule.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @edited July 24, 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    private TicTacToeModule gameModule; // Lazy initialization
    
    /**
     * Default constructor that initializes the module components.
     */
    public Main() {
        // Don't instantiate gameModule here - do it lazily when needed
        Logging.info("🎮 TicTacToe Main module initialized");
    }
    
    // ==================== GAME CONTROL ====================
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        try {
            Logging.info("🚀 Starting TicTacToe...");
            
            // Initialize game module lazily and delegate to it for actual game execution
            if (gameModule == null) {
                gameModule = new TicTacToeModule();
            }
            Scene gameScene = gameModule.launchGame(primaryStage);
            
            Logging.info("✅ TicTacToe launched successfully");
            return gameScene;
            
        } catch (Exception e) {
            Logging.error("❌ Error launching TicTacToe: " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void onGameClose() {
        try {
            Logging.info("🔒 TicTacToe closing - cleaning up resources");
            
            // Clean up game resources
            if (gameModule != null) {
                gameModule.onGameClose();
            }
            
            Logging.info("✅ TicTacToe cleanup completed");
            
        } catch (Exception e) {
            Logging.error("❌ Error during TicTacToe cleanup: " + e.getMessage(), e);
        }
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Gets the game module instance
     */
    public TicTacToeModule getGameModule() {
        if (gameModule == null) {
            gameModule = new TicTacToeModule();
        }
        return gameModule;
    }
    
    /**
     * Main method for standalone testing
     */
    public static void main(String[] args) {
        try {
            Logging.info("🎮 Starting TicTacToe as standalone application");
            
            // Create and launch the game module
            Main main = new Main();
            
            // Note: This would typically be called by the GDK launcher
            // For standalone testing, we create a simple stage
            javafx.application.Application.launch(javafx.application.Application.class, args);
            
        } catch (Exception e) {
            Logging.error("❌ Fatal error in TicTacToe: " + e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 