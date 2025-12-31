package launcher.features.lobby_features.managers.json;

import launcher.features.json_processing.JsonProcessingService;
import launcher.ui.lobby.json_editor.JsonEditor;

import java.util.Map;

/**
 * Handles JSON editor UI operations.
 * 
 * <p>This class handles UI-related operations:
 * <ul>
 *   <li>Clearing JSON editor content</li>
 *   <li>Filling JSON editor with template content</li>
 *   <li>Displaying formatted JSON in editors</li>
 *   <li>Reporting user actions via messages</li>
 * </ul>
 * 
 * <p>Business logic (JSON parsing, formatting) is delegated to {@link JsonProcessingService}.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class JsonEditorOperations {
    
    /**
     * Interface for reporting messages to the UI.
     */
    public interface MessageReporter {
        void addMessage(String message);
    }
    
    // ==================== DEPENDENCIES ====================
    
    /** Business logic service for JSON processing. */
    private final JsonProcessingService jsonProcessingService;
    
    /** Input JSON editor UI component. */
    private final JsonEditor jsonInputEditor;
    
    /** Output JSON editor UI component. */
    private final JsonEditor jsonOutputEditor;
    
    /** Callback for reporting messages to the UI. */
    private final MessageReporter messageReporter;
    
    /** Message sender for sending JSON messages. */
    private final JsonMessageSender messageSender;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new JsonEditorOperations.
     * 
     * @param jsonProcessingService The business logic service for JSON processing
     * @param jsonInputEditor The input JSON editor
     * @param jsonOutputEditor The output JSON editor
     * @param messageReporter Callback to report messages to the UI
     */
    public JsonEditorOperations(JsonProcessingService jsonProcessingService,
                                JsonEditor jsonInputEditor, 
                                JsonEditor jsonOutputEditor, 
                                MessageReporter messageReporter) {
        this.jsonProcessingService = jsonProcessingService;
        this.jsonInputEditor = jsonInputEditor;
        this.jsonOutputEditor = jsonOutputEditor;
        this.messageReporter = messageReporter;
        this.messageSender = new JsonMessageSender(jsonProcessingService, jsonInputEditor, jsonOutputEditor, messageReporter);
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
        messageReporter.addMessage("Cleared JSON input data");
    }
    
    /**
     * Clear the JSON output data text area.
     */
    public void clearJsonOutputData() {
        jsonOutputEditor.clear();
        messageReporter.addMessage("Cleared JSON output data");
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
        messageReporter.addMessage("Filled JSON input with metadata request");
    }
    
    /**
     * Parses the JSON configuration data from the text area.
     * Delegates to business service for parsing.
     * 
     * @return The parsed JSON data as a Map, or null if parsing fails or input is empty
     */
    public Map<String, Object> parseJsonConfigurationData() {
        // Get the JSON text from the UI text area
        String jsonConfigurationText = jsonInputEditor.getText().trim();
        
        // Delegate parsing to business service
        return jsonProcessingService.parseJsonConfiguration(jsonConfigurationText);
    }
    
    /**
     * Formats and displays a JSON response in the output editor.
     * 
     * @param response The response map to format and display
     */
    public void displayJsonResponse(Map<String, Object> response) {
        // Format using business service
        String formattedJson = jsonProcessingService.formatJsonResponse(response);
        // Update UI
        jsonOutputEditor.setText(formattedJson);
    }
}

