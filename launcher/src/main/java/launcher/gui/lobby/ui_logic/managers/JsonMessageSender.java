package launcher.gui.lobby.ui_logic.managers;

import gdk.api.GameModule;
import launcher.gui.json_editor.JsonEditor;
import launcher.gui.lobby.GameMessageHandler;
import launcher.gui.lobby.JsonFormatter;
import launcher.gui.lobby.GDKViewModel;

import java.util.Map;

/**
 * Handles sending messages to game modules from JSON editor.
 * Manages message parsing, sending, and response handling.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class JsonMessageSender {
    
    
    private final GDKViewModel viewModel;
    private final JsonEditor jsonInputEditor;
    private final JsonEditor jsonOutputEditor;
    private final JsonEditorOperations.MessageReporter messageReporter;
    
    /**
     * Create a new JsonMessageSender.
     * 
     * @param viewModel The ViewModel for business logic (may be null initially)
     * @param jsonInputEditor The input JSON editor
     * @param jsonOutputEditor The output JSON editor
     * @param messageReporter Callback to report messages to the UI
     */
    public JsonMessageSender(GDKViewModel viewModel,
                            JsonEditor jsonInputEditor,
                            JsonEditor jsonOutputEditor,
                            JsonEditorOperations.MessageReporter messageReporter) {
        this.viewModel = viewModel;
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
            messageReporter.addMessage("⚠️ Please select a game module first before sending a message");
            return;
        }
        
        // Get the content from the JSON input area
        String jsonContent = jsonInputEditor.getText().trim();
        String gameModuleName = selectedGameModule.getMetadata().getGameName();
        
        if (jsonContent.isEmpty()) {
            // If the JSON field is empty, send a placeholder message
            messageReporter.addMessage("💬 No content to send to " + gameModuleName + " (JSON field is empty)");
            return;
        }
        
        // Parse JSON using ViewModel (business logic)
        Map<String, Object> messageData = null;
        if (viewModel != null) {
            messageData = viewModel.parseJsonConfiguration(jsonContent);
        }
        
        if (messageData == null) {
            messageReporter.addMessage("❌ Invalid JSON syntax - message not sent");
            return;
        }
        
        // Send message using business logic handler
        GameMessageHandler.MessageResult result = GameMessageHandler.sendMessage(selectedGameModule, messageData);
        
        if (!result.isSuccess()) {
            messageReporter.addMessage("❌ " + result.getErrorMessage());
            return;
        }
        
        // Handle the response if there is one
        if (result.hasResponse()) {
            // Format response using business logic formatter
            String responseJson = JsonFormatter.formatJsonResponse(result.getResponse());
            
            // Display in the output area (UI operation)
            jsonOutputEditor.setText(responseJson);
            
            messageReporter.addMessage("✅ Message sent successfully to " + gameModuleName + " - Response received");
        } else {
            messageReporter.addMessage("📭 No response from " + gameModuleName);
        }
    }
}

