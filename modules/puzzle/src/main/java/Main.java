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
 * Puzzle game module implementation.
 * A simple sliding puzzle demonstration.
 *
 * @authors Clement Luo
 * @date August 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    private static final String GAME_ID = "puzzle";
    private final Metadata metadata;
    
    /**
     * Constructor for Main.
     */
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        Logging.info("🧩 Launching Sliding Puzzle Game");
        try {
            return createGameScene(primaryStage);
        } catch (Exception e) {
            Logging.error("❌ Failed to launch Sliding Puzzle Game: " + e.getMessage(), e);
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
            Logging.info("📋 Returning metadata for Sliding Puzzle Game");
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
     * Creates the main game scene for Sliding Puzzle
     */
    private Scene createGameScene(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1e3a8a; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        Label titleLabel = new Label("🧩 Sliding Puzzle");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");
        
        // Game info
        Label infoLabel = new Label("Arrange the tiles in numerical order");
        infoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #93c5fd;");
        
        // Puzzle grid
        GridPane puzzleGrid = new GridPane();
        puzzleGrid.setHgap(5);
        puzzleGrid.setVgap(5);
        puzzleGrid.setAlignment(Pos.CENTER);
        
        // Create 3x3 puzzle tiles
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int number = row * 3 + col + 1;
                Button tile = new Button(number == 9 ? "" : String.valueOf(number));
                tile.setMinSize(60, 60);
                tile.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: #3b82f6; -fx-text-fill: white; -fx-border-color: #1d4ed8; -fx-border-width: 2px;");
                
                if (number == 9) {
                    tile.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: #1e40af; -fx-text-fill: #1e40af; -fx-border-color: #1d4ed8; -fx-border-width: 2px;");
                }
                
                tile.setOnAction(e -> {
                    Logging.info("🧩 Tile clicked: " + tile.getText());
                });
                
                puzzleGrid.add(tile, col, row);
            }
        }
        
        // Controls
        VBox controls = new VBox(10);
        controls.setAlignment(Pos.CENTER);
        
        Button shuffleButton = new Button("🔀 Shuffle");
        shuffleButton.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10px 20px;");
        
        Button backButton = new Button("🔙 Back to Lobby");
        backButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        controls.getChildren().addAll(shuffleButton, backButton);
        
        // Event handlers
        shuffleButton.setOnAction(e -> {
            Logging.info("🔀 Shuffling puzzle");
        });
        
        backButton.setOnAction(e -> {
            Logging.info("🔙 Returning to lobby from Sliding Puzzle Game");
            stopGame();
        });
        
        // Add components to root
        root.getChildren().addAll(titleLabel, infoLabel, puzzleGrid, controls);
        
        // Create scene
        Scene scene = new Scene(root, 400, 600);
        
        // Configure stage
        primaryStage.setTitle("Sliding Puzzle");
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(600);
        
        Logging.info("✅ Sliding Puzzle game scene created successfully");
        return scene;
    }
} 