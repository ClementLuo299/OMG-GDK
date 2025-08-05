import gdk.GameModule;
import gdk.Logging;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * Memory game module implementation.
 * A simple card matching game demonstration.
 *
 * @authors Clement Luo
 * @date August 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    private static final String GAME_ID = "memory";
    private final Metadata metadata;
    
    /**
     * Constructor for Main.
     */
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        Logging.info("🧠 Launching Memory Match Game");
        try {
            return createGameScene(primaryStage);
        } catch (Exception e) {
            Logging.error("❌ Failed to launch Memory Match Game: " + e.getMessage(), e);
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
            Logging.info("📋 Returning metadata for Memory Match Game");
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
     * Creates the main game scene for Memory Match
     */
    private Scene createGameScene(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #581c87; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        Label titleLabel = new Label("🧠 Memory Match");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #c084fc;");
        
        // Game info
        Label infoLabel = new Label("Find matching pairs of cards");
        infoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #d8b4fe;");
        
        // Memory grid
        GridPane memoryGrid = new GridPane();
        memoryGrid.setHgap(5);
        memoryGrid.setVgap(5);
        memoryGrid.setAlignment(Pos.CENTER);
        
        // Card symbols for matching
        String[] symbols = {"🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼"};
        
        // Create 4x4 memory grid
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                Button card = new Button("?");
                card.setMinSize(80, 80);
                card.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color: #7c3aed; -fx-text-fill: white; -fx-border-color: #a855f7; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-radius: 10px;");
                
                // Assign symbols to cards (pairs)
                int index = row * 4 + col;
                String symbol = symbols[index / 2]; // Each symbol appears twice
                
                card.setOnAction(e -> {
                    Logging.info("🧠 Card clicked: " + symbol);
                    card.setText(symbol);
                    card.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-color: #f3e8ff; -fx-text-fill: #581c87; -fx-border-color: #a855f7; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-radius: 10px;");
                });
                
                memoryGrid.add(card, col, row);
            }
        }
        
        // Controls
        VBox controls = new VBox(10);
        controls.setAlignment(Pos.CENTER);
        
        Button newGameButton = new Button("🆕 New Game");
        newGameButton.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10px 20px;");
        
        Button backButton = new Button("🔙 Back to Lobby");
        backButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        controls.getChildren().addAll(newGameButton, backButton);
        
        // Event handlers
        newGameButton.setOnAction(e -> {
            Logging.info("🆕 Starting new memory game");
        });
        
        backButton.setOnAction(e -> {
            Logging.info("🔙 Returning to lobby from Memory Match Game");
            stopGame();
        });
        
        // Add components to root
        root.getChildren().addAll(titleLabel, infoLabel, memoryGrid, controls);
        
        // Create scene
        Scene scene = new Scene(root, 450, 600);
        
        // Configure stage
        primaryStage.setTitle("Memory Match");
        primaryStage.setMinWidth(450);
        primaryStage.setMinHeight(600);
        
        Logging.info("✅ Memory Match game scene created successfully");
        return scene;
    }
} 