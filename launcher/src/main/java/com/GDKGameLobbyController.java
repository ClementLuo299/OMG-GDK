package com;

import com.gdk.shared.game.GameModule;
import com.gdk.shared.enums.GameDifficulty;
import com.gdk.shared.enums.GameMode;
import com.gdk.shared.settings.GameSettings;
import com.gdk.shared.game.GameOptions;
import com.gdk.shared.game.GameEventHandler;
import com.gdk.shared.game.GameEvent;
import com.gdk.shared.utils.ModuleLoader;
import com.gdk.shared.utils.error_handling.Logging;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Properties;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * GDK Game Picker Controller - Simplified game picker for the GDK.
 * Allows developers to easily select and run game modules for testing.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited July 20, 2025
 * @since 1.0
 */
public class GDKGameLobbyController implements Initializable {

    // ==================== FXML INJECTIONS ====================
    
    @FXML private VBox mainContainer;
    @FXML private Button backButton;
    @FXML private Button refreshButton;
    @FXML private Label gameTitleLabel;
    @FXML private Label gameDescriptionLabel;
    @FXML private ImageView gameIconImageView;
    @FXML private ComboBox<GameModule> gameSelector;
    @FXML private ComboBox<GameMode> gameModeComboBox;
    @FXML private Spinner<Integer> playerCountSpinner;
    @FXML private ComboBox<String> difficultyComboBox;
    @FXML private Button launchGameButton;
    @FXML private Button quickPlayButton;
    @FXML private Button settingsButton;
    @FXML private TextArea logArea;
    
    // ==================== DEPENDENCIES ====================
    
    private static final String CONFIG_FILE = "gdk-config.properties";
    private static Properties config;
    private Stage primaryStage;
    private ObservableList<GameModule> availableGames;
    private GameModule selectedGame;
    private GDKApplication gdkApplication;
    
    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logging.info("🎮 Initializing GDK Game Picker Controller");
        
        // Load configuration
        loadConfig();
        
        // Initialize UI components
        setupUI();
        setupEventHandlers();
        
        // Load available games
        refreshGameList();
        
