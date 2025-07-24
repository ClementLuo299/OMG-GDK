package tictactoe;

import gdk.GameModule;
import gdk.GameMode;
import gdk.GameOptions;
import gdk.GameState;

import gdk.Logging;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Map;

/**
 * Main entry point for the TicTacToe game module.
 * Handles JSON communication and game start/stop operations.
 * Delegates metadata to GameMetadata class.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    private final GameMetadata metadata;
    private TicTacToeModule gameModule; // Lazy initialization
    
    /**
     * Default constructor that initializes the module components.
     */
    public Main() {
        this.metadata = new GameMetadata();
        // Don't instantiate gameModule here - do it lazily when needed
        Logging.info("🎮 TicTacToe Main module initialized");
    }
    
    // ==================== JSON COMMUNICATION & GAME CONTROL ====================
    
    @Override
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions, Object eventHandler) {
        try {
            Logging.info("🚀 Starting TicTacToe...");
            
            // Handle JSON data if present
            handleJsonData(gameOptions);
            
            // Event system removed - use server simulator for communication
            
            // Initialize game module lazily and delegate to it for actual game execution
            if (gameModule == null) {
                gameModule = new TicTacToeModule();
            }
            Scene gameScene = gameModule.launchGame(primaryStage, gameMode, playerCount, gameOptions, eventHandler);
            
            Logging.info("✅ TicTacToe launched successfully");
            return gameScene;
            
        } catch (Exception e) {
            Logging.error("❌ Error launching TicTacToe: " + e.getMessage(), e);
            
            // Event system removed - use server simulator for communication
            
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
    
    @Override
    public GameState getGameState() {
        return metadata.getGameState();
    }
    
    @Override
    public void loadGameState(GameState gameState) {
        try {
            Logging.info("📂 Loading game state for TicTacToe");
            metadata.loadGameState(gameState);
            Logging.info("✅ Game state loaded successfully");
        } catch (Exception e) {
            Logging.error("❌ Error loading game state: " + e.getMessage(), e);
        }
    }
    
    // ==================== JSON DATA HANDLING ====================
    
    /**
     * Handles JSON data received from the launcher.
     * @param gameOptions The game options containing JSON data
     */
    private void handleJsonData(GameOptions gameOptions) {
        if (gameOptions.hasOption("customData")) {
            Object customData = gameOptions.getOption("customData", null);
            Logging.info("📦 Received JSON data: " + customData);
            
            // Process the JSON data as needed
            if (customData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) customData;
                processJsonData(data);
            }
        } else {
            Logging.info("📦 No JSON data received");
        }
    }
    
    /**
     * Processes the parsed JSON data.
     * @param data The parsed JSON data as a Map
     */
    private void processJsonData(Map<String, Object> data) {
        try {
            // TicTacToe-specific JSON processing
            if (data.containsKey("player1Name")) {
                String player1Name = (String) data.get("player1Name");
                Logging.info("👤 Player 1 name from JSON: " + player1Name);
            }
            
            if (data.containsKey("player2Name")) {
                String player2Name = (String) data.get("player2Name");
                Logging.info("👤 Player 2 name from JSON: " + player2Name);
            }
            
            if (data.containsKey("aiDifficulty")) {
                String aiDifficulty = (String) data.get("aiDifficulty");
                Logging.info("🤖 AI difficulty from JSON: " + aiDifficulty);
            }
            
            if (data.containsKey("boardSize")) {
                Object boardSizeObj = data.get("boardSize");
                if (boardSizeObj instanceof Number) {
                    int boardSize = ((Number) boardSizeObj).intValue();
                    Logging.info("📐 Board size from JSON: " + boardSize);
                }
            }
            
            // Settings system removed - all configuration handled via JSON
            
        } catch (Exception e) {
            Logging.error("❌ Error processing JSON data: " + e.getMessage(), e);
        }
    }
    
    // ==================== METADATA DELEGATION ====================
    
    @Override
    public String getGameId() {
        return metadata.getGameId();
    }
    
    @Override
    public String getGameName() {
        return metadata.getGameName();
    }
    
    @Override
    public String getGameDescription() {
        return metadata.getGameDescription();
    }
    
    @Override
    public int getMinPlayers() {
        return metadata.getMinPlayers();
    }
    
    @Override
    public int getMaxPlayers() {
        return metadata.getMaxPlayers();
    }
    
    @Override
    public int getEstimatedDuration() {
        return metadata.getEstimatedDuration();
    }
    

    
    @Override
    public String getGameCategory() {
        return metadata.getGameCategory();
    }
    
    @Override
    public boolean supportsOnlineMultiplayer() {
        return metadata.supportsOnlineMultiplayer();
    }
    
    @Override
    public boolean supportsLocalMultiplayer() {
        return metadata.supportsLocalMultiplayer();
    }
    
    @Override
    public boolean supportsSinglePlayer() {
        return metadata.supportsSinglePlayer();
    }
    
    @Override
    public String getGameFxmlPath() {
        return metadata.getGameFxmlPath();
    }
    
    @Override
    public String getGameCssPath() {
        return metadata.getGameCssPath();
    }
    
    @Override
    public String getGameIconPath() {
        return metadata.getGameIconPath();
    }
    
    @Override
    public GameMode[] getSupportedGameModes() {
        return metadata.getSupportedGameModes();
    }
    

    
    @Override
    public Map<GameMode, int[]> getSupportedPlayerCounts() {
        return metadata.getSupportedPlayerCounts();
    }
    
    @Override
    public GameMode getDefaultGameMode() {
        return metadata.getDefaultGameMode();
    }
    

    
    @Override
    public int getDefaultPlayerCount(GameMode gameMode) {
        return metadata.getDefaultPlayerCount(gameMode);
    }
    
    @Override
    public boolean supportsGameMode(GameMode gameMode) {
        return metadata.supportsGameMode(gameMode);
    }
    

    
    @Override
    public boolean supportsPlayerCount(GameMode gameMode, int playerCount) {
        return metadata.supportsPlayerCount(gameMode, playerCount);
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
     * @return The TicTacToeModule instance
     */
    public TicTacToeModule getGameModule() {
        if (gameModule == null) {
            gameModule = new TicTacToeModule();
        }
        return gameModule;
    }
    
    /**
     * Main method for standalone execution (if needed).
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

            Logging.info("📂 Category: " + main.getGameCategory());
            
        } catch (Exception e) {
            Logging.error("❌ Failed to initialize TicTacToe module: " + e.getMessage(), e);
        }
    }
} 