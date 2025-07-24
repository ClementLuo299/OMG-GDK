package example;

import gdk.GameModule;
import gdk.Logging;
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
    public Scene launchGame(Stage primaryStage, int playerCount, Object eventHandler) {
        Logging.info("🎮 Launching " + getGameName() + " with players: " + playerCount);
        
        try {
            // Create a simple test interface instead of loading FXML
            return createTestInterface(primaryStage, playerCount, eventHandler);
            
        } catch (Exception e) {
            Logging.error("❌ Failed to launch " + getGameName() + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Creates a simple test interface to demonstrate game communication.
     */
    private Scene createTestInterface(Stage primaryStage, int playerCount, Object eventHandler) {
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("�� Game Communication Test");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
        
        // Game info
        String difficulty = "Medium"; // Default difficulty
        javafx.scene.control.Label infoLabel = new javafx.scene.control.Label(
            "Game: " + getGameName() + " | Players: " + playerCount + " | Difficulty: " + difficulty
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
        

        
        // JSON Data button
        javafx.scene.control.Button jsonDataButton = new javafx.scene.control.Button("📦 Show JSON Data");
        jsonDataButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        jsonDataButton.setOnAction(e -> {
            Logging.info("📦 Checking for custom JSON data...");
            
            // No custom data available
            Logging.info("📦 No custom JSON data found");
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("JSON Data Information");
            alert.setHeaderText("No Custom Data");
            alert.setContentText("No custom JSON data was provided when launching this game.");
            alert.showAndWait();
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
            startButton, moveButton, turnButton, messageButton, errorButton, endButton, jsonDataButton, backButton,
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
    
    /**
     * Example game controller (placeholder).
     * In a real implementation, this would be a separate controller class.
     */
    public static class ExampleGameController {
        
        public void initializeGame(int playerCount) {
            Logging.info("🎯 Initializing Example Game with " + playerCount + " players");
            
            // Example game initialization logic would go here
            Logging.info("⚙️ Example option: default");
        }
    }
} 