        Logging.info("✅ GDK Game Picker Controller initialized successfully");
    }
    
    /**
     * Sets the primary stage for launching games.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    /**
     * Sets the GDK application reference for proper event handling.
     */
    public void setGDKApplication(GDKApplication gdkApplication) {
        this.gdkApplication = gdkApplication;
    }
    
    // ==================== CONFIGURATION ====================
    
    private void loadConfig() {
        config = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config.load(reader);
                Logging.info("📂 Loaded GDK configuration from " + CONFIG_FILE);
            } catch (IOException e) {
                Logging.error("❌ Failed to load config: " + e.getMessage());
                createDefaultConfig();
            }
        } else {
            createDefaultConfig();
        }
    }
    
    private void createDefaultConfig() {
        Logging.info("🆕 Creating default GDK configuration");
        
        config.setProperty("username", "GDK Developer");
        config.setProperty("serverUrl", "localhost");
        config.setProperty("serverPort", "8080");
        config.setProperty("enableLogging", "true");
        config.setProperty("enableDebugMode", "true");
        
        saveConfig();
    }
    
    private void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            config.store(writer, "GDK Configuration");
            Logging.info("💾 Saved GDK configuration to " + CONFIG_FILE);
        } catch (IOException e) {
            Logging.error("❌ Failed to save config: " + e.getMessage());
        }
    }
    
    // ==================== UI SETUP ====================
    
    private void setupUI() {
        // Set up game selector
        availableGames = FXCollections.observableArrayList();
        gameSelector.setItems(availableGames);
        gameSelector.setCellFactory(param -> new ListCell<GameModule>() {
            @Override
            protected void updateItem(GameModule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getGameName() + " (" + item.getGameCategory() + ")");
                }
            }
        });
        gameSelector.setButtonCell(gameSelector.getCellFactory().call(null));
        
        // Set up game mode combo box (will be populated when game is selected)
        gameModeComboBox.setItems(FXCollections.observableArrayList());
        
        // Add tooltip to show game mode descriptions
        gameModeComboBox.setCellFactory(param -> new ListCell<GameMode>() {
            @Override
            protected void updateItem(GameMode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item.toString()); // Uses the enhanced toString() method
                    setTooltip(new Tooltip(item.getDescription()));
                }
            }
        });
        gameModeComboBox.setButtonCell(gameModeComboBox.getCellFactory().call(null));
        
        // Set up player count spinner (will be configured when game is selected)
        playerCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 2));
        
        // Set up difficulty combo box (will be populated when game is selected)
        difficultyComboBox.setItems(FXCollections.observableArrayList());
        
        // Add tooltip to show difficulty descriptions
        difficultyComboBox.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    // Find the corresponding GameDifficulty and set tooltip
                    for (GameDifficulty difficulty : GameDifficulty.values()) {
                        if (difficulty.getDisplayName().equals(item)) {
                            setTooltip(new Tooltip(difficulty.getDescription()));
                            break;
                        }
                    }
                }
            }
        });
        difficultyComboBox.setButtonCell(difficultyComboBox.getCellFactory().call(null));
        
        // Set up log area
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(8);
        
        // Update game info when game is selected
        gameSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedGame = newVal;
            updateGameInfo();
            updatePlayerCountLimits();
        });
        
        // Update player count limits when game mode changes
        gameModeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updatePlayerCountLimits();
                logArea.appendText("🎮 Switched to " + newVal.getDisplayName() + " mode\n");
            }
        });
    }
    
    private void setupEventHandlers() {
        // Back button
        backButton.setOnAction(e -> {
            Logging.info("Back button clicked");
            if (primaryStage != null) {
                primaryStage.close();
            }
        });
        
        // Refresh button
        refreshButton.setOnAction(e -> {
            Logging.info("Refresh button clicked");
            refreshGameList();
        });
        
        // Launch game button
        launchGameButton.setOnAction(e -> launchGame());
        
        // Quick play button
        quickPlayButton.setOnAction(e -> quickPlay());
        
        // Settings button
        settingsButton.setOnAction(e -> openGameSettings());
    }
    
    // ==================== GAME MANAGEMENT ====================
    
    private void refreshGameList() {
        try {
            logArea.appendText("🔄 Refreshing game modules...\n");
            
            List<GameModule> modules = ModuleLoader.discoverModules("../modules");
            availableGames.clear();
            availableGames.addAll(modules);
            
            logArea.appendText("📦 Found " + modules.size() + " game modules\n");
            
            for (GameModule module : modules) {
                logArea.appendText("✅ " + module.getGameName() + " - " + module.getGameDescription() + "\n");
            }
            
        } catch (Exception e) {
            Logging.error("❌ Failed to refresh games: " + e.getMessage());
            logArea.appendText("❌ Error: " + e.getMessage() + "\n");
        }
    }
    
    private void updateGameInfo() {
        if (selectedGame != null) {
            gameTitleLabel.setText(selectedGame.getGameName());
            gameDescriptionLabel.setText(selectedGame.getGameDescription());
            
            // Try to load game icon
            try {
                String iconPath = "/games/" + selectedGame.getGameName().toLowerCase().replace(" ", "") + "/icons/game_icon.png";
                javafx.scene.image.Image icon = new javafx.scene.image.Image(getClass().getResourceAsStream(iconPath));
                if (icon != null) {
                    gameIconImageView.setImage(icon);
                }
            } catch (Exception e) {
                // Use default icon or leave empty
                Logging.info("No custom icon found for " + selectedGame.getGameName());
            }
            
            // Update game mode options based on what the game supports
            updateGameModeOptions();
            
            // Update difficulty options based on what the game supports
            updateDifficultyOptions();
            
            // Update player count limits
            updatePlayerCountLimits();
            
            logArea.appendText("🎮 Loaded game options for " + selectedGame.getGameName() + "\n");
        } else {
            gameTitleLabel.setText("Select a Game");
            gameDescriptionLabel.setText("Choose a game from the dropdown to get started...");
            gameIconImageView.setImage(null);
            
            // Clear all options
            gameModeComboBox.getItems().clear();
            difficultyComboBox.getItems().clear();
            playerCountSpinner.getValueFactory().setValue(1);
        }
    }
    
    private void updateGameModeOptions() {
        if (selectedGame != null) {
            GameMode[] supportedModes = selectedGame.getSupportedGameModes();
            ObservableList<GameMode> modeOptions = FXCollections.observableArrayList(supportedModes);
            gameModeComboBox.setItems(modeOptions);
            
            // Set default game mode
            GameMode defaultMode = selectedGame.getDefaultGameMode();
            if (modeOptions.contains(defaultMode)) {
                gameModeComboBox.setValue(defaultMode);
            } else if (!modeOptions.isEmpty()) {
                gameModeComboBox.setValue(modeOptions.get(0));
            }
            
            logArea.appendText("🎮 Supported game modes: " + modeOptions.size() + " modes\n");
        }
    }
    
    private void updateDifficultyOptions() {
        if (selectedGame != null) {
            GameDifficulty[] supportedDifficulties = selectedGame.getSupportedDifficulties();
            ObservableList<String> difficultyOptions = FXCollections.observableArrayList();
            
            for (GameDifficulty difficulty : supportedDifficulties) {
                difficultyOptions.add(difficulty.getDisplayName());
            }
            
            difficultyComboBox.setItems(difficultyOptions);
            
            // Set default difficulty
            GameDifficulty defaultDifficulty = selectedGame.getDefaultDifficulty();
            String defaultDifficultyName = defaultDifficulty.getDisplayName();
            if (difficultyOptions.contains(defaultDifficultyName)) {
                difficultyComboBox.setValue(defaultDifficultyName);
            } else if (!difficultyOptions.isEmpty()) {
                difficultyComboBox.setValue(difficultyOptions.get(0));
            }
            
            logArea.appendText("🎮 Supported difficulties: " + difficultyOptions.size() + " levels\n");
        }
    }
    
    private void updatePlayerCountLimits() {
        if (selectedGame != null) {
            GameMode currentMode = gameModeComboBox.getValue();
            if (currentMode != null) {
                java.util.Map<GameMode, int[]> playerCounts = selectedGame.getSupportedPlayerCounts();
                int[] range = playerCounts.get(currentMode);
                
                if (range != null) {
                    int minPlayers = range[0];
                    int maxPlayers = range[1];
                    int defaultPlayers = selectedGame.getDefaultPlayerCount(currentMode);
                    
                    SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        minPlayers, maxPlayers, defaultPlayers
                    );
                    playerCountSpinner.setValueFactory(factory);
                    
                    logArea.appendText("🎮 " + currentMode.getDisplayName() + " supports " + minPlayers + "-" + maxPlayers + " players\n");
                }
            }
        }
    }
    
    private void launchGame() {
        if (selectedGame == null) {
            showError("No Game Selected", "Please select a game to launch.");
            return;
        }

        GameMode gameMode = gameModeComboBox.getValue();
        int playerCount = playerCountSpinner.getValue();
        String difficulty = difficultyComboBox.getValue();

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
        options.setOption("difficulty", difficulty);

        logArea.appendText("🚀 Launching " + selectedGame.getGameName() + " in " + gameMode.getDisplayName() + 
                          " mode with " + playerCount + " players (Difficulty: " + difficulty + ")\n");

        try {
            // Delegate to the GDK application to launch the game with proper event handling
            if (gdkApplication != null) {
                gdkApplication.launchGame(selectedGame, gameMode, playerCount, difficulty);
            } else {
                Logging.error("❌ GDK Application reference not set");
                logArea.appendText("❌ Error: GDK Application not available\n");
            }
        } catch (Exception e) {
            Logging.error("❌ Error launching game: " + e.getMessage());
            logArea.appendText("❌ Error: " + e.getMessage() + "\n");
        }
    }
    
    private void quickPlay() {
        if (selectedGame == null) {
            showError("No Game Selected", "Please select a game for quick play.");
            return;
        }

        // Set default quick play settings based on game's defaults
        GameMode defaultMode = selectedGame.getDefaultGameMode();
        if (gameModeComboBox.getItems().contains(defaultMode)) {
            gameModeComboBox.setValue(defaultMode);
        }
        
        int defaultPlayerCount = selectedGame.getDefaultPlayerCount(defaultMode);
        playerCountSpinner.getValueFactory().setValue(defaultPlayerCount);
        
        GameDifficulty defaultDifficulty = selectedGame.getDefaultDifficulty();
        String defaultDifficultyName = defaultDifficulty.getDisplayName();
        if (difficultyComboBox.getItems().contains(defaultDifficultyName)) {
            difficultyComboBox.setValue(defaultDifficultyName);
        }

        logArea.appendText("⚡ Quick play mode activated for " + selectedGame.getGameName() + 
                          " (Mode: " + defaultMode.getDisplayName() + 
                          ", Players: " + defaultPlayerCount + 
                          ", Difficulty: " + defaultDifficultyName + ")\n");
        
        // Launch the game with quick play settings
        launchGame();
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Opens the game settings dialog for the selected game.
     */
    private void openGameSettings() {
        if (selectedGame == null) {
            showError("No Game Selected", "Please select a game to configure settings.");
            return;
        }
        
        if (!selectedGame.hasCustomSettings()) {
            showError("No Custom Settings", "This game doesn't have any custom settings to configure.");
            return;
        }
        
        try {
            GameSettings gameSettings = selectedGame.getCustomSettings();
            GameSettingsDialog dialog = new GameSettingsDialog(primaryStage, gameSettings);
            
            logArea.appendText("⚙️ Opening settings for " + selectedGame.getGameName() + "\n");
            
            if (dialog.showAndWait()) {
                logArea.appendText("✅ Settings configured successfully\n");
            } else {
                logArea.appendText("❌ Settings configuration cancelled or invalid\n");
            }
            
        } catch (Exception e) {
            Logging.error("❌ Failed to open game settings: " + e.getMessage(), e);
            showError("Settings Error", "Failed to open game settings: " + e.getMessage());
        }
    }
} 