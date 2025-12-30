package launcher.gui.lobby.ui_logic.managers.json;

import launcher.gui.json_editor.JsonEditor;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.JsonFormatter;
import launcher.gui.lobby.ui_logic.managers.json.JsonMessageSender;

import java.util.Map;

/**
 * Handles JSON editor UI operations.
 * Manages clearing, filling, and displaying JSON content in editors.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class JsonEditorOperations {
    
    /**
     * Interface for reporting messages to the UI.
     */
    public interface MessageReporter {
        void addMessage(String message);
    }
    
    private final GDKViewModel viewModel;
    private final JsonEditor jsonInputEditor;
    private final JsonEditor jsonOutputEditor;
    private final MessageReporter messageReporter;
    private final JsonMessageSender messageSender;
    
    /**
     * Create a new JsonEditorOperations.
     * 
     * @param viewModel The ViewModel for business logic (may be null initially)
     * @param jsonInputEditor The input JSON editor
     * @param jsonOutputEditor The output JSON editor
     * @param messageReporter Callback to report messages to the UI
     */
    public JsonEditorOperations(GDKViewModel viewModel,
                                JsonEditor jsonInputEditor, 
                                JsonEditor jsonOutputEditor, 
                                MessageReporter messageReporter) {
        this.viewModel = viewModel;
        this.jsonInputEditor = jsonInputEditor;
        this.jsonOutputEditor = jsonOutputEditor;
        this.messageReporter = messageReporter;
        this.messageSender = new JsonMessageSender(viewModel, jsonInputEditor, jsonOutputEditor, messageReporter);
    }
    
    /**
     * Send the JSON text field content as a message to the selected game module.
     * Delegates to JsonMessageSender.
     * 
     * @param selectedGameModule The currently selected game module
     */
    public void sendMessage(gdk.api.GameModule selectedGameModule) {
        messageSender.sendMessage(selectedGameModule);
    }
    
    
    /**
     * Clear the JSON input data text area.
     */
    public void clearJsonInputData() {
        jsonInputEditor.clear();
        messageReporter.addMessage("🗑️ Cleared JSON input data");
    }
    
    /**
     * Clear the JSON output data text area.
     */
    public void clearJsonOutputData() {
        jsonOutputEditor.clear();
        messageReporter.addMessage("🗑️ Cleared JSON output data");
    }
    
    /**
     * Fill the JSON text area with a metadata request.
     */
    public void fillMetadataRequest() {
        // Create a standard metadata request JSON
        String metadataRequest = "{\n  \"function\": \"metadata\"\n}";
        
        // Set the JSON input area content
        jsonInputEditor.setText(metadataRequest);
        
        // Provide user feedback about the action
        messageReporter.addMessage("📋 Filled JSON input with metadata request");
    }
    
    /**
     * Parse the JSON configuration data from the text area.
     * Delegates to ViewModel for business logic.
     * 
     * @return The parsed JSON data as a Map, or null if parsing fails or input is empty
     */
    public Map<String, Object> parseJsonConfigurationData() {
        // Get the JSON text from the text area
        String jsonConfigurationText = jsonInputEditor.getText().trim();
        
        // Delegate parsing to ViewModel (business logic)
        if (viewModel != null) {
            return viewModel.parseJsonConfiguration(jsonConfigurationText);
        }
        
        return null;
    }
    
    /**
     * Format and display a JSON response in the output editor.
     * 
     * @param response The response map to format and display
     */
    public void displayJsonResponse(Map<String, Object> response) {
        String formattedJson = JsonFormatter.formatJsonResponse(response);
        jsonOutputEditor.setText(formattedJson);
    }
}

