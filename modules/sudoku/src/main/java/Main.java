import gdk.GameModule;
import gdk.Logging;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * Sudoku game module implementation.
 * A simple sudoku game demonstration.
 *
 * @authors Clement Luo
 * @date August 2025
 * @since 1.0
 */
public class Main implements GameModule {
    
    private static final String GAME_ID = "sudoku";
    private final Metadata metadata;
    
    /**
     * Constructor for Main.
     */
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        Logging.info("🔢 Launching Sudoku Game");
        try {
            return createGameScene(primaryStage);
        } catch (Exception e) {
            Logging.error("❌ Failed to launch Sudoku Game: " + e.getMessage(), e);
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
            Logging.info("📋 Returning metadata for Sudoku Game");
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
     * Creates the main game scene for Sudoku
     */
    private Scene createGameScene(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1f2937; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        Label titleLabel = new Label("🔢 Sudoku");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
        
        // Game info
        Label infoLabel = new Label("Fill the grid with numbers 1-9");
        infoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b7280;");
        
        // Sudoku grid
        GridPane sudokuGrid = new GridPane();
        sudokuGrid.setHgap(1);
        sudokuGrid.setVgap(1);
        sudokuGrid.setAlignment(Pos.CENTER);
        sudokuGrid.setStyle("-fx-background-color: #374151; -fx-padding: 10px; -fx-border-color: #10b981; -fx-border-width: 3px;");
        
        // Create 9x9 sudoku grid
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                TextField cell = new TextField();
                cell.setMinSize(40, 40);
                cell.setMaxSize(40, 40);
                cell.setAlignment(Pos.CENTER);
                cell.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-border-color: #d1d5db; -fx-border-width: 1px;");
                
                // Add thicker borders for 3x3 sub-grids
                if (row % 3 == 0) {
                    cell.setStyle(cell.getStyle() + " -fx-border-top-width: 2px;");
                }
                if (col % 3 == 0) {
                    cell.setStyle(cell.getStyle() + " -fx-border-left-width: 2px;");
                }
                if (row == 8) {
                    cell.setStyle(cell.getStyle() + " -fx-border-bottom-width: 2px;");
                }
                if (col == 8) {
                    cell.setStyle(cell.getStyle() + " -fx-border-right-width: 2px;");
                }
                
                // Add some sample numbers
                if ((row == 0 && col == 0) || (row == 1 && col == 3) || (row == 2 && col == 6)) {
                    cell.setText("5");
                    cell.setEditable(false);
                    cell.setStyle(cell.getStyle() + " -fx-background-color: #e5e7eb;");
                }
                
                cell.setOnAction(e -> {
                    Logging.info("🔢 Cell filled: " + cell.getText());
                });
                
                sudokuGrid.add(cell, col, row);
            }
        }
        
        // Controls
        VBox controls = new VBox(10);
        controls.setAlignment(Pos.CENTER);
        
        Button checkButton = new Button("✅ Check Solution");
        checkButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10px 20px;");
        
        Button newGameButton = new Button("🆕 New Game");
        newGameButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10px 20px;");
        
        Button backButton = new Button("🔙 Back to Lobby");
        backButton.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        
        controls.getChildren().addAll(checkButton, newGameButton, backButton);
        
        // Event handlers
        checkButton.setOnAction(e -> {
            Logging.info("✅ Checking sudoku solution");
        });
        
        newGameButton.setOnAction(e -> {
            Logging.info("🆕 Starting new sudoku game");
        });
        
        backButton.setOnAction(e -> {
            Logging.info("🔙 Returning to lobby from Sudoku Game");
            stopGame();
        });
        
        // Add components to root
        root.getChildren().addAll(titleLabel, infoLabel, sudokuGrid, controls);
        
        // Create scene
        Scene scene = new Scene(root, 500, 700);
        
        // Configure stage
        primaryStage.setTitle("Sudoku");
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(700);
        
        Logging.info("✅ Sudoku game scene created successfully");
        return scene;
    }
} 