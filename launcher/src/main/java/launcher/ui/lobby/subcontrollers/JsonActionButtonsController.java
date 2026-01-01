package launcher.ui.lobby.subcontrollers;

import gdk.api.GameModule;
import launcher.ui.lobby.json_editor.JsonEditor;
import launcher.ui.lobby.managers.json.JsonEditorOperations;
import launcher.features.lobby_features.business.JsonPersistenceManager;
import launcher.ui.lobby.managers.messaging.MessageManager;
import javafx.scene.control.Button;
import com.jfoenix.controls.JFXToggleButton;

/**
 * Subcontroller for the JSON editor action buttons.
 * 
 * Handles only the buttons under the JSON editors:
 * - Clear input button
 * - Request metadata button
 * - Send message button
 * - Save JSON toggle (persistence toggle)
 * - Clear output button
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class JsonActionButtonsController {
    
    // UI Components - Buttons only
    private final Button clearInputButton;
    private final Button clearOutputButton;
    private final Button metadataRequestButton;
    private final Button sendMessageButton;
    private final JFXToggleButton jsonPersistenceToggle;
    
    // References needed for button handlers
    private final JsonEditor jsonInputEditor;
    private final JsonEditor jsonOutputEditor;
    
    // Managers
    private final JsonEditorOperations jsonEditorOperations;
    private final JsonPersistenceManager jsonPersistenceManager;
    private final MessageManager messageManager;
    
    // State
    private GameModule selectedGameModule;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a new JsonActionButtonsController.
     * 
     * @param jsonInputEditor The input JSON editor (needed for operations)
     * @param jsonOutputEditor The output JSON editor (needed for operations)
     * @param clearInputButton Button to clear input
     * @param clearOutputButton Button to clear output
     * @param metadataRequestButton Button to fill metadata request
     * @param sendMessageButton Button to send message
     * @param jsonPersistenceToggle Toggle for JSON persistence (save JSON)
     * @param jsonEditorOperations The JSON editor operations handler
     * @param jsonPersistenceManager The JSON persistence manager
     * @param messageManager The message manager
     */
    public JsonActionButtonsController(
            JsonEditor jsonInputEditor,
            JsonEditor jsonOutputEditor,
            Button clearInputButton,
            Button clearOutputButton,
            Button metadataRequestButton,
            Button sendMessageButton,
            JFXToggleButton jsonPersistenceToggle,
            JsonEditorOperations jsonEditorOperations,
            JsonPersistenceManager jsonPersistenceManager,
            MessageManager messageManager) {
        
        this.jsonInputEditor = jsonInputEditor;
        this.jsonOutputEditor = jsonOutputEditor;
        this.clearInputButton = clearInputButton;
        this.clearOutputButton = clearOutputButton;
        this.metadataRequestButton = metadataRequestButton;
        this.sendMessageButton = sendMessageButton;
        this.jsonPersistenceToggle = jsonPersistenceToggle;
        this.jsonEditorOperations = jsonEditorOperations;
        this.jsonPersistenceManager = jsonPersistenceManager;
        this.messageManager = messageManager;
    }
    
    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize the button event handlers.
     * Sets up action handlers for all JSON editor action buttons.
     */
    public void initialize() {
        // Clear input button: Remove all JSON input
        clearInputButton.setOnAction(event -> jsonEditorOperations.clearJsonInputData());
        
        // Clear output button: Remove all JSON output
        clearOutputButton.setOnAction(event -> jsonEditorOperations.clearJsonOutputData());
        
        // Metadata request button: Fill JSON with metadata request
        metadataRequestButton.setOnAction(event -> jsonEditorOperations.fillMetadataRequest());
        
        // Send message button: Send a test message
        sendMessageButton.setOnAction(event -> jsonEditorOperations.sendMessage(selectedGameModule));
        
        // Save JSON toggle (persistence toggle): Enable/disable JSON persistence
        jsonPersistenceToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Skip messages during startup loading
            if (jsonPersistenceManager.isLoadingPersistenceSettings()) {
                return;
            }
            
            boolean isEnabled = newValue;
            jsonPersistenceManager.savePersistenceToggleState();
            
            if (!isEnabled) {
                jsonPersistenceManager.clearJsonPersistenceFile();
                messageManager.addMessage("📋 JSON persistence disabled");
            } else {
                messageManager.addMessage("📋 JSON persistence enabled");
            }
        });
    }
    
    // ==================== STATE MANAGEMENT ====================
    
    /**
     * Set the currently selected game module.
     * Needed for send message button functionality.
     * Called by GameSelectionController when a game is selected.
     * 
     * @param gameModule The selected game module
     */
    public void setSelectedGameModule(GameModule gameModule) {
        this.selectedGameModule = gameModule;
    }
    
    // ==================== PUBLIC API ====================
    
    /**
     * Get the JSON input editor.
     * Exposed for use by other managers (e.g., GameLaunchManager).
     * 
     * @return The input editor
     */
    public JsonEditor getJsonInputEditor() {
        return jsonInputEditor;
    }
    
    /**
     * Get the JSON output editor.
     * Exposed for use by other managers (e.g., MessageBridgeManager).
     * 
     * @return The output editor
     */
    public JsonEditor getJsonOutputEditor() {
        return jsonOutputEditor;
    }
    
    /**
     * Parse JSON configuration data from the input editor.
     * Exposed for use by other managers (e.g., GameLaunchManager).
     * Delegates to JsonEditorOperations for parsing logic.
     * 
     * @return The parsed JSON data, or null if parsing fails or input is empty
     */
    public java.util.Map<String, Object> parseJsonConfigurationData() {
        return jsonEditorOperations != null ? 
            jsonEditorOperations.parseJsonConfigurationData() : null;
    }
}

