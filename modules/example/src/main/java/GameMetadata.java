package example;

import gdk.GameModule;
import gdk.Logging;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Map;
import java.util.HashMap;

/**
 * Game metadata and configuration for the Example Game module.
 * Contains all the game information, supported modes, difficulties, and player counts.
 * This class is responsible for providing metadata to the GDK launcher.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class GameMetadata implements GameModule {
    
    // ==================== GAME METADATA ====================
    
    private static final String GAME_ID = "example";
    private static final String GAME_NAME = "Example Game";
    private static final String GAME_DESCRIPTION = "Template game for development and testing";
    private static final String GAME_CATEGORY = "Classic";
    private static final int MIN_PLAYERS = 1;
    private static final int MAX_PLAYERS = 4;
    private static final int ESTIMATED_DURATION = 10; // 10 minutes
    
    // ==================== GAME MODULE INTERFACE ====================
    
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
        return MIN_PLAYERS;
    }
    
    @Override
    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }
    
    @Override
    public int getEstimatedDuration() {
        return ESTIMATED_DURATION;
    }
    

    
    @Override
    public String getGameCategory() {
        return GAME_CATEGORY;
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
    
    // ==================== GAME EXECUTION (DELEGATED) ====================
    
    @Override
    public Scene launchGame(Stage primaryStage, int playerCount, Object eventHandler) {
        // Delegate to the actual game implementation
        ExampleGameModule gameModule = new ExampleGameModule();
        return gameModule.launchGame(primaryStage, playerCount, eventHandler);
    }
    
    @Override
    public void onGameClose() {
        Logging.info("🔒 " + GAME_NAME + " closing - cleaning up resources");
    }
} 