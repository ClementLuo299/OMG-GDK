import com.gdk.shared.game.GameModule;
import com.gdk.shared.game.GameMode;
import com.gdk.shared.game.GameOptions;
import com.gdk.shared.game.GameEvent;
import com.gdk.shared.game.GameEventHandler;
import com.gdk.shared.utils.ModuleLoader;
import com.gdk.shared.utils.error_handling.Logging;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.List;
import java.net.URL;
import java.io.File;

/**
 * Simple Game Development Kit (GDK) Application.
 * Allows developers to easily run and test game modules.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited July 22, 2025
 * @since 1.0
 */
public class GDKApplication extends Application {

    // Game modules directory
    private static final String MODULES_DIR = "../modules";
    
    // Configuration values - hardcoded for simplicity
    private static String username = "GDK Developer";
    private static String serverUrl = "localhost";
    private static int serverPort = 8080;
    
    private ComboBox<GameModule> gameSelector;
    private ComboBox<GameMode> gameModeSelector;
    private Spinner<Integer> playerCountSpinner;
    private TextArea logArea;
    private Stage primaryStage;
    private Scene lobbyScene;
    private GameModule currentGame;
    private boolean gameIsRunning = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            // Create and configure the UI
            createUI();
            
            // Configure the stage
            primaryStage.setTitle("OMG Game Development Kit");
            primaryStage.setWidth(1200);
            primaryStage.setHeight(900);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
            
            // Show the stage
            primaryStage.show();
            
            // Load available games
            refreshGameList();
            
