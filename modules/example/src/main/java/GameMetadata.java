package example;

import gdk.GameModule;
import gdk.Logging;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Map;
import java.util.HashMap;

/**
 * Game metadata and configuration for the Example game module.
 * Provides game information and handles game execution.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 24, 2025
 * @since 1.0
 */
public class GameMetadata implements GameModule {
    
    // ==================== GAME CONSTANTS ====================
    
    private static final String GAME_ID = "example";
    private static final String GAME_NAME = "Example Game";
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        ExampleGameModule gameModule = new ExampleGameModule();
        return gameModule.launchGame(primaryStage);
    }
    
    @Override
    public void onGameClose() {
        Logging.info("🔒 " + GAME_NAME + " closing - cleaning up resources");
    }
} 