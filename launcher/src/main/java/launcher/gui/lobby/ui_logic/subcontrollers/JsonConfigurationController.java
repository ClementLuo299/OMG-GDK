package launcher.gui.lobby.ui_logic.subcontrollers;

import gdk.api.GameModule;
import launcher.gui.json_editor.JsonEditor;
import launcher.gui.lobby.ui_logic.managers.JsonConfigurationHandler;
import launcher.gui.lobby.persistence.JsonPersistenceManager;
import launcher.gui.lobby.ui_logic.managers.MessageManager;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;

/**
 * Subcontroller for the JSON Configuration UI area.
 * 
 * Handles JSON input/output editors, buttons, and persistence toggle.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class JsonConfigurationController {
    
    // UI Components
    private final VBox jsonInputEditorContainer;
    private final VBox jsonOutputEditorContainer;
    private final JsonEditor jsonInputEditor;
    private final JsonEditor jsonOutputEditor;
    private final Button clearInputButton;
    private final Button clearOutputButton;
    private final Button metadataRequestButton;
    private final Button sendMessageButton;
    private final JFXToggleButton jsonPersistenceToggle;
    
    // Managers
    private final JsonConfigurationHandler jsonConfigurationHandler;
    private final JsonPersistenceManager jsonPersistenceManager;
    private final MessageManager messageManager;
    
    // State
    private GameModule selectedGameModule;
    
    /**
     * Create a new JsonConfigurationController.
     * 
     * @param jsonInputEditorContainer Container for input editor
     * @param jsonOutputEditorContainer Container for output editor
     * @param jsonInputEditor The input JSON editor
     * @param jsonOutputEditor The output JSON editor
     * @param clearInputButton Button to clear input
     * @param clearOutputButton Button to clear output
     * @param metadataRequestButton Button to fill metadata request
     * @param sendMessageButton Button to send message
     * @param jsonPersistenceToggle Toggle for JSON persistence
     * @param jsonConfigurationHandler The JSON configuration handler
     * @param jsonPersistenceManager The JSON persistence manager
     * @param messageManager The message manager
     */
    public JsonConfigurationController(
            VBox jsonInputEditorContainer,
            VBox jsonOutputEditorContainer,
            JsonEditor jsonInputEditor,
            JsonEditor jsonOutputEditor,
            Button clearInputButton,
            Button clearOutputButton,
            Button metadataRequestButton,
            Button sendMessageButton,
            JFXToggleButton jsonPersistenceToggle,
            JsonConfigurationHandler jsonConfigurationHandler,
            JsonPersistenceManager jsonPersistenceManager,
            MessageManager messageManager) {
        
        this.jsonInputEditorContainer = jsonInputEditorContainer;
        this.jsonOutputEditorContainer = jsonOutputEditorContainer;
        this.jsonInputEditor = jsonInputEditor;
        this.jsonOutputEditor = jsonOutputEditor;
        this.clearInputButton = clearInputButton;
        this.clearOutputButton = clearOutputButton;
        this.metadataRequestButton = metadataRequestButton;
        this.sendMessageButton = sendMessageButton;
        this.jsonPersistenceToggle = jsonPersistenceToggle;
        this.jsonConfigurationHandler = jsonConfigurationHandler;
        this.jsonPersistenceManager = jsonPersistenceManager;
        this.messageManager = messageManager;
    }
    
    /**
     * Set the currently selected game module.
     * 
     * @param gameModule The selected game module
     */
    public void setSelectedGameModule(GameModule gameModule) {
        this.selectedGameModule = gameModule;
    }
    
    /**
     * Get the JSON input editor.
     * 
     * @return The input editor
     */
    public JsonEditor getJsonInputEditor() {
        return jsonInputEditor;
    }
    
    /**
     * Get the JSON output editor.
     * 
     * @return The output editor
     */
    public JsonEditor getJsonOutputEditor() {
        return jsonOutputEditor;
    }
    
    /**
     * Parse JSON configuration data from the input editor.
     * 
     * @return The parsed JSON data, or null if parsing fails or input is empty
     */
    public java.util.Map<String, Object> parseJsonConfigurationData() {
        return jsonConfigurationHandler != null ? 
            jsonConfigurationHandler.parseJsonConfigurationData() : null;
    }
    
    /**
     * Initialize the JSON configuration UI and event handlers.
     */
    public void initialize() {
        // Add the editors to their containers
        jsonInputEditorContainer.getChildren().add(jsonInputEditor);
        jsonOutputEditorContainer.getChildren().add(jsonOutputEditor);
        
        // Ensure containers can grow vertically
        javafx.scene.layout.VBox.setVgrow(jsonInputEditorContainer, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.VBox.setVgrow(jsonOutputEditorContainer, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.VBox.setVgrow(jsonInputEditor, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.VBox.setVgrow(jsonOutputEditor, javafx.scene.layout.Priority.ALWAYS);
        
        // Clear input button: Remove all JSON input
        clearInputButton.setOnAction(event -> jsonConfigurationHandler.clearJsonInputData());
        
        // Clear output button: Remove all JSON output
        clearOutputButton.setOnAction(event -> jsonConfigurationHandler.clearJsonOutputData());
        
        // Metadata request button: Fill JSON with metadata request
        metadataRequestButton.setOnAction(event -> jsonConfigurationHandler.fillMetadataRequest());
        
        // Send message button: Send a test message
        sendMessageButton.setOnAction(event -> jsonConfigurationHandler.sendMessage(selectedGameModule));
        
        // Persistence Toggle Handler
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
        
        // JSON Text Area Change Handler - Save JSON content when modified (with debouncing)
        jsonInputEditor.textProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (jsonPersistenceToggle.isSelected()) {
                    jsonPersistenceManager.saveJsonContent();
                }
            });
        });
    }
}

