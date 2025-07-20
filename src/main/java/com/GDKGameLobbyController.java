package com;

import com.game.GameModule;
import com.game.enums.GameDifficulty;
import com.game.enums.GameMode;
import com.game.GameOptions;
import com.utils.ModuleLoader;
import com.utils.error_handling.Logging;
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
 * GDK Game Lobby Controller - Simplified game lobby for the GDK.
 * Combines game module discovery with lobby functionality for easy game testing.
 *
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class GDKGameLobbyController implements Initializable {

    // ==================== FXML INJECTIONS ====================
    
    @FXML private BorderPane mainContainer;
    @FXML private Button backButton;
    @FXML private Button refreshButton;
    @FXML private Label gameTitleLabel;
    @FXML private Label gameDescriptionLabel;
    @FXML private ImageView gameIconImageView;
    @FXML private VBox createMatchPanel;
    @FXML private ComboBox<GameModule> gameSelector;
    @FXML private ComboBox<GameMode> gameModeComboBox;
    @FXML private Spinner<Integer> playerCountSpinner;
    @FXML private TextField matchNameField;
    @FXML private CheckBox privateMatchCheckBox;
    @FXML private Spinner<Integer> timeLimitSpinner;
    @FXML private ComboBox<String> difficultyComboBox;
    @FXML private Button createMatchButton;
    @FXML private Button quickPlayButton;
    @FXML private VBox availableMatchesPanel;
    @FXML private ListView<MatchInfo> availableMatchesListView;
    @FXML private Label noMatchesLabel;
    @FXML private TextArea logArea;
    
    // ==================== DEPENDENCIES ====================
    
    private static final String CONFIG_FILE = "gdk-config.properties";
    private static Properties config;
    private Stage primaryStage;
    private ObservableList<GameModule> availableGames;
    private ObservableList<MatchInfo> availableMatches;
    private GameModule selectedGame;
    
    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logging.info("🎮 Initializing GDK Game Lobby Controller");
        
        // Load configuration
        loadConfig();
        
        // Initialize UI components
        setupUI();
        setupEventHandlers();
        
        // Load available games
        refreshGameList();
        
        Logging.info("✅ GDK Game Lobby Controller initialized successfully");
    }
    
    /**
     * Sets the primary stage for launching games.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
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
        
        // Set up game mode combo box
        gameModeComboBox.setItems(FXCollections.observableArrayList(GameMode.values()));
        gameModeComboBox.setValue(GameMode.LOCAL_MULTIPLAYER);
        
        // Set up player count spinner
        playerCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 2));
        
        // Set up time limit spinner (in minutes)
        timeLimitSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 30));
        
        // Set up difficulty combo box
        difficultyComboBox.setItems(FXCollections.observableArrayList("Easy", "Medium", "Hard"));
        difficultyComboBox.setValue("Medium");
        
        // Set up available matches list
        availableMatches = FXCollections.observableArrayList();
        availableMatchesListView.setItems(availableMatches);
        availableMatchesListView.setCellFactory(param -> new MatchInfoListCell());
        
        // Set up match name field
        matchNameField.setText("My Match");
        
        // Set up log area
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(8);
        
        // Initially hide no matches label
        noMatchesLabel.setVisible(false);
        
        // Update game info when game is selected
        gameSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            selectedGame = newVal;
            updateGameInfo();
            updatePlayerCountLimits();
        });
        
        // Update player count limits when game mode changes
        gameModeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePlayerCountLimits();
        });
    }
    
    private void setupEventHandlers() {
        // Back button
        backButton.setOnAction(e -> {
            Logging.info("Back button clicked");
            // For GDK, this could go back to a main menu or close the app
            if (primaryStage != null) {
                primaryStage.close();
            }
        });
        
        // Refresh button
        refreshButton.setOnAction(e -> {
            Logging.info("Refresh button clicked");
            refreshGameList();
        });
        
        // Create match button
        createMatchButton.setOnAction(e -> {
            Logging.info("Create match button clicked");
            createMatch();
        });
        
        // Quick play button
        quickPlayButton.setOnAction(e -> {
            Logging.info("Quick play button clicked");
            quickPlay();
        });
        
        // Available matches list
        availableMatchesListView.setOnMouseClicked(event -> {
            MatchInfo selectedMatch = availableMatchesListView.getSelectionModel().getSelectedItem();
            if (selectedMatch != null) {
                Logging.info("Selected match: " + selectedMatch.getMatchName());
                joinMatch(selectedMatch);
            }
        });
    }
    
    // ==================== GAME MANAGEMENT ====================
    
    private void refreshGameList() {
        try {
            logArea.appendText("🔄 Refreshing game modules...\n");
            
            List<GameModule> modules = ModuleLoader.discoverModules("modules");
            availableGames.clear();
            availableGames.addAll(modules);
            
            logArea.appendText("📦 Found " + modules.size() + " game modules\n");
            
            for (GameModule module : modules) {
                logArea.appendText("✅ " + module.getGameName() + " - " + module.getGameDescription() + "\n");
            }
            
            // Generate some sample matches for demonstration
            generateSampleMatches();
            
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
                String iconPath = selectedGame.getGameIconPath();
                if (iconPath != null && !iconPath.isEmpty()) {
                    // For now, we'll just show a placeholder
                    // In a real implementation, you'd load the actual icon
                }
            } catch (Exception e) {
                Logging.warning("Could not load game icon: " + e.getMessage());
            }
        } else {
            gameTitleLabel.setText("Select a Game");
            gameDescriptionLabel.setText("Choose a game from the dropdown to get started");
        }
    }
    
    private void updatePlayerCountLimits() {
        if (selectedGame != null) {
            int minPlayers = selectedGame.getMinPlayers();
            int maxPlayers = selectedGame.getMaxPlayers();
            
            SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                minPlayers, maxPlayers, Math.max(minPlayers, playerCountSpinner.getValue())
            );
            playerCountSpinner.setValueFactory(factory);
        }
    }
    
    // ==================== MATCH MANAGEMENT ====================
    
    private void generateSampleMatches() {
        availableMatches.clear();
        
        if (selectedGame != null) {
            // Generate some sample matches for the selected game
            availableMatches.add(new MatchInfo(
                selectedGame.getGameName() + " - Quick Match",
                config.getProperty("username", "GDK Developer"),
                GameMode.LOCAL_MULTIPLAYER,
                1,
                2,
                false
            ));
            
            availableMatches.add(new MatchInfo(
                selectedGame.getGameName() + " - Tournament",
                "Player2",
                GameMode.ONLINE_MULTIPLAYER,
                2,
                4,
                false
            ));
            
            availableMatches.add(new MatchInfo(
                selectedGame.getGameName() + " - Private Game",
                "Player3",
                GameMode.LOCAL_MULTIPLAYER,
                1,
                2,
                true
            ));
        }
        
        updateMatchesDisplay();
    }
    
    private void updateMatchesDisplay() {
        if (availableMatches.isEmpty()) {
            noMatchesLabel.setVisible(true);
            availableMatchesListView.setVisible(false);
        } else {
            noMatchesLabel.setVisible(false);
            availableMatchesListView.setVisible(true);
        }
    }
    
    private void createMatch() {
        if (selectedGame == null) {
            showError("No Game Selected", "Please select a game first.");
            return;
        }
        
        GameMode gameMode = gameModeComboBox.getValue();
        int playerCount = playerCountSpinner.getValue();
        String matchName = matchNameField.getText();
        boolean isPrivate = privateMatchCheckBox.isSelected();
        int timeLimit = timeLimitSpinner.getValue();
        String difficulty = difficultyComboBox.getValue();
        
        // Validate inputs
        if (matchName == null || matchName.trim().isEmpty()) {
            showError("Invalid Match Name", "Please enter a match name.");
            return;
        }
        
        // Create game options
        GameOptions options = new GameOptions();
        options.setOption("matchName", matchName);
        options.setOption("isPrivate", String.valueOf(isPrivate));
        options.setOption("timeLimit", String.valueOf(timeLimit));
        options.setOption("difficulty", difficulty);
        options.setOption("debugMode", config.getProperty("enableDebugMode", "true"));
        options.setOption("serverUrl", config.getProperty("serverUrl", "localhost"));
        options.setOption("serverPort", config.getProperty("serverPort", "8080"));
        
        logArea.appendText("🚀 Creating match: " + matchName + " for " + selectedGame.getGameName() + "\n");
        
        // Launch the game
        launchGame(gameMode, playerCount, options);
    }
    
    private void quickPlay() {
        if (selectedGame == null) {
            showError("No Game Selected", "Please select a game first.");
            return;
        }
        
        // Quick play with default settings
        GameOptions options = new GameOptions();
        options.setOption("matchName", "Quick Play");
        options.setOption("isPrivate", "false");
        options.setOption("timeLimit", "30");
        options.setOption("difficulty", "Medium");
        options.setOption("debugMode", config.getProperty("enableDebugMode", "true"));
        options.setOption("serverUrl", config.getProperty("serverUrl", "localhost"));
        options.setOption("serverPort", config.getProperty("serverPort", "8080"));
        
        logArea.appendText("⚡ Quick play: " + selectedGame.getGameName() + "\n");
        
        // Launch with default mode and player count
        GameMode defaultMode = selectedGame.supportsLocalMultiplayer() ? 
            GameMode.LOCAL_MULTIPLAYER : GameMode.SINGLE_PLAYER;
        int defaultPlayers = Math.max(selectedGame.getMinPlayers(), 2);
        
        launchGame(defaultMode, defaultPlayers, options);
    }
    
    private void joinMatch(MatchInfo match) {
        logArea.appendText("🎮 Joining match: " + match.getMatchName() + "\n");
        
        // For GDK, we'll just launch the game directly
        // In a real implementation, you'd connect to the match server
        
        GameOptions options = new GameOptions();
        options.setOption("matchName", match.getMatchName());
        options.setOption("isPrivate", String.valueOf(match.isPrivate()));
        options.setOption("hostName", match.getHostName());
        options.setOption("debugMode", config.getProperty("enableDebugMode", "true"));
        options.setOption("serverUrl", config.getProperty("serverUrl", "localhost"));
        options.setOption("serverPort", config.getProperty("serverPort", "8080"));
        
        launchGame(match.getGameMode(), match.getMaxPlayers(), options);
    }
    
    private void launchGame(GameMode gameMode, int playerCount, GameOptions options) {
        try {
            Scene gameScene = selectedGame.launchGame(primaryStage, gameMode, playerCount, options);
            if (gameScene != null) {
                primaryStage.setScene(gameScene);
                primaryStage.setTitle(selectedGame.getGameName() + " - GDK");
                logArea.appendText("✅ Game launched successfully\n");
            } else {
                logArea.appendText("❌ Failed to launch game\n");
            }
        } catch (Exception e) {
            Logging.error("❌ Error launching game: " + e.getMessage());
            logArea.appendText("❌ Error: " + e.getMessage() + "\n");
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // ==================== INNER CLASSES ====================
    
    public static class MatchInfo {
        private String matchName;
        private String hostName;
        private GameMode gameMode;
        private int currentPlayers;
        private int maxPlayers;
        private boolean isPrivate;
        
        public MatchInfo(String matchName, String hostName, GameMode gameMode, 
                        int currentPlayers, int maxPlayers, boolean isPrivate) {
            this.matchName = matchName;
            this.hostName = hostName;
            this.gameMode = gameMode;
            this.currentPlayers = currentPlayers;
            this.maxPlayers = maxPlayers;
            this.isPrivate = isPrivate;
        }
        
        public String getMatchName() { return matchName; }
        public String getHostName() { return hostName; }
        public GameMode getGameMode() { return gameMode; }
        public int getCurrentPlayers() { return currentPlayers; }
        public int getMaxPlayers() { return maxPlayers; }
        public boolean isPrivate() { return isPrivate; }
        
        @Override
        public String toString() {
            return String.format("%s (%s) - %s/%d players - %s", 
                matchName, hostName, currentPlayers, maxPlayers, 
                isPrivate ? "Private" : "Public");
        }
    }
    
    private class MatchInfoListCell extends ListCell<MatchInfo> {
        @Override
        protected void updateItem(MatchInfo match, boolean empty) {
            super.updateItem(match, empty);
            if (empty || match == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(match.toString());
                setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8px;");
            }
        }
    }
} 