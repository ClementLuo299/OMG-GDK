import gdk.GameModule;
import gdk.Logging;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * Snake game module implementation.
 * A simple snake game demonstration.
 *
 * @authors Clement Luo
 * @date August 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    private static final String GAME_ID = "snake";
    private final Metadata metadata;
    
    /**
     * Constructor for Main.
     */
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        Logging.info("🐍 Launching Snake Game");
        try {
            return createGameScene(primaryStage);
        } catch (Exception e) {
            Logging.error("❌ Failed to launch Snake Game: " + e.getMessage(), e);
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
            Logging.info("📋 Returning metadata for Snake Game");
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
     * Creates the main game scene for Snake
     */
    private Scene createGameScene(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2d5a27; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        // Title
        Label titleLabel = new Label("🐍 Snake Game");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #4ade80;");

        // Game info
        Label infoLabel = new Label("Use arrow keys to control the snake");
        infoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #a7f3d0;");

        // Game area placeholder
        Label gameArea = new Label("Game Area\n(Click to start)");
        gameArea.setStyle("-fx-font-size: 18px; -fx-text-fill: #86efac; -fx-background-color: #1b4332; -fx-padding: 40px; -fx-border-color: #4ade80; -fx-border-width: 2px;");
        gameArea.setAlignment(Pos.CENTER);
        gameArea.setMinSize(300, 200);

        // Controls
        VBox controls = new VBox(10);
        controls.setAlignment(Pos.CENTER);

        Button startButton = new Button("🎮 Start Game");
        startButton.setStyle("-fx-background-color: #4ade80; -fx-text-fill: #064e3b; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10px 20px;");

        Button backButton = new Button("🔙 Back to Lobby");
        backButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");

        controls.getChildren().addAll(startButton, backButton);

        // Event handlers
        startButton.setOnAction(e -> {
            Logging.info("🎮 Starting Snake game");
            gameArea.setText("Game Started!\nSnake is moving...");
        });

        backButton.setOnAction(e -> {
            Logging.info("🔙 Returning to lobby from Snake Game");
            stopGame();
        });

        // Add components to root
        root.getChildren().addAll(titleLabel, infoLabel, gameArea, controls);

        // Create scene
        Scene scene = new Scene(root, 500, 600);

        // Configure stage
        primaryStage.setTitle("Snake");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(600);

        Logging.info("✅ Snake game scene created successfully");
        return scene;
    }
} 