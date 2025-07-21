package com.games.modules.example;

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
    public GameDifficulty getDifficulty() {
        return GameDifficulty.MEDIUM;
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
    
    @Override
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions, com.game.GameEventHandler eventHandler) {
        Logging.info("🎮 Launching " + getGameName() + " with mode: " + gameMode.getDisplayName() + ", players: " + playerCount);
        
        try {
            // Create a simple test interface instead of loading FXML
            return createTestInterface(primaryStage, gameMode, playerCount, gameOptions, eventHandler);
            
        } catch (Exception e) {
            Logging.error("❌ Failed to launch " + getGameName() + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Creates a simple test interface to demonstrate game communication.
     */
    private Scene createTestInterface(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions, com.game.GameEventHandler eventHandler) {
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        
        // Title
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("🧪 Game Communication Test");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
        
        // Game info
        javafx.scene.control.Label infoLabel = new javafx.scene.control.Label(
            "Game: " + getGameName() + " | Mode: " + gameMode.getDisplayName() + " | Players: " + playerCount
        );
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057;");
        
        // Test buttons
        javafx.scene.control.Button startButton = new javafx.scene.control.Button("🚀 Start Game");
        startButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        startButton.setOnAction(e -> {
            eventHandler.handleGameEvent(new com.game.GameEvent(
                com.game.GameEvent.EventType.GAME_STARTED,
                getGameId(),
                "Example game started with " + playerCount + " players in " + gameMode.getDisplayName() + " mode"
            ));
        });
        
        javafx.scene.control.Button moveButton = new javafx.scene.control.Button("🎯 Make Move");
        moveButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        moveButton.setOnAction(e -> {
            eventHandler.handleGameEvent(new com.game.GameEvent(
                com.game.GameEvent.EventType.MOVE_MADE,
                getGameId(),
                "Player made a test move",
                "Test move data"
            ));
        });
        
        javafx.scene.control.Button turnButton = new javafx.scene.control.Button("🔄 Change Turn");
        turnButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-padding: 10 20; -fx-cursor: hand;");
        turnButton.setOnAction(e -> {
            eventHandler.handleGameEvent(new com.game.GameEvent(
                com.game.GameEvent.EventType.PLAYER_TURN_CHANGED,
                getGameId(),
                "Turn changed to next player"
            ));
        });
        
        javafx.scene.control.Button messageButton = new javafx.scene.control.Button("💬 Send Message");
        messageButton.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        messageButton.setOnAction(e -> {
            eventHandler.handleGameEvent(new com.game.GameEvent(
                com.game.GameEvent.EventType.MOVE_MADE,
                getGameId(),
                "Test message from example game: " + java.time.LocalTime.now()
            ));
        });
        
        javafx.scene.control.Button errorButton = new javafx.scene.control.Button("❌ Simulate Error");
        errorButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        errorButton.setOnAction(e -> {
            eventHandler.handleGameEvent(new com.game.GameEvent(
                com.game.GameEvent.EventType.ERROR_OCCURRED,
                getGameId(),
                "Simulated error for testing error handling"
            ));
        });
        
        javafx.scene.control.Button endButton = new javafx.scene.control.Button("🏁 End Game");
        endButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        endButton.setOnAction(e -> {
            eventHandler.handleGameEvent(new com.game.GameEvent(
                com.game.GameEvent.EventType.GAME_ENDED,
                getGameId(),
                "Example game ended - test completed"
            ));
        });
        
        javafx.scene.control.Button backButton = new javafx.scene.control.Button("🔙 Back to Lobby");
        backButton.setStyle("-fx-background-color: #fd7e14; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        backButton.setOnAction(e -> {
            Logging.info("🔙 Back to Lobby button clicked in Example Game");
            eventHandler.handleGameEvent(new com.game.GameEvent(
                com.game.GameEvent.EventType.BACK_TO_LOBBY_REQUESTED,
                getGameId(),
                "User requested to return to lobby"
            ));
            Logging.info("🔙 BACK_TO_LOBBY_REQUESTED event sent from Example Game");
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
            startButton, moveButton, turnButton, messageButton, errorButton, endButton, backButton,
            instructionsLabel
        );
        
        // Send game started event immediately
        eventHandler.handleGameEvent(new com.game.GameEvent(
            com.game.GameEvent.EventType.GAME_STARTED,
            getGameId(),
            "Example game test interface loaded"
        ));
        
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
    
    @Override
    public GameState getGameState() {
        GameOptions options = new GameOptions();
        options.setOption("exampleOption", "exampleValue");
        options.setOption("difficulty", "medium");
        
        GameState gameState = new GameState(GAME_ID, GAME_NAME, GameMode.LOCAL_MULTIPLAYER, 2, options);
        
        // Add example state data
        gameState.setStateValue("exampleData", "This is example game data");
        gameState.setStateValue("score", 0);
        
        return gameState;
    }
    
    @Override
    public void loadGameState(GameState gameState) {
        Logging.info("📂 Loading Example Game state");
        
        if (gameState != null) {
            String exampleData = gameState.getStringStateValue("exampleData", "No data");
            int score = gameState.getIntStateValue("score", 0);
            
            Logging.info("📊 Loaded example game state - Data: " + exampleData + ", Score: " + score);
        }
    }
    
    /**
     * Example game controller (placeholder).
     * In a real implementation, this would be a separate controller class.
     */
    public static class ExampleGameController {
        
        public void initializeGame(GameMode gameMode, int playerCount, GameOptions gameOptions) {
            Logging.info("🎯 Initializing Example Game with " + playerCount + " players");
            Logging.info("🎮 Game mode: " + gameMode.getDisplayName());
            
            // Example game initialization logic would go here
            String exampleOption = gameOptions.getStringOption("exampleOption", "default");
            Logging.info("⚙️ Example option: " + exampleOption);
        }
    }
} 