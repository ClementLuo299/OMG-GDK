import gdk.GameModule;
import gdk.Logging;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.application.Platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
 * @edited July 25, 2025
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
    @FXML private Button clearJsonButton;
    @FXML private Button sendMessageButton;
    
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
        
        // Initialize dependencies
        jsonDataMapper = new ObjectMapper();
        availableGameModules = FXCollections.observableArrayList();
        
        // Set up the UI components
        setupUserInterface();
        
        // Set up event handlers
        setupEventHandlers();
        
        // Refresh the game list on startup
        refreshAvailableGameModules();
        
        Logging.info("✅ GDK Game Picker Controller initialized successfully");
    }

    // ==================== DEPENDENCY INJECTION ====================
    
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
        // Game Selector Setup
        
        // Configure the ComboBox to display game module names from metadata
        // This creates a custom cell factory that shows the game name of each game module
        gameSelector.setCellFactory(param -> new ListCell<GameModule>() {
            @Override
            protected void updateItem(GameModule gameModule, boolean empty) {
                super.updateItem(gameModule, empty);
                if (empty || gameModule == null) {
                    // Show placeholder text when no game is selected
                    setText("Select a game...");
                } else {
                    // Display the game name from the GameModule interface
                    setText(gameModule.getGameName());
                }
            }
        });
        
        // Apply the same cell factory to the button cell (the part that shows the selected item)
        // This ensures consistent display between the dropdown list and the selected item
        gameSelector.setButtonCell(gameSelector.getCellFactory().call(null));
        
        // Message Area Setup
        
        // Make the message area read-only so users can't edit the displayed messages
        messageArea.setEditable(false);
        
        // Enable text wrapping so long messages don't extend beyond the visible area
        messageArea.setWrapText(true);
        
        // Apply consistent styling to the message area for better readability
        // Uses a modern font stack with fallbacks and appropriate size
        messageArea.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-font-size: 12px;");
        
        // Display a welcome message to inform users they're in the right place
        addUserMessage("🎮 Welcome to GDK Game Picker!");
        
        // Log successful UI initialization for debugging purposes
        Logging.info("🎨 UI components initialized");
    }

    // ==================== EVENT HANDLER SETUP ====================
    
    /**
     * Set up all event handlers for user interactions.
     * 
     * This method configures event handlers for all interactive
     * components in the lobby interface.
     */
    private void setupEventHandlers() {
        // Game Selection Handler
        // When user selects a game from the dropdown, update the selected game and show feedback
        gameSelector.setOnAction(event -> {
            selectedGameModule = gameSelector.getValue(); // Store the selected game module
            String selectedGameName = selectedGameModule != null ? 
                selectedGameModule.getGameName() : "None"; // Get game name from interface
            addUserMessage("🎮 Selected game: " + selectedGameName); // Show user feedback
        });
        
        // Button Event Handlers
        // Launch button: Start the selected game with validation
        launchGameButton.setOnAction(event -> launchSelectedGame());
        
        // Refresh button: Reload the list of available games
        refreshButton.setOnAction(event -> {
            messageArea.clear(); // Clear the message area first
            addUserMessage("🔄 Refreshing game list..."); // Show user feedback
            refreshAvailableGameModules(); // Reload games from modules directory
        });
        
        // Exit button: Close the entire application
        exitButton.setOnAction(event -> {
            Logging.info("🔒 GDK Game Lobby closing"); // Log the action
            Platform.exit(); // Terminate the JavaFX application
        });
        
        // JSON Configuration Handlers
        // Clear button: Remove all JSON input
        clearJsonButton.setOnAction(event -> clearJsonConfigurationData());
        
        // Send message button: Send a test message
        sendMessageButton.setOnAction(event -> sendMessage());
        
        Logging.info("🎯 Event handlers configured");
    }

    // ==================== GAME MODULE MANAGEMENT ====================
    
    /**
     * Refresh the list of available game modules.
     * 
     * This method scans the modules directory for available game modules
     * and updates the UI accordingly with proper error handling.
     */
    private void refreshAvailableGameModules() {
        try {
            // Clear existing game modules to start fresh
            availableGameModules.clear();
            
            // Get the path to the modules directory from application constants
            String modulesDirectoryPath = GDKApplication.MODULES_DIRECTORY_PATH;
            Logging.info("📂 Scanning for modules in: " + modulesDirectoryPath);
            
            // Use the ModuleLoader to discover all available game modules
            List<GameModule> discoveredGameModules = ModuleLoader.discoverModules(modulesDirectoryPath);
            
            // Add each discovered module to our observable list
            for (GameModule gameModule : discoveredGameModules) {
                availableGameModules.add(gameModule); // Add to observable list for UI binding
                String gameName = gameModule.getGameName();
                String className = gameModule.getClass().getSimpleName();
                Logging.info("📦 Loaded game module: " + gameName + " (" + className + ")");
            }
            
            // Update the ComboBox with the new list of games
            gameSelector.setItems(availableGameModules);
            
            // Provide user feedback about the discovery results
            if (availableGameModules.isEmpty()) {
                addUserMessage("⚠️ No game modules found in " + modulesDirectoryPath);
            } else {
                addUserMessage("✅ Found " + availableGameModules.size() + " game module(s)");
            }
            
        } catch (Exception moduleDiscoveryError) {
            // Handle any errors during module discovery
            Logging.error("❌ Error refreshing game list: " + moduleDiscoveryError.getMessage(), moduleDiscoveryError);
            addUserMessage("❌ Error refreshing game list: " + moduleDiscoveryError.getMessage());
        }
    }

    // ==================== GAME LAUNCHING ====================
    
    /**
     * Launch the currently selected game with validation.
     * 
     * This method validates the game selection and JSON configuration,
     * then delegates the actual launching to the ViewModel.
     */
    private void launchSelectedGame() {
        // Step 1: Validate that a game is selected
        if (selectedGameModule == null) {
            showErrorDialog("No Game Selected", "Please select a game to launch."); // Show error to user
            return; // Exit early if no game is selected
        }

        // Step 2: Parse and validate JSON syntax
        Map<String, Object> jsonConfigurationData = parseJsonConfigurationData();
        if (jsonConfigurationData == null) {
            // Check if it's due to invalid JSON syntax (not empty input)
            String jsonText = jsonDataTextArea.getText().trim();
            if (!jsonText.isEmpty()) {
                showErrorDialog("Invalid JSON", "Please enter valid JSON configuration."); // Show error to user
                return; // Exit early if JSON syntax is invalid
            }
            // If JSON is empty, continue with null data (let game decide)
            addUserMessage("📦 No JSON data provided (game will use its own defaults)");
        } else {
            // Log information about the JSON data being included
            addUserMessage("📦 Including custom JSON data with " + jsonConfigurationData.size() + " fields");
        }
        
        addUserMessage("🚀 Launching " + selectedGameModule.getGameName());
        
        // Step 5: Launch the game using the ViewModel
        if (applicationViewModel != null) {
            applicationViewModel.handleLaunchGame(selectedGameModule); // Delegate to ViewModel
        } else {
            showErrorDialog("Application Error", "ViewModel reference is not available."); // Show error if ViewModel is missing
        }
    }

    // ==================== JSON CONFIGURATION HANDLING ====================
    
    /**
     * Send the JSON text field content as a message to the selected game module.
     * 
     * This method validates JSON syntax and sends the content to the selected game module.
     * It also handles metadata requests and displays the response.
     */
    private void sendMessage() {
        // Check if a game module is selected
        if (selectedGameModule == null) {
            addUserMessage("⚠️ Please select a game module first before sending a message");
            return;
        }
        
        // Get the content from the JSON text area
        String jsonContent = jsonDataTextArea.getText().trim();
        String gameModuleName = selectedGameModule.getGameName();
        
        if (jsonContent.isEmpty()) {
            // If the JSON field is empty, send a placeholder message
            addUserMessage("💬 No content to send to " + gameModuleName + " (JSON field is empty)");
            return;
        }
        
        // Validate JSON syntax before sending
        try {
            // Attempt to parse the JSON to validate its syntax
            Map<String, Object> messageData = jsonDataMapper.readValue(jsonContent, Map.class);
            
            // Display the message being sent
            addUserMessage("💬 Sending to " + gameModuleName + ": " + jsonContent);
            
            // Send the message to the game module
            Map<String, Object> response = selectedGameModule.handleMessage(messageData);
            
            // Handle the response if there is one
            if (response != null) {
                String responseJson = jsonDataMapper.writeValueAsString(response);
                addUserMessage("📋 Response from " + gameModuleName + ": " + responseJson);
            } else {
                addUserMessage("📭 No response from " + gameModuleName);
            }
            
        } catch (JsonProcessingException jsonProcessingError) {
            // If JSON is invalid, notify the user and don't send
            addUserMessage("❌ Invalid JSON syntax - message not sent");
        } catch (Exception e) {
            // Handle any other errors
            addUserMessage("❌ Error sending message: " + e.getMessage());
        }
    }
    
    /**
     * Clear the JSON configuration data text area.
     * 
     * This method clears the JSON input area and provides
     * user feedback about the action.
     */
    private void clearJsonConfigurationData() {
        // Remove all text from the JSON configuration text area
        jsonDataTextArea.clear();
        
        // Provide user feedback about the action
        addUserMessage("🗑️ Cleared JSON data");
    }
    
    /**
     * Parse the JSON configuration data from the text area.
     * 
     * @return The parsed JSON data as a Map, or null if parsing fails or input is empty
     */
    private Map<String, Object> parseJsonConfigurationData() {
        // Get the JSON text from the text area and remove leading/trailing whitespace
        String jsonConfigurationText = jsonDataTextArea.getText().trim();
        
        // If JSON is empty, return null (let the game decide what to do)
        if (jsonConfigurationText.isEmpty()) {
            return null;
        }
        
        try {
            // Parse the JSON text into a Map<String, Object> for easy access
            @SuppressWarnings("unchecked")
            Map<String, Object> configurationData = jsonDataMapper.readValue(jsonConfigurationText, Map.class);
            return configurationData; // Return the parsed configuration
        } catch (JsonProcessingException jsonProcessingError) {
            // Log the parsing error for debugging
            Logging.error("❌ Failed to parse JSON: " + jsonProcessingError.getMessage());
            return null; // Return null to indicate parsing failure
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
        // Create a timestamp in HH:mm:ss format for the message
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        // Append the timestamped message to the message area with a newline
        messageArea.appendText("[" + timestamp + "] " + userMessage + "\n");
        
        // Auto-scroll to the bottom to show the latest message
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
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(dialogTitle);
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }
}  