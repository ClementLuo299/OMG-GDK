import gdk.GameModule;
import gdk.Logging;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Controller for the GDK Game Lobby interface.
 * 
 * This class manages the main lobby interface where users can select and launch
 * game modules. It provides functionality for game selection, JSON configuration
 * validation, and game launching with proper error handling.
 * 
 * Key responsibilities:
 * - Manage the game selection interface
 * - Handle JSON configuration input and validation
 * - Coordinate game launching with the ViewModel
 * - Provide user feedback through messages and error dialogs
 * - Manage the refresh of available game modules
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @since 1.0
 */
public class GDKGameLobbyController implements Initializable {

    // ==================== FXML INJECTIONS ====================
    
    // Game Selection Components
    @FXML private ComboBox<GameModule> gameSelector;
    @FXML private Button launchGameButton;
    @FXML private Button refreshButton;
    
    // Message and Logging Components
    @FXML private TextArea messageArea;
    
    // JSON Configuration Components
    @FXML private TextArea jsonDataTextArea;
    @FXML private Button validateJsonButton;
    @FXML private Button clearJsonButton;
    
    // Application Control Components
    @FXML private Button exitButton;

    // ==================== DEPENDENCIES ====================
    
    /**
     * The ViewModel that manages application state and business logic
     */
    private GDKViewModel applicationViewModel;
    
    /**
     * List of available game modules discovered by the module loader
     */
    private ObservableList<GameModule> availableGameModules;
    
    /**
     * The currently selected game module for launching
     */
    private GameModule selectedGameModule;
    
    /**
     * JSON mapper for parsing and validating JSON configuration data
     */
    private ObjectMapper jsonDataMapper;

    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize the controller when FXML is loaded.
     * 
     * This method is called automatically by JavaFX when the FXML
     * file is loaded. It sets up all components and event handlers.
     * 
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logging.info("🎮 Initializing GDK Game Picker Controller");
        
        initializeDependencies();
        setupUserInterface();
        setupEventHandlers();
        
