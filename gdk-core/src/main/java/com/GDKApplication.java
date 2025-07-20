package com;

import com.game.GameModule;
import com.game.enums.GameMode;
import com.game.GameOptions;
import com.game.GameEvent;
import com.game.GameEventHandler;
import com.utils.ModuleLoader;
import com.utils.error_handling.Logging;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.List;
import java.util.Properties;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Simple Game Development Kit (GDK) Application.
 * Allows developers to easily run and test game modules.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited July 20, 2025
 * @since 1.0
 */
public class GDKApplication extends Application {

    private static final String CONFIG_FILE = "gdk-config.properties";
    private static final String MODULES_DIR = "../modules";
    private static Properties config;
    
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
            // Load configuration
            loadConfig();
            
            // Create and configure the UI
            createUI();
            
            // Configure the stage
            primaryStage.setTitle("OMG Game Development Kit");
            primaryStage.setWidth(800);
            primaryStage.setHeight(600);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);
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
     * Loads configuration from file or creates default.
     */
    private void loadConfig() {
        config = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config.load(reader);
                Logging.info("📂 Loaded configuration from " + CONFIG_FILE);
            } catch (IOException e) {
                Logging.error("❌ Failed to load config: " + e.getMessage());
                createDefaultConfig();
            }
        } else {
            createDefaultConfig();
        }
    }

    /**
     * Creates default configuration.
     */
    private void createDefaultConfig() {
        Logging.info("🆕 Creating default configuration");
        
        config.setProperty("username", "GDK Developer");
        config.setProperty("serverUrl", "localhost");
        config.setProperty("serverPort", "8080");
        config.setProperty("enableLogging", "true");
        config.setProperty("enableDebugMode", "true");
        
        saveConfig();
    }

    /**
     * Saves configuration to file.
     */
    private void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            config.store(writer, "GDK Configuration");
            Logging.info("💾 Saved configuration to " + CONFIG_FILE);
        } catch (IOException e) {
            Logging.error("❌ Failed to save config: " + e.getMessage());
        }
    }

    /**
     * Creates the main UI.
     */
    private void createUI() {
        try {
            // Load the GDK Game Lobby FXML
            FXMLLoader loader = new FXMLLoader(GDKApplication.class.getResource("/GDKGameLobby.fxml"));
            lobbyScene = new Scene(loader.load());
            
            // Apply GDK lobby CSS
            lobbyScene.getStylesheets().add(GDKApplication.class.getResource("/gdk-lobby.css").toExternalForm());
            
            // Get the controller and set the primary stage
            GDKGameLobbyController controller = loader.getController();
            if (controller != null) {
                controller.setPrimaryStage(primaryStage);
                controller.setGDKApplication(this);
            }
            
            // Set the scene
            primaryStage.setScene(lobbyScene);
            
            Logging.info("✅ GDK Game Lobby loaded successfully");
            
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
        String username = config.getProperty("username", "Developer");
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
        gameModeSelector.getItems().addAll(GameMode.values());
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
        if (selectedGame == null) {
            Logging.error("❌ No game selected for launch");
            return;
        }

        // Validate player count
        if (playerCount < selectedGame.getMinPlayers() || playerCount > selectedGame.getMaxPlayers()) {
            Logging.error("❌ Invalid player count: " + playerCount + " (supported: " + selectedGame.getMinPlayers() + "-" + selectedGame.getMaxPlayers() + ")");
            return;
        }

        // Create game options
        GameOptions options = new GameOptions();
        options.setOption("debugMode", config.getProperty("enableDebugMode", "true"));
        options.setOption("serverUrl", config.getProperty("serverUrl", "localhost"));
        options.setOption("serverPort", config.getProperty("serverPort", "8080"));
        options.setOption("difficulty", difficulty);

        Logging.info("🚀 Launching " + selectedGame.getGameName() + " in " + gameMode.getDisplayName() + " mode with " + playerCount + " players (Difficulty: " + difficulty + ")");

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
        options.setOption("debugMode", config.getProperty("enableDebugMode", "true"));
        options.setOption("serverUrl", config.getProperty("serverUrl", "localhost"));
        options.setOption("serverPort", config.getProperty("serverPort", "8080"));

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
        TextField usernameField = new TextField(config.getProperty("username", "GDK Developer"));

        Label serverLabel = new Label("Server URL:");
        TextField serverField = new TextField(config.getProperty("serverUrl", "localhost"));

        Label portLabel = new Label("Server Port:");
        Spinner<Integer> portSpinner = new Spinner<>(1, 65535, Integer.parseInt(config.getProperty("serverPort", "8080")));

        content.getChildren().addAll(usernameLabel, usernameField, serverLabel, serverField, portLabel, portSpinner);

        dialog.getDialogPane().setContent(content);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                config.setProperty("username", usernameField.getText());
                config.setProperty("serverUrl", serverField.getText());
                config.setProperty("serverPort", String.valueOf(portSpinner.getValue()));
                saveConfig();
                if (logArea != null) {
                    logArea.appendText("💾 Settings saved\n");
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
        Logging.info("🎮 Game Event: " + event.getType() + " - " + event.getMessage());
        
        switch (event.getType()) {
            case BACK_TO_LOBBY_REQUESTED:
                Logging.info("🔙 GDK received BACK_TO_LOBBY_REQUESTED event");
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
        if (gameIsRunning && currentGame != null) {
            Logging.info("🔙 Returning to GDK lobby");
            
            // Clean up current game
            currentGame.onGameClose();
            gameIsRunning = false;
            currentGame = null;
            
            // Return to lobby scene
            if (lobbyScene != null) {
                primaryStage.setScene(lobbyScene);
                primaryStage.setTitle("OMG Game Development Kit");
                Logging.info("✅ Returned to GDK lobby");
            }
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