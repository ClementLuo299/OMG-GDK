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
 * @edited July 25, 2025
 * @since 1.0
 */
public class GDKGameLobbyController implements Initializable {

    // ==================== FXML INJECTIONS ====================
    
    // Main UI components
    @FXML private VBox mainContainer;
    @FXML private Label gameTitleLabel;
    @FXML private ImageView gameIconImageView;
    @FXML private Label statusLabel;
    
    // Game selection components
    @FXML private ComboBox<GameModule> gameSelector;
    @FXML private Button launchGameButton;
    @FXML private Button refreshButton;
    
    // Message and logging components
    @FXML private TextArea messageArea;
    
    // JSON configuration components
    @FXML private TextArea jsonDataTextArea;
    @FXML private Button validateJsonButton;
    @FXML private Button clearJsonButton;
    
    // Application control components
    @FXML private Button exitButton;

    // ==================== DEPENDENCIES ====================
    
    private Stage primaryStage;
    private GDKApplication gdkApplication;
    private GDKViewModel viewModel;
    
    // Game management
    private ObservableList<GameModule> availableGames;
    private GameModule selectedGame;
    
    // JSON processing
    private ObjectMapper jsonMapper;
    
    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logging.info("🎮 Initializing GDK Game Picker Controller");
        
        initializeDependencies();
        setupUI();
        setupEventHandlers();
        loadInitialData();
        
        Logging.info("✅ GDK Game Picker Controller initialized successfully");
    }
    
    /**
     * Initialize all dependencies and data structures
     */
    private void initializeDependencies() {
        jsonMapper = new ObjectMapper();
        availableGames = FXCollections.observableArrayList();
    }
    
    /**
     * Load initial data and perform startup tasks
     */
    private void loadInitialData() {
        refreshGameList();
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
    
    // ==================== UI SETUP ====================
    
    /**
     * Setup all UI components and their properties
     */
    private void setupUI() {
        setupGameSelector();
        setupMessageArea();
        setupJsonComponents();
        
        Logging.info("🎨 UI components initialized");
    }
    
    /**
     * Setup the game selector ComboBox with custom cell factory
     */
    private void setupGameSelector() {
        gameSelector.setCellFactory(param -> new ListCell<GameModule>() {
            @Override
            protected void updateItem(GameModule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select a game...");
                } else {
                    setText(item.getClass().getSimpleName());
                }
            }
        });
        
        gameSelector.setButtonCell(gameSelector.getCellFactory().call(null));
    }
    
    /**
     * Setup the message area for logging
     */
    private void setupMessageArea() {
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;");
        
        // Add welcome message
        addMessage("🎮 Welcome to GDK Game Picker!");
    }
    
    /**
     * Setup JSON-related components
     */
    private void setupJsonComponents() {
        // JSON components are already defined in FXML
        // Additional setup can be added here if needed
    }
    
    // ==================== EVENT HANDLERS ====================
    
    /**
     * Setup all event handlers for UI components
     */
    private void setupEventHandlers() {
        setupGameSelectionHandler();
        setupButtonHandlers();
        setupJsonHandlers();
        
        Logging.info("🎯 Event handlers configured");
    }
    
    /**
     * Setup game selection event handler
     */
    private void setupGameSelectionHandler() {
        gameSelector.setOnAction(e -> {
            selectedGame = gameSelector.getValue();
            updateGameInfo();
            addLogMessage("🎮 Selected game: " + (selectedGame != null ? selectedGame.getClass().getSimpleName() : "None"));
        });
    }
    
    /**
     * Setup button event handlers
     */
    private void setupButtonHandlers() {
        launchGameButton.setOnAction(e -> launchGame());
        refreshButton.setOnAction(e -> {
            addLogMessage("🔄 Refreshing game list...");
            refreshGameList();
        });
        exitButton.setOnAction(e -> {
            Logging.info("🔒 GDK Game Lobby closing");
            Platform.exit();
        });
    }
    
    /**
     * Setup JSON-related event handlers
     */
    private void setupJsonHandlers() {
        validateJsonButton.setOnAction(e -> validateJsonData());
        clearJsonButton.setOnAction(e -> clearJsonData());
        
        // Auto-clear validation on text change
        jsonDataTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
            clearJsonValidation();
        });
    }
    
    // ==================== GAME MANAGEMENT ====================
    
    /**
     * Refresh the list of available games by scanning modules directory
     */
    private void refreshGameList() {
        try {
            availableGames.clear();
            
            String modulesDir = GDKApplication.MODULES_DIR;
            Logging.info("📂 Scanning for modules in: " + modulesDir);
            
            List<GameModule> discoveredModules = ModuleLoader.discoverModules(modulesDir);
            
            for (GameModule module : discoveredModules) {
                availableGames.add(module);
                Logging.info("📦 Loaded game module: " + module.getClass().getSimpleName());
            }
            
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
    
    /**
     * Update game information display when a game is selected
     */
    private void updateGameInfo() {
        if (selectedGame != null) {
            // Game data will be handled via JSON
            // Icon loading can be done through JSON configuration
        }
    }
    
    /**
     * Launch the currently selected game with JSON configuration
     */
    private void launchGame() {
        if (selectedGame == null) {
            showError("No Game Selected", "Please select a game to launch.");
            return;
        }

        Map<String, Object> jsonData = getJsonData();
        if (jsonData == null) {
            showError("Invalid JSON", "Please enter valid JSON configuration.");
            return;
        }
        
        if (!validatePlayerCount(jsonData)) {
            return;
        }
        
        addLogMessage("📦 Including custom JSON data with " + jsonData.size() + " fields");
        addLogMessage("🚀 Launching " + selectedGame.getClass().getSimpleName());

        if (viewModel != null) {
            viewModel.handleLaunchGame(selectedGame);
        } else {
            showError("Application Error", "ViewModel reference is not available.");
        }
    }
    
    /**
     * Validate player count from JSON data
     */
    private boolean validatePlayerCount(Map<String, Object> jsonData) {
        Integer playerCount = (Integer) jsonData.getOrDefault("playerCount", 2);
        
        if (playerCount < 1 || playerCount > 10) {
            showError("Invalid Player Count", 
                "Player count must be between 1 and 10. Please check your JSON configuration.");
            return false;
        }
        return true;
    }
    
    // ==================== JSON DATA HANDLING ====================
    
    /**
     * Validate the JSON data entered by the user
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
     * Clear the JSON data text area
     */
    private void clearJsonData() {
        jsonDataTextArea.clear();
        clearJsonValidation();
        addLogMessage("🗑️ Cleared JSON data");
    }
    
    /**
     * Clear JSON validation messages (placeholder for future implementation)
     */
    private void clearJsonValidation() {
        // Validation messages are shown in the message area
        // No specific clearing needed for now
    }
    
    /**
     * Parse JSON data from the text area
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
    
    // ==================== MESSAGING & LOGGING ====================
    
    /**
     * Add a timestamped log message to the message area
     */
    private void addLogMessage(String message) {
        addMessage(message);
    }
    
    /**
     * Add a message to the message area with timestamp
     */
    private void addMessage(String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        messageArea.appendText("[" + timestamp + "] " + message + "\n");
        // Auto-scroll to bottom
        messageArea.setScrollTop(Double.MAX_VALUE);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Show an error dialog with the given title and content
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 