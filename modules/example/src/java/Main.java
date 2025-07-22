package example;

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
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions, GameEventHandler eventHandler) {
        try {
            Logging.info("🚀 Starting Example Game...");
            
            // Handle JSON data if present
            handleJsonData(gameOptions);
            
            // Log game start event
            eventHandler.handleGameEvent(new com.gdk.shared.game.GameEvent(
                com.gdk.shared.game.GameEvent.EventType.GAME_STARTED,
                getGameId(),
                "Example Game started with " + playerCount + " players in " + gameMode.getDisplayName() + " mode"
            ));
            
            // Initialize game module lazily and delegate to it for actual game execution
            if (gameModule == null) {
                gameModule = new ExampleGameModule();
            }
            Scene gameScene = gameModule.launchGame(primaryStage, gameMode, playerCount, gameOptions, eventHandler);
            
            Logging.info("✅ Example Game launched successfully");
            return gameScene;
            
        } catch (Exception e) {
            Logging.error("❌ Error launching Example Game: " + e.getMessage(), e);
            
            // Send error event
            eventHandler.handleGameEvent(new com.gdk.shared.game.GameEvent(
                com.gdk.shared.game.GameEvent.EventType.ERROR_OCCURRED,
                getGameId(),
                "Failed to launch Example Game: " + e.getMessage()
            ));
            
            return null;
        }
    }
    
    @Override
    public void onGameClose() {
        try {
            Logging.info("🔒 Example Game closing - cleaning up resources");
            
            // Clean up game resources
            if (gameModule != null) {
                gameModule.onGameClose();
            }
            
            Logging.info("✅ Example Game cleanup completed");
            
        } catch (Exception e) {
            Logging.error("❌ Error during Example Game cleanup: " + e.getMessage(), e);
        }
    }
    
    @Override
    public GameState getGameState() {
        return metadata.getGameState();
    }
    
    @Override
    public void loadGameState(GameState gameState) {
        try {
            Logging.info("📂 Loading game state for Example Game");
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
            // Example JSON processing
            if (data.containsKey("playerName")) {
                String playerName = (String) data.get("playerName");
                Logging.info("👤 Player name from JSON: " + playerName);
            }
            
            if (data.containsKey("level")) {
                Object levelObj = data.get("level");
                if (levelObj instanceof Number) {
                    int level = ((Number) levelObj).intValue();
                    Logging.info("📊 Level from JSON: " + level);
                }
            }
            
            if (data.containsKey("settings")) {
                Object settingsObj = data.get("settings");
                if (settingsObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> settings = (Map<String, Object>) settingsObj;
                    Logging.info("⚙️ Settings from JSON: " + settings);
                }
            }
            
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
    public GameDifficulty getDifficulty() {
        return metadata.getDifficulty();
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
    public GameDifficulty[] getSupportedDifficulties() {
        return metadata.getSupportedDifficulties();
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
    public GameDifficulty getDefaultDifficulty() {
        return metadata.getDefaultDifficulty();
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
    public boolean supportsDifficulty(GameDifficulty difficulty) {
        return metadata.supportsDifficulty(difficulty);
    }
    
    @Override
    public boolean supportsPlayerCount(GameMode gameMode, int playerCount) {
        return metadata.supportsPlayerCount(gameMode, playerCount);
    }
    
    @Override
    public boolean hasCustomSettings() {
        return metadata.hasCustomSettings();
    }
    
    @Override
    public GameSettings getCustomSettings() {
        return metadata.getCustomSettings();
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
            Logging.info("🎮 Game: " + main.getGameName());
            Logging.info("📝 Description: " + main.getGameDescription());
            Logging.info("👥 Players: " + main.getMinPlayers() + "-" + main.getMaxPlayers());
            Logging.info("⏱️ Duration: " + main.getEstimatedDuration() + " minutes");
            Logging.info("🎯 Difficulty: " + main.getDifficulty());
            Logging.info("📂 Category: " + main.getGameCategory());
            
        } catch (Exception e) {
            Logging.error("❌ Failed to initialize Example Game module: " + e.getMessage(), e);
        }
    }
} 