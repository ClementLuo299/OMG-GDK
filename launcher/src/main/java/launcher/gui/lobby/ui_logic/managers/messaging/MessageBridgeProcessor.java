package launcher.gui.lobby.ui_logic.managers.messaging;

import javafx.application.Platform;
import launcher.gui.lobby.JsonFormatter;
import launcher.gui.lobby.TranscriptManager;
import launcher.gui.json_editor.JsonEditor;

import java.util.Map;

/**
 * Processes messages from the messaging bridge.
 * Handles transcript recording, JSON formatting, and UI updates.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class MessageBridgeProcessor {
    
    private final JsonEditor jsonOutputEditor;
    
    /**
     * Create a new MessageBridgeProcessor.
     * 
     * @param jsonOutputEditor The JSON output editor to update
     */
    public MessageBridgeProcessor(JsonEditor jsonOutputEditor) {
        this.jsonOutputEditor = jsonOutputEditor;
    }
    
    /**
     * Process a message from the game.
     * Records to transcript, formats, and updates UI if it's an "end" message.
     * 
     * @param msg The message map from the game
     */
    public void processMessage(Map<String, Object> msg) {
        try {
            // Record the message to the transcript (business logic)
            TranscriptManager.recordFromGame(msg);
            
            Object fn = (msg != null) ? msg.get("function") : null;
            if (fn != null && "end".equals(String.valueOf(fn)) && jsonOutputEditor != null) {
                // Format using business logic formatter
                String pretty = JsonFormatter.formatJsonResponse(msg);
                Platform.runLater(() -> jsonOutputEditor.setText(pretty));
            }
        } catch (Exception ignored) {
            // Silently ignore errors in message processing
        }
    }
}

