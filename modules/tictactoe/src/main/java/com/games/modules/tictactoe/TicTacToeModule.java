package com.games.modules.tictactoe;

import com.game.GameModule;
import com.game.enums.GameDifficulty;
import com.game.enums.GameMode;
import com.game.GameOptions;
import com.game.GameState;
import com.utils.error_handling.Logging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * TicTacToe game module implementation.
 * Demonstrates how to create a game module that integrates with the main application.
 *
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class TicTacToeModule implements GameModule {
    
    private static final String GAME_ID = "tictactoe";
    private static final String GAME_NAME = "Tic Tac Toe";
    private static final String GAME_DESCRIPTION = "Classic 3x3 grid game for two players";
    
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
        return 2;
    }
    
    @Override
    public int getMaxPlayers() {
        return 2;
    }
    
    @Override
    public int getEstimatedDuration() {
        return 5; // 5 minutes
    }
    
    @Override
    public GameDifficulty getDifficulty() {
        return GameDifficulty.EASY;
    }
    
    @Override
    public String getGameCategory() {
        return "Classic";
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
        return true; // vs AI
    }
    
    @Override
    public String getGameFxmlPath() {
        return "/games/tictactoe/fxml/tictactoe.fxml";
    }
    
    @Override
    public String getGameCssPath() {
        return "/games/tictactoe/css/tictactoe.css";
    }
    
    @Override
    public String getGameIconPath() {
        return "/games/tictactoe/icons/tic_tac_toe_icon.png";
    }
    
    @Override
    public void onGameClose() {
        Logging.info("🔄 " + getGameName() + " closing - cleaning up resources");
    }
    
    @Override
    public GameState getGameState() {
        // This would be implemented to save the current game state
        // For now, return a basic game state
        GameOptions options = new GameOptions();
        options.setOption("boardSize", 3);
        options.setOption("aiDifficulty", "medium");
        options.setOption("timeLimit", 30);
        
        GameState gameState = new GameState(GAME_ID, GAME_NAME, GameMode.LOCAL_MULTIPLAYER, 2, options);
        
        // Add game-specific state data
        gameState.setStateValue("currentPlayer", "X");
        gameState.setStateValue("movesCount", 0);
        gameState.setStateValue("gameBoard", new String[3][3]); // 3x3 board
        
        return gameState;
    }
    
    @Override
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions) {
        Logging.info("🎮 Launching " + getGameName() + " with mode: " + gameMode.getDisplayName() + ", players: " + playerCount);
        
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(getGameFxmlPath()));
            Scene scene = new Scene(loader.load());
            
            // Get the controller and initialize it
            TicTacToeController controller = loader.getController();
            if (controller != null) {
                controller.setPrimaryStage(primaryStage);
                controller.initializeGame(gameMode, playerCount, gameOptions);
            }
            
            // Apply CSS if available
            try {
                String cssPath = getGameCssPath();
                if (cssPath != null && !cssPath.isEmpty()) {
                    scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                }
            } catch (Exception e) {
                Logging.warning("⚠️ Could not load CSS for " + getGameName() + ": " + e.getMessage());
            }
            
            Logging.info("✅ " + getGameName() + " launched successfully");
            return scene;
            
        } catch (Exception e) {
            Logging.error("❌ Failed to launch " + getGameName() + ": " + e.getMessage(), e);
            
            // Fallback to simple game scene
            return createSimpleGameScene(primaryStage, gameMode, playerCount, gameOptions);
        }
    }
    
    /**
     * Creates a simple game scene for TicTacToe as fallback.
     */
    private Scene createSimpleGameScene(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions) {
        Logging.info("🎮 Creating simple TicTacToe game scene (fallback)");
        
        // Create a simple game board
        javafx.scene.layout.GridPane gameBoard = new javafx.scene.layout.GridPane();
        gameBoard.setHgap(5);
        gameBoard.setVgap(5);
        gameBoard.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Create 3x3 grid of buttons
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                javafx.scene.control.Button button = new javafx.scene.control.Button("");
                button.setPrefSize(80, 80);
                button.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
                
                final int finalRow = row;
                final int finalCol = col;
                button.setOnAction(e -> {
                    if (button.getText().isEmpty()) {
                        button.setText("X");
                        Logging.info("🎮 Player X placed at [" + finalRow + "," + finalCol + "]");
                    }
                });
                
                gameBoard.add(button, col, row);
            }
        }
        
        // Create title
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Tic Tac Toe");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 20px;");
        
        // Create back button
        javafx.scene.control.Button backButton = new javafx.scene.control.Button("← Back to GDK");
        backButton.setOnAction(e -> {
            Logging.info("🔄 Returning to GDK from " + getGameName());
            primaryStage.close();
        });
        
        // Create main layout
        javafx.scene.layout.VBox mainLayout = new javafx.scene.layout.VBox(20);
        mainLayout.setAlignment(javafx.geometry.Pos.CENTER);
        mainLayout.setPadding(new javafx.geometry.Insets(20));
        mainLayout.getChildren().addAll(titleLabel, gameBoard, backButton);
        
        Scene scene = new Scene(mainLayout, 400, 500);
        
        Logging.info("✅ Simple " + getGameName() + " scene created successfully");
        return scene;
    }
    
    @Override
    public void loadGameState(GameState gameState) {
        Logging.info("📂 Loading TicTacToe game state");
        
        if (gameState != null) {
            // Load game-specific state data
            String currentPlayer = gameState.getStringStateValue("currentPlayer", "X");
            int movesCount = gameState.getIntStateValue("movesCount", 0);
            
            Logging.info("📊 Loaded game state - Current player: " + currentPlayer + ", Moves: " + movesCount);
        }
    }
} 