            Logging.info("✅ GDK started successfully");
            
        } catch (Exception e) {
            Logging.error("❌ Failed to start GDK: " + e.getMessage(), e);
            showError("Startup Error", "Failed to start GDK: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        Logging.info("🔄 GDK shutting down");
    }

    /**
     * Creates the main UI.
     */
    private void createUI() {
        try {
            // Load the GDK Game Lobby FXML with more robust error handling
            URL fxmlUrl = GDKApplication.class.getResource("/GDKGameLobby.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("FXML resource not found: /GDKGameLobby.fxml");
            }
            
            Logging.info("📂 Loading FXML from: " + fxmlUrl);
            
            // Try to load from classpath first, then fallback to file URL
            FXMLLoader loader;
            try {
                loader = new FXMLLoader(GDKApplication.class.getResource("/GDKGameLobby.fxml"));
                lobbyScene = new Scene(loader.load());
            } catch (Exception e) {
                Logging.warning("⚠️ Failed to load FXML from classpath, trying file path...");
                // Fallback to direct file loading
                File fxmlFile = new File("launcher/target/classes/GDKGameLobby.fxml");
                if (!fxmlFile.exists()) {
                    throw new RuntimeException("FXML file not found: " + fxmlFile.getAbsolutePath());
                }
                loader = new FXMLLoader(fxmlFile.toURI().toURL());
                lobbyScene = new Scene(loader.load());
            }
            
            // Apply GDK lobby CSS
            URL cssUrl = GDKApplication.class.getResource("/gdk-lobby.css");
            if (cssUrl != null) {
                lobbyScene.getStylesheets().add(cssUrl.toExternalForm());
                Logging.info("📂 CSS loaded from: " + cssUrl);
            }
            
            // Get the controller and set the primary stage
            GDKGameLobbyController controller = loader.getController();
            if (controller != null) {
                controller.setPrimaryStage(primaryStage);
                controller.setGDKApplication(this);
                Logging.info("✅ Controller initialized successfully");
            } else {
                Logging.warning("⚠️ Controller is null");
            }
            
            // Set the scene
            primaryStage.setScene(lobbyScene);
            
            Logging.info("✅ GDK Game Lobby loaded successfully - lobbyScene: " + (lobbyScene != null ? "created" : "null"));
            
        } catch (Exception e) {
            Logging.error("❌ Failed to load GDK Game Lobby: " + e.getMessage(), e);
            
            // Fallback to simple UI if FXML loading fails
            createSimpleFallbackUI();
        }
    }
    
    /**
     * Creates a simple fallback UI if FXML loading fails.
     */
    private void createSimpleFallbackUI() {
        VBox root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #212529;");

        // Title
        Label titleLabel = new Label("OMG Game Development Kit");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");

        // Welcome message
                    String username = GDKApplication.username;
        Label welcomeLabel = new Label("Welcome, " + username + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #495057;");

        // Game selection
        VBox gameSection = createGameSection();
        
        // Launch button
        Button launchButton = new Button("🚀 Launch Game");
        launchButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12px 24px; -fx-cursor: hand;");
        launchButton.setOnAction(e -> launchSelectedGame());

        // Settings button
        Button settingsButton = new Button("⚙️ Settings");
        settingsButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 8px 16px; -fx-cursor: hand;");
        settingsButton.setOnAction(e -> openSettings());

        // Log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #28a745; -fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        logArea.setPrefRowCount(10);

        // Add all components
        root.getChildren().addAll(
            titleLabel, welcomeLabel, gameSection, 
            new HBox(10, launchButton, settingsButton), logArea
        );

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }

    /**
     * Creates the game selection section.
     */
    private VBox createGameSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: #ffffff; -fx-padding: 15px; -fx-border-color: #dee2e6; -fx-border-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 5, 0, 0, 2);");

        Label sectionTitle = new Label("Game Module Selection");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #007bff;");

        // Game selector
        HBox gameRow = new HBox(10);
        Label gameLabel = new Label("Available Games:");
        gameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
        gameSelector = new ComboBox<>();
        gameSelector.setPrefWidth(300);
        gameSelector.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #212529; -fx-border-color: #ced4da;");
        
        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-cursor: hand;");
        refreshButton.setOnAction(e -> refreshGameList());
        
        gameRow.getChildren().addAll(gameLabel, gameSelector, refreshButton);

        // Game configuration
        GridPane configGrid = new GridPane();
        configGrid.setHgap(10);
        configGrid.setVgap(5);

        Label modeLabel = new Label("Game Mode:");
        modeLabel.setStyle("-fx-font-weight: bold;");
        gameModeSelector = new ComboBox<>();
        gameModeSelector.getItems().addAll(
            GameMode.SINGLE_PLAYER,
            GameMode.LOCAL_MULTIPLAYER,
            GameMode.ONLINE_MULTIPLAYER,
            GameMode.PRACTICE,
            GameMode.TUTORIAL,
            GameMode.HOT_SEAT,
            GameMode.RANKED,
            GameMode.CASUAL,
            GameMode.TIME_TRIAL,
            GameMode.SURVIVAL,
            GameMode.PUZZLE,
            GameMode.CREATIVE,
            GameMode.SANDBOX,
            GameMode.AI_VERSUS,
            GameMode.AI_COOP
        );
        gameModeSelector.setValue(GameMode.LOCAL_MULTIPLAYER);
        gameModeSelector.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");

        Label playerLabel = new Label("Player Count:");
        playerLabel.setStyle("-fx-font-weight: bold;");
        playerCountSpinner = new Spinner<>(1, 8, 2);
        playerCountSpinner.setEditable(true);
        playerCountSpinner.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");

        configGrid.add(modeLabel, 0, 0);
        configGrid.add(gameModeSelector, 1, 0);
        configGrid.add(playerLabel, 0, 1);
        configGrid.add(playerCountSpinner, 1, 1);

        section.getChildren().addAll(sectionTitle, gameRow, configGrid);
        return section;
    }

    /**
     * Refreshes the list of available games.
     */
    private void refreshGameList() {
        try {
            // Only log if we're using the fallback UI
            if (logArea != null) {
                logArea.appendText("🔄 Refreshing game modules...\n");
            }
            
            List<GameModule> modules = ModuleLoader.discoverModules(MODULES_DIR);
            
            // Only update UI if we're using the fallback UI
            if (gameSelector != null) {
                gameSelector.getItems().clear();
                gameSelector.getItems().addAll(modules);
            }
            
            if (logArea != null) {
                logArea.appendText("📦 Found " + modules.size() + " game modules\n");
                
                for (GameModule module : modules) {
                    logArea.appendText("✅ " + module.getGameName() + " - " + module.getGameDescription() + "\n");
                }
            }
            
        } catch (Exception e) {
            Logging.error("❌ Failed to refresh games: " + e.getMessage());
            if (logArea != null) {
                logArea.appendText("❌ Error: " + e.getMessage() + "\n");
            }
        }
    }

    /**
     * Launches a game with the specified parameters.
     * This method is called by the FXML controller.
     */
    public void launchGame(GameModule selectedGame, GameMode gameMode, int playerCount, String difficulty) {
        // Create default options and call the overloaded method
        GameOptions options = new GameOptions();
        options.setOption("serverUrl", serverUrl);
        options.setOption("serverPort", String.valueOf(serverPort));
        options.setOption("difficulty", difficulty);
        
        launchGame(selectedGame, gameMode, playerCount, difficulty, options);
    }
    
    public void launchGame(GameModule selectedGame, GameMode gameMode, int playerCount, String difficulty, GameOptions options) {
        if (selectedGame == null) {
            Logging.error("❌ No game selected for launch");
            return;
        }

        // Validate player count
        if (playerCount < selectedGame.getMinPlayers() || playerCount > selectedGame.getMaxPlayers()) {
            Logging.error("❌ Invalid player count: " + playerCount + " (supported: " + selectedGame.getMinPlayers() + "-" + selectedGame.getMaxPlayers() + ")");
            return;
        }

        // Log custom data if present
        if (options.hasOption("customData")) {
            Object customData = options.getOption("customData", null);
            Logging.info("📦 Launching with custom data: " + customData);
        }

        Logging.info("🚀 Launching " + selectedGame.getGameName() + " in " + gameMode.getDisplayName() + " mode with " + playerCount + " players (Difficulty: " + difficulty + ")");

        try {
            // Create game event handler that points to the GDK application
            GameEventHandler eventHandler = this::handleGameEvent;
            Logging.info("🎮 Created event handler for " + selectedGame.getGameName() + " - handler: " + (eventHandler != null ? "valid" : "null"));
            
            javafx.scene.Scene gameScene = selectedGame.launchGame(primaryStage, gameMode, playerCount, options, eventHandler);
            if (gameScene != null) {
                currentGame = selectedGame;
                gameIsRunning = true;
                primaryStage.setScene(gameScene);
                primaryStage.setTitle(selectedGame.getGameName() + " - GDK");
                Logging.info("🎮 Game launched successfully - GDK event handler connected");
                Logging.info("🎮 Current state - gameIsRunning: " + gameIsRunning + ", currentGame: " + (currentGame != null ? currentGame.getGameName() : "null"));
            } else {
                Logging.error("❌ Failed to launch game - null scene returned");
            }
        } catch (Exception e) {
            Logging.error("❌ Error launching game: " + e.getMessage());
        }
    }
    
    /**
     * Launches the selected game.
     */
    public void launchSelectedGame() {
        // Only work if we're using the fallback UI
        if (gameSelector == null || gameModeSelector == null || playerCountSpinner == null) {
            Logging.info("🎮 Game launching handled by FXML controller");
            return;
        }
        
        GameModule selectedGame = gameSelector.getValue();
        if (selectedGame == null) {
            showError("No Game Selected", "Please select a game to launch.");
            return;
        }

        GameMode gameMode = gameModeSelector.getValue();
        int playerCount = playerCountSpinner.getValue();

        // Validate player count
        if (playerCount < selectedGame.getMinPlayers() || playerCount > selectedGame.getMaxPlayers()) {
            showError("Invalid Player Count", 
                "This game supports " + selectedGame.getMinPlayers() + "-" + selectedGame.getMaxPlayers() + " players.");
            return;
        }

        // Create game options
        GameOptions options = new GameOptions();
        options.setOption("serverUrl", serverUrl);
        options.setOption("serverPort", String.valueOf(serverPort));

        if (logArea != null) {
            logArea.appendText("🚀 Launching " + selectedGame.getGameName() + " in " + gameMode.getDisplayName() + " mode with " + playerCount + " players\n");
        }

        try {
            // Create game event handler that points to the GDK application
            GameEventHandler eventHandler = this::handleGameEvent;
            
            javafx.scene.Scene gameScene = selectedGame.launchGame(primaryStage, gameMode, playerCount, options, eventHandler);
            if (gameScene != null) {
                currentGame = selectedGame;
                gameIsRunning = true;
                primaryStage.setScene(gameScene);
                primaryStage.setTitle(selectedGame.getGameName() + " - GDK");
                Logging.info("🎮 Game launched successfully - GDK event handler connected");
                if (logArea != null) {
                    logArea.appendText("✅ Game launched successfully\n");
                }
            } else {
                if (logArea != null) {
                    logArea.appendText("❌ Failed to launch game\n");
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error launching game: " + e.getMessage());
            if (logArea != null) {
                logArea.appendText("❌ Error: " + e.getMessage() + "\n");
            }
        }
    }

    /**
     * Opens the settings dialog.
     */
    private void openSettings() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("GDK Settings");
        dialog.setHeaderText("Configure GDK Settings");

        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(10));

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField(username);

        Label serverLabel = new Label("Server URL:");
        TextField serverField = new TextField(serverUrl);

        Label portLabel = new Label("Server Port:");
        Spinner<Integer> portSpinner = new Spinner<>(1, 65535, serverPort);

        content.getChildren().addAll(usernameLabel, usernameField, serverLabel, serverField, portLabel, portSpinner);

        dialog.getDialogPane().setContent(content);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                username = usernameField.getText();
                serverUrl = serverField.getText();
                serverPort = portSpinner.getValue();
                if (logArea != null) {
                    logArea.appendText("💾 Settings updated (in-memory only)\n");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Handles game events from running games.
     * This method is called by games to communicate state changes.
     */
    private void handleGameEvent(GameEvent event) {
        Logging.info("🎮 Game Event: " + event.getEventType() + " - " + event.getMessage());
        
        switch (event.getEventType()) {
            case BACK_TO_LOBBY_REQUESTED:
                Logging.info("🔙 GDK received BACK_TO_LOBBY_REQUESTED event");
                Logging.info("🔙 Current game state - gameIsRunning: " + gameIsRunning + ", currentGame: " + (currentGame != null ? currentGame.getGameName() : "null"));
                returnToLobby();
                break;
            case GAME_ENDED:
                gameIsRunning = false;
                currentGame = null;
                Logging.info("🎮 Game ended, ready for new game");
                break;
            case ERROR_OCCURRED:
                Logging.error("❌ Game Error: " + event.getMessage());
                if (logArea != null) {
                    logArea.appendText("❌ Game Error: " + event.getMessage() + "\n");
                }
                break;
            default:
                // Log other events
                if (logArea != null) {
                    logArea.appendText("🎮 " + event.getMessage() + "\n");
                }
                break;
        }
    }
    
    /**
     * Returns to the GDK lobby from a running game.
     */
    private void returnToLobby() {
        Logging.info("🔙 returnToLobby() called - gameIsRunning: " + gameIsRunning + ", currentGame: " + (currentGame != null ? currentGame.getGameName() : "null") + ", lobbyScene: " + (lobbyScene != null ? "exists" : "null"));
        
        if (gameIsRunning && currentGame != null) {
            Logging.info("🔙 Returning to GDK lobby");
            
            // Clean up current game
            currentGame.onGameClose();
            gameIsRunning = false;
            currentGame = null;
            
            // Return to lobby scene - ensure we're on the JavaFX Application Thread
            if (lobbyScene != null) {
                javafx.application.Platform.runLater(() -> {
                    primaryStage.setScene(lobbyScene);
                    primaryStage.setTitle("OMG Game Development Kit");
                    Logging.info("✅ Returned to GDK lobby");
                });
            } else {
                Logging.error("❌ lobbyScene is null - cannot return to lobby");
            }
        } else {
            Logging.warning("⚠️ Cannot return to lobby - gameIsRunning: " + gameIsRunning + ", currentGame: " + (currentGame != null ? "exists" : "null"));
        }
    }
    
    /**
     * Shows an error dialog.
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            Logging.error("❌ Fatal error: " + e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 