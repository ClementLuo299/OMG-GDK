package example;

import gdk.GameModule;
import gdk.Logging;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Example Game Module - A simple game module for testing the GDK.
 * Demonstrates basic game module implementation and communication.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 24, 2025
 * @since 1.0
 */
public class ExampleGameModule implements GameModule {
    
    // ==================== GAME CONSTANTS ====================
    
    private static final String GAME_ID = "example";
    private static final String GAME_NAME = "Example Game";
    
    @Override
    public Scene launchGame(Stage primaryStage, int playerCount, Object eventHandler) {
        Logging.info("🎮 Launching " + GAME_NAME + " with players: " + playerCount);
        
        try {
            // Create a simple test interface instead of loading FXML
            return createTestInterface(primaryStage, playerCount, eventHandler);
            
        } catch (Exception e) {
            Logging.error("❌ Failed to launch " + GAME_NAME + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void onGameClose() {
        Logging.info("🔄 " + GAME_NAME + " closing - cleaning up resources");
    }
    
    // ==================== PRIVATE METHODS ====================
    
    /**
     * Creates a simple test interface to demonstrate game communication.
     */
    private Scene createTestInterface(Stage primaryStage, int playerCount, Object eventHandler) {
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(" Game Communication Test");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
        
        // Game info
        String difficulty = "Medium"; // Default difficulty
        javafx.scene.control.Label infoLabel = new javafx.scene.control.Label(
            "Game: " + GAME_NAME + " | Players: " + playerCount + " | Difficulty: " + difficulty
        );
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        
        // Status
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label("🎮 Game is running!");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
        
        // Buttons
        javafx.scene.control.Button jsonDataButton = new javafx.scene.control.Button("📦 Check JSON Data");
        jsonDataButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        javafx.scene.control.Button backButton = new javafx.scene.control.Button("🔙 Back to Lobby");
        backButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        // Event handlers
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
        
        javafx.scene.control.Button closeButton = new javafx.scene.control.Button("❌ Close Game");
        closeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        closeButton.setOnAction(e -> {
            Logging.info("🔒 Closing " + GAME_NAME);
            onGameClose();
            Platform.exit();
        });
        
        backButton.setOnAction(e -> {
            Logging.info("🔙 Returning to lobby from " + GAME_NAME);
            onGameClose();
            // The launcher will handle returning to the lobby
        });
        
        // Add components to root
        root.getChildren().addAll(
            titleLabel,
            infoLabel,
            statusLabel,
            new javafx.scene.control.Separator(),
            jsonDataButton,
            backButton,
            closeButton
        );
        
        // Create scene
        Scene scene = new Scene(root, 500, 400);
        scene.getStylesheets().add(getClass().getResource("/games/example/css/example.css").toExternalForm());
        
        Logging.info("✅ " + GAME_NAME + " test interface created successfully");
        return scene;
    }
} 