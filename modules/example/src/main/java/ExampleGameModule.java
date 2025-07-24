package example;

import com.gdk.shared.game.GameModule;
import com.gdk.shared.game.GameMode;

import com.gdk.shared.game.GameOptions;
import com.gdk.shared.game.GameState;

import com.gdk.shared.settings.GameSettings;
import com.gdk.shared.utils.error_handling.Logging;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Map;

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
    
    // ==================== ENHANCED SUPPORT METHODS ====================
    // Example game demonstrates custom support configuration
    
    @Override
    public GameMode[] getSupportedGameModes() {
        // Example game supports many modes for demonstration
        return new GameMode[] {
            GameMode.SINGLE_PLAYER,
            GameMode.PRACTICE,
            GameMode.LOCAL_MULTIPLAYER,
            GameMode.HOT_SEAT,
            GameMode.PUZZLE,
            GameMode.CREATIVE,
            GameMode.AI_VERSUS,
            GameMode.AI_COOP
        };
    }
    

    
    @Override
    public java.util.Map<GameMode, int[]> getSupportedPlayerCounts() {
        java.util.Map<GameMode, int[]> playerCounts = new java.util.HashMap<>();
        
        // Example game supports various player counts for different modes
        playerCounts.put(GameMode.SINGLE_PLAYER, new int[]{1, 1});
        playerCounts.put(GameMode.PRACTICE, new int[]{1, 1});
        playerCounts.put(GameMode.LOCAL_MULTIPLAYER, new int[]{2, 4});
        playerCounts.put(GameMode.HOT_SEAT, new int[]{2, 4});
        playerCounts.put(GameMode.PUZZLE, new int[]{1, 2});
        playerCounts.put(GameMode.CREATIVE, new int[]{1, 4});
        playerCounts.put(GameMode.AI_VERSUS, new int[]{1, 1});
        playerCounts.put(GameMode.AI_COOP, new int[]{1, 1});
        
        return playerCounts;
    }
    
    @Override
    public GameMode getDefaultGameMode() {
        return GameMode.SINGLE_PLAYER;
    }
    

    
    @Override
    public int getDefaultPlayerCount(GameMode gameMode) {
        // Example game defaults to 1 player for single player modes, 2 for multiplayer
        if (gameMode == GameMode.LOCAL_MULTIPLAYER || gameMode == GameMode.HOT_SEAT) {
            return 2;
        }
        return 1; // Single player modes default to 1
    }
    
    @Override
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions, Object eventHandler) {
        Logging.info("🎮 Launching " + getGameName() + " with mode: " + gameMode.getDisplayName() + ", players: " + playerCount);
        
        try {
            // Create a simple test interface instead of loading FXML
            return createTestInterface(primaryStage, gameMode, playerCount, gameOptions, eventHandler);
            
        } catch (Exception e) {
            Logging.error("❌ Failed to launch " + getGameName() + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Creates a simple test interface to demonstrate game communication.
     */
    private Scene createTestInterface(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions, Object eventHandler) {
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("🧪 Game Communication Test");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
        
        // Game info
        String difficulty = (gameOptions != null) ? gameOptions.getStringOption("difficulty", "Medium") : "Medium";
        javafx.scene.control.Label infoLabel = new javafx.scene.control.Label(
            "Game: " + getGameName() + " | Mode: " + gameMode.getDisplayName() + " | Players: " + playerCount + " | Difficulty: " + difficulty
        );
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057;");
        
        // Test buttons
        javafx.scene.control.Button startButton = new javafx.scene.control.Button("🚀 Start Game");
        startButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        startButton.setOnAction(e -> {
            // Event system removed - use server simulator for communication
        });
        
        javafx.scene.control.Button moveButton = new javafx.scene.control.Button("🎯 Make Move");
        moveButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        moveButton.setOnAction(e -> {
            // Event system removed - use server simulator for communication
        });
        
        javafx.scene.control.Button turnButton = new javafx.scene.control.Button("🔄 Change Turn");
        turnButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-padding: 10 20; -fx-cursor: hand;");
        turnButton.setOnAction(e -> {
            // Event system removed - use server simulator for communication
        });
        
        javafx.scene.control.Button messageButton = new javafx.scene.control.Button("💬 Send Message");
        messageButton.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        messageButton.setOnAction(e -> {
            // Event system removed - use server simulator for communication
        });
        
        javafx.scene.control.Button errorButton = new javafx.scene.control.Button("❌ Simulate Error");
        errorButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        errorButton.setOnAction(e -> {
            // Event system removed - use server simulator for communication
        });
        
        javafx.scene.control.Button endButton = new javafx.scene.control.Button("🏁 End Game");
        endButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        endButton.setOnAction(e -> {
            // Event system removed - use server simulator for communication
        });
        

        
        javafx.scene.control.Button gameModeButton = new javafx.scene.control.Button("🎮 Show Game Mode Info");
        gameModeButton.setStyle("-fx-background-color: #20c997; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        gameModeButton.setOnAction(e -> {
            Logging.info("🎮 Game Mode Info: " + gameMode.getDisplayName());
            
            String message = "Current Game Mode: " + gameMode.getDisplayName() + 
                           "\nDescription: " + gameMode.getDescription() +
                           "\nCategory: " + gameMode.getCategory() +
                           "\nIcon: " + gameMode.getIcon() +
                           "\nColor: " + gameMode.getColorCode();
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Game Mode Information");
            alert.setHeaderText("Game Mode Details");
            alert.setContentText(message);
            alert.showAndWait();
        });
        
        // JSON Data button
        javafx.scene.control.Button jsonDataButton = new javafx.scene.control.Button("📦 Show JSON Data");
        jsonDataButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        jsonDataButton.setOnAction(e -> {
            Logging.info("📦 Checking for custom JSON data...");
            
            if (gameOptions != null && gameOptions.hasOption("customData")) {
                Object customData = gameOptions.getOption("customData", null);
                Logging.info("📦 Found custom data: " + customData);
                
                String message = "Custom JSON Data Received:\n\n" + customData.toString();
                
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("JSON Data Information");
                alert.setHeaderText("Custom Data from Launcher");
                alert.setContentText(message);
                alert.showAndWait();
                
                // Event system removed - use server simulator for communication
            } else {
                Logging.info("📦 No custom JSON data found");
                
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("JSON Data Information");
                alert.setHeaderText("No Custom Data");
                alert.setContentText("No custom JSON data was provided when launching this game.");
                alert.showAndWait();
            }
        });
        
        javafx.scene.control.Button backButton = new javafx.scene.control.Button("🔙 Back to Lobby");
        backButton.setStyle("-fx-background-color: #fd7e14; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        backButton.setOnAction(e -> {
            Logging.info("🔙 Back to Lobby button clicked in Example Game");
            // Event system removed - use server simulator for communication
        });
        
        // Instructions
        javafx.scene.control.Label instructionsLabel = new javafx.scene.control.Label(
            "Click buttons to test different game events. Check the GDK log to see events being received."
        );
        instructionsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-wrap-text: true;");
        instructionsLabel.setMaxWidth(400);
        
        // Add all components
        root.getChildren().addAll(
            titleLabel, infoLabel,
            startButton, moveButton, turnButton, messageButton, errorButton, endButton, gameModeButton, jsonDataButton, backButton,
            instructionsLabel
        );
        
        // Event system removed - use server simulator for communication
        
        return new javafx.scene.Scene(root, 450, 500);
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
    
    // ==================== CUSTOM SETTINGS ====================
    
    @Override
    public GameSettings getCustomSettings() {
        return new ExampleGameSettings();
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
            String exampleOption = (gameOptions != null) ? gameOptions.getStringOption("exampleOption", "default") : "default";
            Logging.info("⚙️ Example option: " + exampleOption);
        }
    }
} 