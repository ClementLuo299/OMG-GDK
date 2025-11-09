

import gdk.api.GameModule;
import gdk.infrastructure.Logging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;


/**
 * TicTacToe game module implementation.
 * Demonstrates how to create a game module that integrates with the main application.
 *
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    private static final String GAME_ID = "tictactoe";
    private final Metadata metadata;
    
    /**
     * Constructor for Main.
     */
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        Logging.info("🎮 Launching TicTacToe Game");
        try {
            return createGameScene(primaryStage);
        } catch (Exception e) {
            Logging.error("❌ Failed to launch TicTacToe Game: " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void stopGame() {
        Logging.info("🔄 " + metadata.getGameName() + " closing - cleaning up resources");
    }
    
    @Override
    public java.util.Map<String, Object> handleMessage(java.util.Map<String, Object> message) {
        if (message == null) {
            return null;
        }
        
        String function = (String) message.get("function");
        if ("metadata".equals(function)) {
            Logging.info("📋 Returning metadata for TicTacToe Game");
            return metadata.toMap();
        }
        
        return null;
    }
    
    @Override
    public Metadata getMetadata() {
        return metadata;
    }
    
    // ==================== PRIVATE METHODS ====================
    
    /**
     * Creates the main game scene for TicTacToe
     */
    private Scene createGameScene(Stage primaryStage) {
        try {
            // Load the FXML file
            URL fxmlUrl = getClass().getResource("/games/tictactoe/fxml/tictactoe.fxml");
            if (fxmlUrl == null) {
                Logging.warning("⚠️ FXML file not found, creating simple interface");
                return createSimpleGameScene(primaryStage);
            }
            
            // Load the FXML and create the scene
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load());
            
            // Apply CSS styling
            URL cssUrl = getClass().getResource("/games/tictactoe/css/tictactoe.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Get the controller and set up references
            TicTacToeController controller = loader.getController();
            if (controller != null) {
                controller.setGameModule(this);
                Logging.info("✅ TicTacToe controller initialized successfully");
            }
            
            // Configure stage
            primaryStage.setTitle("Tic Tac Toe");
            primaryStage.setMinWidth(400);
            primaryStage.setMinHeight(500);
            
            Logging.info("✅ TicTacToe game scene created successfully");
            return scene;
            
        } catch (Exception e) {
            Logging.error("❌ Failed to load TicTacToe FXML: " + e.getMessage(), e);
            return createSimpleGameScene(primaryStage);
        }
    }
    
    /**
     * Creates a simple game scene as fallback
     */
    private Scene createSimpleGameScene(Stage primaryStage) {
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Tic Tac Toe");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
        
        // Game info
        javafx.scene.control.Label infoLabel = new javafx.scene.control.Label(
            "Classic 3x3 grid game for two players"
        );
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");
        
        // Status
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label("🎮 TicTacToe is running!");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
        
        // Simple game grid (3x3 buttons)
        javafx.scene.layout.GridPane gameGrid = new javafx.scene.layout.GridPane();
        gameGrid.setHgap(5);
        gameGrid.setVgap(5);
        gameGrid.setAlignment(javafx.geometry.Pos.CENTER);
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                javafx.scene.control.Button cell = new javafx.scene.control.Button("");
                cell.setMinSize(60, 60);
                cell.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
                cell.setOnAction(e -> {
                    if (cell.getText().isEmpty()) {
                        cell.setText("X");
                        cell.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
                    }
                });
                gameGrid.add(cell, col, row);
            }
        }
        
        // Buttons
        javafx.scene.control.Button resetButton = new javafx.scene.control.Button("🔄 Reset Game");
        resetButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        javafx.scene.control.Button backButton = new javafx.scene.control.Button("🔙 Back to Lobby");
        backButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        javafx.scene.control.Button closeButton = new javafx.scene.control.Button("❌ Close Game");
        closeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        // Event handlers
        resetButton.setOnAction(e -> {
            Logging.info("🔄 Resetting TicTacToe game");
            for (javafx.scene.Node node : gameGrid.getChildren()) {
                if (node instanceof javafx.scene.control.Button) {
                    ((javafx.scene.control.Button) node).setText("");
                    node.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
                }
            }
        });
        
        closeButton.setOnAction(e -> {
            Logging.info("🔒 Closing TicTacToe Game");
            stopGame();
        });
        
        backButton.setOnAction(e -> {
            Logging.info("🔙 Returning to lobby from TicTacToe Game");
            stopGame();
        });
        
        // Add components to root
        root.getChildren().addAll(
            titleLabel,
            infoLabel,
            statusLabel,
            gameGrid,
            resetButton,
            backButton,
            closeButton
        );
        
        // Create scene
        Scene scene = new Scene(root, 400, 500);
        
        // Configure stage
        primaryStage.setTitle("Tic Tac Toe");
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(500);
        
        Logging.info("✅ TicTacToe simple interface created successfully");
        return scene;
    }
} 