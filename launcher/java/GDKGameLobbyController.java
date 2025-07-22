

import com.gdk.shared.game.GameModule;
import com.gdk.shared.game.GameMode;
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
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Properties;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

// JSON parsing imports
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * GDK Game Picker Controller - Simplified game picker for the GDK.
 * Allows developers to easily select and run game modules with JSON configuration.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited July 21, 2025
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
    @FXML private Button launchGameButton;
    @FXML private TextArea logArea;
    
    // JSON data components
    @FXML private TextArea jsonDataTextArea;
    @FXML private Button clearJsonButton;
    @FXML private Button validateJsonButton;
    @FXML private Label jsonValidationLabel;
    
    // ==================== DEPENDENCIES ====================
    
    private static final String CONFIG_FILE = "gdk-config.properties";
    private static Properties config;
    private Stage primaryStage;
    private ObservableList<GameModule> availableGames;
    private GameModule selectedGame;
    private GDKApplication gdkApplication;
    private ObjectMapper jsonMapper; // For JSON parsing and validation
    
    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logging.info("🎮 Initializing GDK Game Picker Controller");
        
        // Initialize JSON mapper
        jsonMapper = new ObjectMapper();
        
        // Load configuration
        loadConfig();
        
        // Initialize UI components
        setupUI();
        setupEventHandlers();
        
        // Load available games
        refreshGameList();
        
        Logging.info("✅ GDK Game Picker Controller initialized successfully");
    }
    
    // ==================== SETUP METHODS ====================
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    public void setGDKApplication(GDKApplication gdkApplication) {
        this.gdkApplication = gdkApplication;
    }
    
    private void loadConfig() {
        config = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config.load(reader);
                Logging.info("📂 Loaded configuration from " + CONFIG_FILE);
            } catch (IOException e) {
                Logging.error("❌ Failed to load config: " + e.getMessage());
            }
        } else {
            createDefaultConfig();
        }
    }
    
    private void createDefaultConfig() {
        config.setProperty("username", "GDK Developer");
        config.setProperty("serverUrl", "localhost");
        config.setProperty("serverPort", "8080");
        config.setProperty("enableDebugMode", "true");
        saveConfig();
        Logging.info("📝 Created default configuration");
    }
    
    private void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            config.store(writer, "GDK Configuration");
            Logging.info("💾 Configuration saved");
        } catch (IOException e) {
            Logging.error("❌ Failed to save config: " + e.getMessage());
        }
    }
    
    private void setupUI() {
        // Setup game selector
        gameSelector.setCellFactory(param -> new ListCell<GameModule>() {
            @Override
            protected void updateItem(GameModule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getGameName());
                }
            }
        });
        
        gameSelector.setButtonCell(gameSelector.getCellFactory().call(null));
        
        // Setup JSON text area with placeholder
        jsonDataTextArea.setPromptText("Enter JSON configuration here...\nExample:\n{\n  \"gameMode\": \"SINGLE_PLAYER\",\n  \"playerCount\": 1,\n  \"difficulty\": \"MEDIUM\",\n  \"customSettings\": {\n    \"soundEnabled\": true,\n    \"maxTurns\": 50\n  }\n}");
        
        // Setup log area with welcome message
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(10);
        logArea.setStyle("-fx-font-family: 'Monaco', 'Consolas', monospace; -fx-font-size: 12px;");
        
        // Add welcome message to log
        logArea.appendText("🎮 Welcome to GDK Game Picker!\n");
        logArea.appendText("📋 Select a game module and configure it with JSON\n");
        logArea.appendText("🚀 Use the Launch Game button to start playing\n");
        logArea.appendText("📝 All activities will be logged here\n");
        logArea.appendText("─".repeat(50) + "\n");
        
        Logging.info("🎨 UI components initialized");
    }
    
    private void setupEventHandlers() {
        // Game selection handler
        gameSelector.setOnAction(e -> {
            selectedGame = gameSelector.getValue();
            updateGameInfo();
            addLogMessage("🎮 Selected game: " + (selectedGame != null ? selectedGame.getGameName() : "None"));
        });
        
        // Launch game handler
        launchGameButton.setOnAction(e -> launchGame());
        
        // Refresh games handler
        refreshButton.setOnAction(e -> {
            refreshGameList();
            addLogMessage("🔄 Refreshed game list");
        });
        
        // JSON validation handler
        validateJsonButton.setOnAction(e -> validateJsonData());
        
        // Clear JSON handler
        clearJsonButton.setOnAction(e -> clearJsonData());
        
        // JSON text change handler
        jsonDataTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
            clearJsonValidation();
        });
        
        Logging.info("🎯 Event handlers configured");
    }
    
    // ==================== GAME MANAGEMENT ====================
    
    private void refreshGameList() {
        try {
            availableGames = FXCollections.observableArrayList();
            List<GameModule> modules = ModuleLoader.discoverModules("../modules");
            
            for (GameModule module : modules) {
                availableGames.add(module);
                Logging.info("📦 Loaded game module: " + module.getGameName());
            }
            
            gameSelector.setItems(availableGames);
            
            if (!availableGames.isEmpty()) {
                gameSelector.setValue(availableGames.get(0));
                selectedGame = availableGames.get(0);
                updateGameInfo();
                addLogMessage("✅ Loaded " + availableGames.size() + " game modules");
            } else {
                addLogMessage("⚠️ No game modules found");
            }
            
        } catch (Exception e) {
            Logging.error("❌ Failed to load game modules: " + e.getMessage(), e);
            addLogMessage("❌ Failed to load game modules: " + e.getMessage());
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
                    // Load icon logic here if needed
                }
            } catch (Exception e) {
                Logging.warning("⚠️ Could not load game icon: " + e.getMessage());
            }
        } else {
            gameTitleLabel.setText("No Game Selected");
            gameDescriptionLabel.setText("Please select a game to launch");
        }
    }
    
    private void launchGame() {
        if (selectedGame == null) {
            showError("No Game Selected", "Please select a game to launch.");
            return;
        }

        // Get JSON data
        Map<String, Object> jsonData = getJsonData();
        if (jsonData == null) {
            showError("Invalid JSON", "Please enter valid JSON configuration.");
            return;
        }
        
        // Extract game mode and player count from JSON
        String gameModeStr = (String) jsonData.getOrDefault("gameMode", "SINGLE_PLAYER");
        Integer playerCount = (Integer) jsonData.getOrDefault("playerCount", 1);
        
        // Find the game mode
        GameMode gameMode = null;
        for (GameMode mode : selectedGame.getSupportedGameModes()) {
            if (mode.getId().equals(gameModeStr)) {
                gameMode = mode;
                break;
            }
        }
        
        if (gameMode == null) {
            gameMode = selectedGame.getDefaultGameMode();
            addLogMessage("⚠️ Game mode '" + gameModeStr + "' not found, using default: " + gameMode.getDisplayName());
        }
        
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
        
        // Add JSON data
        options.setOption("customData", jsonData);
        addLogMessage("📦 Including custom JSON data with " + jsonData.size() + " fields");

        addLogMessage("🚀 Launching " + selectedGame.getGameName() + " in " + gameMode.getDisplayName() + 
                          " mode with " + playerCount + " players");

        // Launch the game
        if (gdkApplication != null) {
            gdkApplication.launchGame(selectedGame, gameMode, playerCount, "MEDIUM", options);
        }
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
     * Adds a timestamped message to the log area
     */
    private void addLogMessage(String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + timestamp + "] " + message + "\n");
        // Auto-scroll to bottom
        logArea.setScrollTop(Double.MAX_VALUE);
    }
    
    // ==================== JSON DATA HANDLING ====================
    
    /**
     * Validates the JSON data entered by the user.
     */
    private void validateJsonData() {
        String jsonText = jsonDataTextArea.getText().trim();
        
        if (jsonText.isEmpty()) {
            jsonValidationLabel.setText("✅ JSON is empty (will use defaults)");
            jsonValidationLabel.setStyle("-fx-text-fill: green;");
            return;
        }
        
        try {
            jsonMapper.readTree(jsonText);
            jsonValidationLabel.setText("✅ Valid JSON");
            jsonValidationLabel.setStyle("-fx-text-fill: green;");
            addLogMessage("✅ JSON validation successful");
        } catch (JsonProcessingException e) {
            jsonValidationLabel.setText("❌ Invalid JSON: " + e.getMessage());
            jsonValidationLabel.setStyle("-fx-text-fill: red;");
            addLogMessage("❌ JSON validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Clears the JSON data text area.
     */
    private void clearJsonData() {
        jsonDataTextArea.clear();
        clearJsonValidation();
        addLogMessage("🗑️ Cleared JSON data");
    }
    
    /**
     * Clears the JSON validation label.
     */
    private void clearJsonValidation() {
        jsonValidationLabel.setText("");
    }
    
    /**
     * Gets the parsed JSON data from the text area.
     * @return Map containing the JSON data, or null if invalid
     */
    private Map<String, Object> getJsonData() {
        String jsonText = jsonDataTextArea.getText().trim();
        
        if (jsonText.isEmpty()) {
            return new HashMap<>(); // Return empty map for defaults
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = jsonMapper.readValue(jsonText, Map.class);
            return data;
        } catch (JsonProcessingException e) {
            Logging.error("❌ Failed to parse JSON: " + e.getMessage());
            return null;
        }
    }
} 