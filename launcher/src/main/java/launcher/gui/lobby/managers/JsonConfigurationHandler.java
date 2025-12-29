package launcher.gui.lobby.managers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.gui.json_editor.JsonEditor;
import launcher.gui.lobby.GDKViewModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Handles JSON configuration UI operations.
 * Delegates business logic (parsing, validation) to ViewModel.
 * 
 * @authors Clement Luo
 * @date December 27, 2025
 * @edited January 2025
 * @since 1.0
 */
public class JsonConfigurationHandler {
    
    /**
     * Interface for reporting messages to the UI.
     */
    public interface MessageReporter {
        void addMessage(String message);
    }
    
    private final GDKViewModel viewModel;
    private final ObjectMapper jsonDataMapper;
    private final JsonEditor jsonInputEditor;
    private final JsonEditor jsonOutputEditor;
    private final MessageReporter messageReporter;
    
    /**
     * Create a new JsonConfigurationHandler.
     * 
     * @param viewModel The ViewModel for business logic (may be null initially)
     * @param jsonInputEditor The input JSON editor
     * @param jsonOutputEditor The output JSON editor
     * @param messageReporter Callback to report messages to the UI
     */
    public JsonConfigurationHandler(GDKViewModel viewModel,
                                   JsonEditor jsonInputEditor, 
                                   JsonEditor jsonOutputEditor, 
                                   MessageReporter messageReporter) {
        this.viewModel = viewModel;
        this.jsonDataMapper = new ObjectMapper();
        this.jsonInputEditor = jsonInputEditor;
        this.jsonOutputEditor = jsonOutputEditor;
        this.messageReporter = messageReporter;
    }
    
    /**
     * Update the ViewModel reference (called when ViewModel is set).
     */
    public void setViewModel(GDKViewModel viewModel) {
        // Note: ViewModel is final, so we can't update it. This method is kept for API compatibility.
        // In practice, JsonConfigurationHandler should be recreated when ViewModel changes.
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
        
        // Validate JSON syntax before sending using ViewModel (business logic)
        Map<String, Object> messageData = null;
        if (viewModel != null) {
            messageData = viewModel.parseJsonConfiguration(jsonContent);
        } else {
            // Fallback parsing if ViewModel not available
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = jsonDataMapper.readValue(jsonContent, Map.class);
                messageData = parsed;
            } catch (JsonProcessingException e) {
                messageReporter.addMessage("❌ Invalid JSON syntax - message not sent");
                return;
            }
        }
        
        if (messageData == null) {
            messageReporter.addMessage("❌ Invalid JSON syntax - message not sent");
            return;
        }
        
        try {
            
            // Record message to transcript before sending
            launcher.utils.game.TranscriptRecorder.recordToGame(messageData);
            
            // Send the message to the game module
            Map<String, Object> response = selectedGameModule.handleMessage(messageData);
            
            // Handle the response if there is one
            if (response != null) {
                String responseJson = formatJsonResponse(response);
                
                // Display in the output area
                jsonOutputEditor.setText(responseJson);
                
                // Record response to transcript
                launcher.utils.game.TranscriptRecorder.recordFromGame(response);
                
                // Add status message to message area
                messageReporter.addMessage("✅ Message sent successfully to " + gameModuleName + " - Response received");
            } else {
                messageReporter.addMessage("📭 No response from " + gameModuleName);
            }
            
        } catch (Exception e) {
            // Handle any other errors
            messageReporter.addMessage("❌ Error sending message: " + e.getMessage());
        }
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
     * Format a JSON response for better display.
     * 
     * @param response The response map to format
     * @return A formatted JSON string with proper indentation
     */
    public String formatJsonResponse(Map<String, Object> response) {
        try {
            // Use pretty printing for better readability
            return jsonDataMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (JsonProcessingException e) {
            // Fallback to simple formatting if pretty printing fails
            try {
                return jsonDataMapper.writeValueAsString(response);
            } catch (JsonProcessingException ex) {
                return "Error formatting response: " + ex.getMessage();
            }
        }
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
        
        // Fallback to local parsing if ViewModel not available (shouldn't happen)
        if (jsonConfigurationText.isEmpty()) {
            return null;
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
}

