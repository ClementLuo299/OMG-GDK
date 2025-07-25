package tictactoe;

import gdk.GameModule;
import gdk.Logging;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Game metadata and configuration for the TicTacToe game module.
 * Contains all the game information and delegates to TicTacToeModule for actual game execution.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @edited July 24, 2025
 * @since 1.0
 */
public class GameMetadata implements GameModule {
    
    // ==================== GAME METADATA ====================
    
    private static final String GAME_ID = "tictactoe";
    private static final String GAME_NAME = "Tic Tac Toe";
    private static final String GAME_DESCRIPTION = "Classic Tic Tac Toe game with AI opponent";
    private static final String GAME_CATEGORY = "Strategy";
    private static final int MIN_PLAYERS = 1;
    private static final int MAX_PLAYERS = 2;
    private static final int ESTIMATED_DURATION = 5; // 5 minutes
    
    private TicTacToeModule gameModule; // Lazy initialization
    
    // ==================== GAME MODULE INTERFACE ====================
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        try {
            Logging.info("🚀 Starting TicTacToe via GameMetadata...");
            
            // Initialize game module lazily and delegate to it for actual game execution
            if (gameModule == null) {
                gameModule = new TicTacToeModule();
            }
            Scene gameScene = gameModule.launchGame(primaryStage);
            
            Logging.info("✅ TicTacToe launched successfully via GameMetadata");
            return gameScene;
            
        } catch (Exception e) {
            Logging.error("❌ Error launching TicTacToe via GameMetadata: " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void onGameClose() {
        try {
            Logging.info("🔒 TicTacToe closing via GameMetadata - cleaning up resources");
            
            // Clean up game resources
            if (gameModule != null) {
                gameModule.onGameClose();
            }
            
            Logging.info("✅ TicTacToe cleanup completed via GameMetadata");
            
        } catch (Exception e) {
            Logging.error("❌ Error during TicTacToe cleanup via GameMetadata: " + e.getMessage(), e);
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
     * Gets the game ID
     */
    public String getGameId() {
        return GAME_ID;
    }
    
    /**
     * Gets the game name
     */
    public String getGameName() {
        return GAME_NAME;
    }
    
    /**
     * Gets the game description
     */
    public String getGameDescription() {
        return GAME_DESCRIPTION;
    }
    
    /**
     * Gets the minimum number of players
     */
    public int getMinPlayers() {
        return MIN_PLAYERS;
    }
    
    /**
     * Gets the maximum number of players
     */
    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }
    
    /**
     * Gets the estimated duration in minutes
     */
    public int getEstimatedDuration() {
        return ESTIMATED_DURATION;
    }
    
    /**
     * Gets the game category
     */
    public String getGameCategory() {
        return GAME_CATEGORY;
    }
    
    /**
     * Checks if the game supports online multiplayer
     */
    public boolean supportsOnlineMultiplayer() {
        return false; // TicTacToe is typically local
    }
    
    /**
     * Checks if the game supports local multiplayer
     */
    public boolean supportsLocalMultiplayer() {
        return true;
    }
    
    /**
     * Checks if the game supports single player
     */
    public boolean supportsSinglePlayer() {
        return true; // Against AI
    }
    
    /**
     * Gets the FXML path
     */
    public String getGameFxmlPath() {
        return "/games/tictactoe/fxml/tictactoe.fxml";
    }
    
    /**
     * Gets the CSS path
     */
    public String getGameCssPath() {
        return "/games/tictactoe/css/tictactoe.css";
    }
    
    /**
     * Gets the icon path
     */
    public String getGameIconPath() {
        return "/games/tictactoe/icons/tic_tac_toe_icon.png";
    }
} 