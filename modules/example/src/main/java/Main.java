package example;

import gdk.GameModule;
import gdk.Logging;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Map;

/**
 * Main entry point for the Example Game module.
 * Handles JSON communication and game start/stop operations.
 * Delegates metadata to GameMetadata class.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    private final GameMetadata metadata;
    private ExampleGameModule gameModule; // Lazy initialization
    
    /**
     * Default constructor that initializes the module components.
     */
    public Main() {
        this.metadata = new GameMetadata();
        // Don't instantiate gameModule here - do it lazily when needed
        Logging.info("🎮 Example Game Main module initialized");
    }
    
    // ==================== JSON COMMUNICATION & GAME CONTROL ====================
    
    @Override
    public Scene launchGame(Stage primaryStage, int playerCount, Object eventHandler) {
        try {
            Logging.info("🚀 Starting Example Game...");
            
            // Event system removed - use server simulator for communication
            
            // Initialize game module lazily and delegate to it for actual game execution
            if (gameModule == null) {
                gameModule = new ExampleGameModule();
            }
            Scene gameScene = gameModule.launchGame(primaryStage, playerCount, eventHandler);
            
            Logging.info("✅ Example Game launched successfully");
            return gameScene;
            
        } catch (Exception e) {
            Logging.error("❌ Error launching Example Game: " + e.getMessage(), e);
            
            // Event system removed - use server simulator for communication
            
            return null;
        }
    }
    
    @Override
    public void onGameClose() {
        try {
            Logging.info("🔒 Example Game closing - cleaning up resources");
            
            // Clean up any resources
            if (gameModule != null) {
                gameModule.onGameClose();
            }
            
        } catch (Exception e) {
            Logging.error("❌ Error during game close: " + e.getMessage(), e);
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Gets the metadata instance.
     * @return The GameMetadata instance
     */
    public GameMetadata getMetadata() {
        return metadata;
    }
    
    /**
     * Gets the game module instance.
     * @return The ExampleGameModule instance
     */
    public ExampleGameModule getGameModule() {
        if (gameModule == null) {
            gameModule = new ExampleGameModule();
        }
        return gameModule;
    }
    
    /**
     * Main method for standalone execution (if needed).
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Logging.info("🎮 Example Game module starting in standalone mode");
        
        try {
            Main main = new Main();
            Logging.info("✅ Example Game module initialized successfully");
            Logging.info("🎮 Game: Example Game");
            
        } catch (Exception e) {
            Logging.error("❌ Failed to initialize Example Game module: " + e.getMessage(), e);
        }
    }
} 