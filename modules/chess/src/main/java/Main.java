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
 * Chess game module implementation.
 * A simple chess game demonstration.
 *
 * @authors Clement Luo
 * @date August 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    private static final String GAME_ID = "chess";
    private final Metadata metadata;
    
    /**
     * Constructor for Main.
     */
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        Logging.info("♟️ Launching Chess Game");
        try {
            return createGameScene(primaryStage);
        } catch (Exception e) {
            Logging.error("❌ Failed to launch Chess Game: " + e.getMessage(), e);
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
            Logging.info("📋 Returning metadata for Chess Game");
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
     * Creates the main game scene for Chess
     */
    private Scene createGameScene(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #78350f; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        Label titleLabel = new Label("♟️ Chess");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #fbbf24;");
        
        // Game info
        Label infoLabel = new Label("The game of kings - White to move");
        infoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #fcd34d;");
        
        // Chess board
        GridPane chessBoard = new GridPane();
        chessBoard.setHgap(2);
        chessBoard.setVgap(2);
        chessBoard.setAlignment(Pos.CENTER);
        
        // Create 8x8 chess board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Button square = new Button();
                square.setMinSize(50, 50);
                
                // Alternate colors
                boolean isLight = (row + col) % 2 == 0;
                String bgColor = isLight ? "#fef3c7" : "#92400e";
                String textColor = isLight ? "#92400e" : "#fef3c7";
                
                square.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-color: #78350f; -fx-border-width: 1px;");
                
                // Add some sample pieces
                if (row == 0 || row == 7) {
                    String piece = getPieceSymbol(row, col);
                    square.setText(piece);
                }
                
                square.setOnAction(e -> {
                    Logging.info("♟️ Square clicked: " + square.getText());
                });
                
                chessBoard.add(square, col, row);
            }
        }
        
        // Controls
        VBox controls = new VBox(10);
        controls.setAlignment(Pos.CENTER);
        
        Button newGameButton = new Button("🆕 New Game");
        newGameButton.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10px 20px;");
        
        Button backButton = new Button("🔙 Back to Lobby");
        backButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        controls.getChildren().addAll(newGameButton, backButton);
        
        // Event handlers
        newGameButton.setOnAction(e -> {
            Logging.info("🆕 Starting new chess game");
        });
        
        backButton.setOnAction(e -> {
            Logging.info("🔙 Returning to lobby from Chess Game");
            stopGame();
        });
        
        // Add components to root
        root.getChildren().addAll(titleLabel, infoLabel, chessBoard, controls);
        
        // Create scene
        Scene scene = new Scene(root, 500, 700);
        
        // Configure stage
        primaryStage.setTitle("Chess");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(700);
        
        Logging.info("✅ Chess game scene created successfully");
        return scene;
    }
    
    /**
     * Get piece symbol for chess board setup
     */
    private String getPieceSymbol(int row, int col) {
        String[] pieces = {"♜", "♞", "♝", "♛", "♚", "♝", "♞", "♜"};
        if (row == 0) {
            return pieces[col];
        } else if (row == 7) {
            return pieces[col];
        }
        return "";
    }
} 