        Logging.info("✅ GDK Game Picker Controller initialized successfully");
    }
    
    /**
     * Initialize all dependencies and data structures.
     * 
     * This method sets up the internal data structures and dependencies
     * required for the controller to function properly.
     */
    private void initializeDependencies() {
        availableGameModules = FXCollections.observableArrayList();
        jsonDataMapper = new ObjectMapper();
    }

    // ==================== SETUP METHODS ====================
    
    /**
     * Set the primary stage reference for this controller.
     * 
     * @param primaryStage The primary application stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        // This method is called by the Startup class but not currently used
        // It's kept for potential future use
    }
    
    /**
     * Set the ViewModel reference for this controller.
     * 
     * @param applicationViewModel The ViewModel to use for business logic
     */
    public void setViewModel(GDKViewModel applicationViewModel) {
        this.applicationViewModel = applicationViewModel;
    }

    // ==================== USER INTERFACE SETUP ====================
    
    /**
     * Set up all user interface components.
     * 
     * This method configures the visual appearance and behavior
     * of all UI components in the lobby interface.
     */
    private void setupUserInterface() {
        setupGameSelectorComponent();
        setupMessageAreaComponent();
        
        Logging.info("🎨 UI components initialized");
    }
    
    /**
     * Set up the game selector ComboBox component.
     * 
     * This method configures the game selector with proper cell factory
     * for displaying game module names in a user-friendly format.
     */
    private void setupGameSelectorComponent() {
        gameSelector.setCellFactory(param -> new ListCell<GameModule>() {
            @Override
            protected void updateItem(GameModule gameModule, boolean empty) {
                super.updateItem(gameModule, empty);
                if (empty || gameModule == null) {
                    setText("Select a game...");
                } else {
                    setText(gameModule.getClass().getSimpleName());
                }
            }
        });
        
        gameSelector.setButtonCell(gameSelector.getCellFactory().call(null));
    }
    
    /**
     * Set up the message area component.
     * 
     * This method configures the message area for displaying
     * user feedback and application status messages.
     */
    private void setupMessageAreaComponent() {
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;");
        
        addUserMessage("🎮 Welcome to GDK Game Picker!");
    }

    // ==================== EVENT HANDLER SETUP ====================
    
    /**
     * Set up all event handlers for user interactions.
     * 
     * This method configures event handlers for all interactive
     * components in the lobby interface.
     */
    private void setupEventHandlers() {
        setupGameSelectionEventHandler();
        setupButtonEventHandlers();
        setupJsonConfigurationEventHandlers();
        
        Logging.info("🎯 Event handlers configured");
    }
    
    /**
     * Set up the game selection event handler.
     * 
     * This method configures what happens when the user selects
     * a game from the ComboBox.
     */
    private void setupGameSelectionEventHandler() {
        gameSelector.setOnAction(event -> {
            selectedGameModule = gameSelector.getValue();
            String selectedGameName = selectedGameModule != null ? 
                selectedGameModule.getClass().getSimpleName() : "None";
            addUserMessage("🎮 Selected game: " + selectedGameName);
        });
    }
    
    /**
     * Set up event handlers for all buttons in the interface.
     * 
     * This method configures the launch, refresh, and exit buttons
     * with their respective actions.
     */
    private void setupButtonEventHandlers() {
        launchGameButton.setOnAction(event -> launchSelectedGame());
        refreshButton.setOnAction(event -> {
            addUserMessage("🔄 Refreshing game list...");
            refreshAvailableGameModules();
        });
        exitButton.setOnAction(event -> {
            Logging.info("🔒 GDK Game Lobby closing");
            Platform.exit();
        });
    }
    
    /**
     * Set up event handlers for JSON configuration components.
     * 
     * This method configures the validate and clear buttons
     * for the JSON configuration area.
     */
    private void setupJsonConfigurationEventHandlers() {
        validateJsonButton.setOnAction(event -> validateJsonConfigurationData());
        clearJsonButton.setOnAction(event -> clearJsonConfigurationData());
    }

    // ==================== GAME MANAGEMENT ====================
    
    /**
     * Refresh the list of available game modules.
     * 
     * This method scans the modules directory for available game modules
     * and updates the UI accordingly with proper error handling.
     */
    private void refreshAvailableGameModules() {
        try {
            availableGameModules.clear();
            
            String modulesDirectoryPath = GDKApplication.MODULES_DIRECTORY_PATH;
            Logging.info("📂 Scanning for modules in: " + modulesDirectoryPath);
            
            List<GameModule> discoveredGameModules = ModuleLoader.discoverModules(modulesDirectoryPath);
            
            for (GameModule gameModule : discoveredGameModules) {
                availableGameModules.add(gameModule);
                Logging.info("📦 Loaded game module: " + gameModule.getClass().getSimpleName());
            }
            
            gameSelector.setItems(availableGameModules);
            
            if (availableGameModules.isEmpty()) {
                addUserMessage("⚠️ No game modules found in " + modulesDirectoryPath);
            } else {
                addUserMessage("✅ Found " + availableGameModules.size() + " game module(s)");
            }
            
        } catch (Exception moduleDiscoveryError) {
            Logging.error("❌ Error refreshing game list: " + moduleDiscoveryError.getMessage(), moduleDiscoveryError);
            addUserMessage("❌ Error refreshing game list: " + moduleDiscoveryError.getMessage());
        }
    }
    
    /**
     * Launch the currently selected game with validation.
     * 
     * This method validates the game selection and JSON configuration,
     * then delegates the actual launching to the ViewModel.
     */
    private void launchSelectedGame() {
        if (!validateGameSelection()) {
            return;
        }

        Map<String, Object> jsonConfigurationData = validateAndGetJsonConfigurationData();
        if (jsonConfigurationData == null) {
            return;
        }
        
        if (!validatePlayerCountInConfiguration(jsonConfigurationData)) {
            return;
        }
        
        logGameLaunchInformation(jsonConfigurationData);
        launchGameWithViewModel();
    }
    
    /**
     * Validate that a game module is selected for launching.
     * 
     * @return true if a game is selected, false otherwise
     */
    private boolean validateGameSelection() {
        if (selectedGameModule == null) {
            showErrorDialog("No Game Selected", "Please select a game to launch.");
            return false;
        }
        return true;
    }
    
    /**
     * Validate and retrieve JSON configuration data.
     * 
     * @return The parsed JSON configuration data, or null if invalid
     */
    private Map<String, Object> validateAndGetJsonConfigurationData() {
        Map<String, Object> jsonConfigurationData = parseJsonConfigurationData();
        if (jsonConfigurationData == null) {
            showErrorDialog("Invalid JSON", "Please enter valid JSON configuration.");
            return null;
        }
        return jsonConfigurationData;
    }
    
    /**
     * Log information about the game being launched.
     * 
     * @param jsonConfigurationData The JSON configuration data being used
     */
    private void logGameLaunchInformation(Map<String, Object> jsonConfigurationData) {
        addUserMessage("📦 Including custom JSON data with " + jsonConfigurationData.size() + " fields");
        addUserMessage("🚀 Launching " + selectedGameModule.getClass().getSimpleName());
    }
    
    /**
     * Launch the game using the ViewModel.
     * 
     * This method delegates the actual game launching to the ViewModel
     * and handles any errors that might occur.
     */
    private void launchGameWithViewModel() {
        if (applicationViewModel != null) {
            applicationViewModel.handleLaunchGame(selectedGameModule);
        } else {
            showErrorDialog("Application Error", "ViewModel reference is not available.");
        }
    }
    
    /**
     * Validate the player count in the JSON configuration.
     * 
     * @param jsonConfigurationData The JSON configuration data to validate
     * @return true if the player count is valid, false otherwise
     */
    private boolean validatePlayerCountInConfiguration(Map<String, Object> jsonConfigurationData) {
        Integer playerCount = (Integer) jsonConfigurationData.getOrDefault("playerCount", 2);
        
        if (playerCount < 1 || playerCount > 10) {
            showErrorDialog("Invalid Player Count", 
                "Player count must be between 1 and 10. Please check your JSON configuration.");
            return false;
        }
        return true;
    }

    // ==================== JSON CONFIGURATION HANDLING ====================
    
    /**
     * Validate the JSON configuration data entered by the user.
     * 
     * This method checks if the JSON text is valid and provides
     * user feedback about the validation result.
     */
    private void validateJsonConfigurationData() {
        String jsonConfigurationText = jsonDataTextArea.getText().trim();
        
        if (jsonConfigurationText.isEmpty()) {
            addUserMessage("✅ JSON is empty (will use defaults)");
            return;
        }
        
        try {
            jsonDataMapper.readTree(jsonConfigurationText);
            addUserMessage("✅ Valid JSON");
        } catch (JsonProcessingException jsonProcessingError) {
            addUserMessage("❌ Invalid JSON: " + jsonProcessingError.getMessage());
        }
    }
    
    /**
     * Clear the JSON configuration data text area.
     * 
     * This method clears the JSON input area and provides
     * user feedback about the action.
     */
    private void clearJsonConfigurationData() {
        jsonDataTextArea.clear();
        addUserMessage("🗑️ Cleared JSON data");
    }
    
    /**
     * Parse the JSON configuration data from the text area.
     * 
     * @return The parsed JSON data as a Map, or null if parsing fails
     */
    private Map<String, Object> parseJsonConfigurationData() {
        String jsonConfigurationText = jsonDataTextArea.getText().trim();
        
        if (jsonConfigurationText.isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> configurationData = jsonDataMapper.readValue(jsonConfigurationText, Map.class);
            return configurationData;
        } catch (JsonProcessingException jsonProcessingError) {
            Logging.error("❌ Failed to parse JSON: " + jsonProcessingError.getMessage());
            return null;
        }
    }

    // ==================== UTILITY METHODS ====================
    
        /**
     * Add a message to the user message area.
     * 
     * This method adds a timestamped message to the message area
     * for user feedback and debugging purposes.
     * 
     * @param userMessage The message to display to the user
     */
    private void addUserMessage(String userMessage) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        messageArea.appendText("[" + timestamp + "] " + userMessage + "\n");
        messageArea.setScrollTop(Double.MAX_VALUE);
    }
    
    /**
     * Show an error dialog to the user.
     * 
     * This method displays a modal error dialog with the specified
     * title and content to inform the user about errors.
     * 
     * @param dialogTitle The title of the error dialog
     * @param errorMessage The error message to display
     */
    private void showErrorDialog(String dialogTitle, String errorMessage) {
        javafx.scene.control.Alert errorDialog = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        errorDialog.setTitle(dialogTitle);
        errorDialog.setHeaderText(null);
        errorDialog.setContentText(errorMessage);
        errorDialog.showAndWait();
    }
}  