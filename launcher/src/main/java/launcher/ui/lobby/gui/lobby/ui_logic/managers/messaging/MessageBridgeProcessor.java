package launcher.ui.lobby.gui.lobby.ui_logic.managers.messaging;

import javafx.application.Platform;
import launcher.features.messaging.MessageProcessingService;
import launcher.ui.lobby.json_editor.JsonEditor;

import java.util.Map;

/**
 * Handles UI updates for messages from the messaging bridge.
 * 
 * <p>This processor handles UI-related operations:
 * <ul>
 *   <li>Updating JSON output editor with formatted messages</li>
 *   <li>Ensuring thread safety (JavaFX application thread)</li>
 * </ul>
 * 
 * <p>Business logic (transcript recording, JSON formatting) is delegated to
 * {@link MessageProcessingService}.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class MessageBridgeProcessor {
    
    // ==================== DEPENDENCIES ====================
    
    /** Business logic service for message processing. */
    private final MessageProcessingService messageProcessingService;
    
    /** JSON output editor UI component. */
    private final JsonEditor jsonOutputEditor;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new MessageBridgeProcessor.
     * 
     * @param messageProcessingService The business logic service for message processing
     * @param jsonOutputEditor The JSON output editor to update
     */
    public MessageBridgeProcessor(MessageProcessingService messageProcessingService,
                                 JsonEditor jsonOutputEditor) {
        this.messageProcessingService = messageProcessingService;
        this.jsonOutputEditor = jsonOutputEditor;
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Processes a message from the game and updates UI.
     * 
     * <p>This method:
     * <ol>
     *   <li>Delegates transcript recording to business service</li>
     *   <li>Checks if message is an "end" message (delegates to business service)</li>
     *   <li>Formats and displays message in UI if needed</li>
     * </ol>
     * 
     * @param msg The message map from the game
     */
    public void processMessage(Map<String, Object> msg) {
        try {
            // Record the message to the transcript (business logic)
            messageProcessingService.recordMessageToTranscript(msg);
            
            // Check if it's an "end" message and update UI if needed
            if (messageProcessingService.isEndMessage(msg) && jsonOutputEditor != null) {
                // Format using business service
                String pretty = messageProcessingService.formatJsonResponse(msg);
                // Update UI on JavaFX thread
                Platform.runLater(() -> jsonOutputEditor.setText(pretty));
            }
        } catch (Exception ignored) {
            // Silently ignore errors in message processing
        }
    }
}

