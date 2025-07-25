



import gdk.GameModule;

import gdk.Logging;
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
import java.util.ArrayList;
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
import javafx.application.Platform;

/**
 * GDK Game Picker Controller - Simplified game picker for the GDK.
 * Allows developers to easily select and run game modules with JSON configuration.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited July 23, 2025
 * @since 1.0
 */
public class GDKGameLobbyController implements Initializable {

    // ==================== FXML INJECTIONS ====================
    
    @FXML private VBox mainContainer;
    @FXML private Button exitButton;
    @FXML private Button refreshButton;
    @FXML private Label gameTitleLabel;
    @FXML private ImageView gameIconImageView;
    @FXML private ComboBox<GameModule> gameSelector;
    @FXML private Button launchGameButton;
    @FXML private TextArea messageArea;
    @FXML private Label statusLabel;
    
    // JSON data components
    @FXML private TextArea jsonDataTextArea;
    @FXML private Button clearJsonButton;
    @FXML private Button validateJsonButton;

    
    // ==================== DEPENDENCIES ====================
    
    private static final String CONFIG_FILE = "gdk-config.properties";
    private static Properties config;
    private Stage primaryStage;
    private ObservableList<GameModule> availableGames;
    private GameModule selectedGame;
    private GDKApplication gdkApplication;
    private GDKViewModel viewModel; // The ViewModel that handles business logic
    private ObjectMapper jsonMapper; // For JSON parsing and validation
    
    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logging.info("🎮 Initializing GDK Game Picker Controller");
        
        // Initialize JSON mapper
        jsonMapper = new ObjectMapper();
        
        // Initialize available games list
        availableGames = FXCollections.observableArrayList();
        
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

    public void setViewModel(GDKViewModel viewModel) {
        this.viewModel = viewModel;
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
        // Setup game selector with custom cell factory
        gameSelector.setCellFactory(param -> new ListCell<GameModule>() {
            @Override
            protected void updateItem(GameModule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getClass().getSimpleName()); // Use class name instead of getGameName()
                }
            }
        });
        
        gameSelector.setButtonCell(gameSelector.getCellFactory().call(null));
        
        // Setup JSON text area with placeholder
        jsonDataTextArea.setPromptText("Enter JSON configuration here. (Resorts to default if invalid)");
        
        // Setup message area
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;");
        
        // Add welcome message to message area
        addMessage("🎮 Welcome to GDK Game Picker!");
        addMessage("📋 Select a game module and configure it with JSON");
        addMessage("🚀 Use the Launch Game button to start playing");
        addMessage("📝 All activities will be logged here");
        addMessage("─".repeat(50));
        
        Logging.info("🎨 UI components initialized");
    }
    
    private void setupEventHandlers() {
        // Game selection handler
        gameSelector.setOnAction(e -> {
            selectedGame = gameSelector.getValue();
            updateGameInfo();
            addLogMessage("🎮 Selected game: " + (selectedGame != null ? selectedGame.getClass().getSimpleName() : "None"));
        });
        
        // Launch game button
        launchGameButton.setOnAction(e -> launchGame());
        
        // Refresh button
        refreshButton.setOnAction(e -> {
            addLogMessage("🔄 Refreshing game list...");
            refreshGameList();
        });
        
        // Exit button
        exitButton.setOnAction(e -> {
            Logging.info("🔒 GDK Game Lobby closing");
            Platform.exit();
        });
        
        // JSON validation button
        validateJsonButton.setOnAction(e -> validateJsonData());
        
        // Clear JSON button
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
            availableGames.clear();
            
            // Get modules directory from config
            String modulesDir = config.getProperty("modulesDir", "modules");
            Logging.info("📂 Scanning for modules in: " + modulesDir);
            
            // Discover modules
            List<GameModule> discoveredModules = ModuleLoader.discoverModules(modulesDir);
            
            for (GameModule module : discoveredModules) {
                availableGames.add(module);
                Logging.info("📦 Loaded game module: " + module.getClass().getSimpleName());
            }
            
            // Set items for the game selector
            gameSelector.setItems(availableGames);
            
            if (availableGames.isEmpty()) {
                addLogMessage("⚠️ No game modules found in " + modulesDir);
            } else {
                addLogMessage("✅ Found " + availableGames.size() + " game module(s)");
            }
            
        } catch (Exception e) {
            Logging.error("❌ Error refreshing game list: " + e.getMessage(), e);
            addLogMessage("❌ Error refreshing game list: " + e.getMessage());
        }
    }
    
    private void updateGameInfo() {
        if (selectedGame != null) {
            // Game data will be handled via JSON
            // Icon loading can be done through JSON configuration
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
        
        // Extract player count from JSON (default to 2 if not specified)
        Integer playerCount = (Integer) jsonData.getOrDefault("playerCount", 2);
        
        // Validate player count (basic validation, detailed validation handled by game)
        if (playerCount < 1 || playerCount > 10) {
            showError("Invalid Player Count", 
                "Player count must be between 1 and 10. Please check your JSON configuration.");
            return;
        }
        
        // Add JSON data
        addLogMessage("📦 Including custom JSON data with " + jsonData.size() + " fields");

        addLogMessage("🚀 Launching " + selectedGame.getClass().getSimpleName());

        // Launch the game using ViewModel
        if (viewModel != null) {
            viewModel.handleLaunchGame(selectedGame);
        } else if (gdkApplication != null) {
            // Fallback to direct application call
            gdkApplication.launchGame(selectedGame);
        } else {
            showError("Application Error", "Neither ViewModel nor GDK Application reference is available.");
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
     * Adds a timestamped message to the message area
     */
    private void addLogMessage(String message) {
        addMessage(message);
    }
    
    /**
     * Adds a message to the message area
     */
    private void addMessage(String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        messageArea.appendText("[" + timestamp + "] " + message + "\n");
        // Auto-scroll to bottom
        messageArea.setScrollTop(Double.MAX_VALUE);
    }
    
    // ==================== JSON DATA HANDLING ====================
    
    /**
     * Validates the JSON data entered by the user.
     */
    private void validateJsonData() {
        String jsonText = jsonDataTextArea.getText().trim();
        
        if (jsonText.isEmpty()) {
            addMessage("✅ JSON is empty (will use defaults)");
            return;
        }
        
        try {
            jsonMapper.readTree(jsonText);
            addMessage("✅ Valid JSON");
            addLogMessage("✅ JSON validation successful");
        } catch (JsonProcessingException e) {
            addMessage("❌ Invalid JSON: " + e.getMessage());
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
     * Clears the JSON validation messages.
     */
    private void clearJsonValidation() {
        // Validation messages are now shown in the message area
        // No need to clear anything specific for validation
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