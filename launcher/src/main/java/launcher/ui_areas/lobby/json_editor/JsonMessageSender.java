package launcher.ui_areas.lobby.json_editor;

import gdk.api.GameModule;
import launcher.features.json_processing.JsonFormatter;
import launcher.features.json_processing.JsonParser;
import launcher.features.game_messaging.SendMessageToGame;

import java.util.Map;

/**
 * Handles UI coordination for sending messages to game modules from JSON editor.
 * 
 * <p>This class handles UI-related operations:
 * <ul>
 *   <li>Retrieving JSON content from UI editor</li>
 *   <li>Displaying formatted responses in UI editor</li>
 *   <li>Reporting success/failure messages to users</li>
 * </ul>
 * 
 * <p>Business logic (JSON parsing, formatting) is delegated to {@link JsonParser} and {@link JsonFormatter}.
 * Message sending is delegated to {@link SendMessageToGame}.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class JsonMessageSender {
    
    // ==================== DEPENDENCIES ====================
    
    /** Input JSON editor UI component. */
    private final JsonEditor jsonInputEditor;
    
    /** Output JSON editor UI component. */
    private final JsonEditor jsonOutputEditor;
    
    /** Callback for reporting messages to the UI. */
    private final JsonEditorOperations.MessageReporter messageReporter;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new JsonMessageSender.
     * 
     * @param jsonInputEditor The input JSON editor
     * @param jsonOutputEditor The output JSON editor
     * @param messageReporter Callback to report messages to the UI
     */
    public JsonMessageSender(JsonEditor jsonInputEditor,
                            JsonEditor jsonOutputEditor,
                            JsonEditorOperations.MessageReporter messageReporter) {
        this.jsonInputEditor = jsonInputEditor;
        this.jsonOutputEditor = jsonOutputEditor;
        this.messageReporter = messageReporter;
    }
    
    /**
     * Send the JSON text field content as a message to the selected game module.
     * 
     * @param selectedGameModule The currently selected game module
     */
    public void sendMessage(GameModule selectedGameModule) {
        // Check if a game module is selected
        if (selectedGameModule == null) {
            messageReporter.addMessage("Please select a game module first before sending a message");
            return;
        }
        
        // Get the content from the JSON input area
        String jsonContent = jsonInputEditor.getText().trim();
        String gameModuleName = selectedGameModule.getMetadata().getGameName();
        
        if (jsonContent.isEmpty()) {
            // If the JSON field is empty, send a placeholder message
            messageReporter.addMessage("No content to send to " + gameModuleName + " (JSON field is empty)");
            return;
        }
        
        // Parse JSON using JsonParser
        Map<String, Object> messageData = JsonParser.parse(jsonContent);
        
        if (messageData == null) {
            messageReporter.addMessage("Invalid JSON syntax - message not sent");
            return;
        }
        
        // Send message using business logic handler
        try {
            Map<String, Object> response = SendMessageToGame.sendMessage(selectedGameModule, messageData);
            
            // Handle the response if there is one
            if (response != null) {
                // Format response using JsonFormatter
                String responseJson = JsonFormatter.format(response);
                
                // Display in the output area (UI operation)
                jsonOutputEditor.setText(responseJson);
                
                messageReporter.addMessage("Message sent successfully to " + gameModuleName + " - Response received");
            } else {
                messageReporter.addMessage("No response from " + gameModuleName);
            }
        } catch (Exception e) {
            messageReporter.addMessage("Error: " + e.getMessage());
        }
    }
}

