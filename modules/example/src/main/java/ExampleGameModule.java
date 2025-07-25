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
    public Scene launchGame(Stage primaryStage) {
        Logging.info("🎮 Launching Example Game");
        try {
            return createTestInterface(primaryStage);
        } catch (Exception e) {
            Logging.error("❌ Failed to launch Example Game: " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void stopGame() {
        Logging.info("🔄 " + GAME_NAME + " closing - cleaning up resources");
    }
    
    // ==================== PRIVATE METHODS ====================
    
    /**
     * Creates a simple test interface to demonstrate game communication.
     */
    private Scene createTestInterface(Stage primaryStage) {
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("🎮 Game Communication Test");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
        
        // Game info
        int playerCount = 2; // Default for display
        String difficulty = "Medium"; // Default difficulty
        javafx.scene.control.Label infoLabel = new javafx.scene.control.Label(
            "Game: Example Game | Players: " + playerCount + " | Difficulty: " + difficulty
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
            Logging.info("🔒 Closing Example Game");
            stopGame();
            Platform.exit();
        });
        
        backButton.setOnAction(e -> {
            Logging.info("🔙 Returning to lobby from Example Game");
            stopGame();
        });
        
        // Add components to root
        root.getChildren().addAll(
            titleLabel,
            infoLabel,
            statusLabel,
            jsonDataButton,
            backButton,
            closeButton
        );
        
        // Create scene
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/games/example/css/example.css").toExternalForm());
        
        // Configure stage
        primaryStage.setTitle("Example Game - GDK Test");
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(300);
        
        Logging.info("✅ Example Game interface created successfully");
        return scene;
    }
} 