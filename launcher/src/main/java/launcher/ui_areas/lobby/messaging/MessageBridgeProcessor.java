package launcher.ui_areas.lobby.messaging;

import javafx.application.Platform;
import launcher.features.transcript_recording.recording.RecordInboundMessage;
import launcher.features.json_processing.JsonFormatter;
import launcher.ui_areas.lobby.json_editor.JsonEditor;

import java.util.Map;

/**
 * Handles UI updates for messages from the messaging bridge.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class MessageBridgeProcessor {
    
    private final JsonEditor jsonOutputEditor;
    
    public MessageBridgeProcessor(JsonEditor jsonOutputEditor) {
        this.jsonOutputEditor = jsonOutputEditor;
    }
    
    /**
     * Processes a message from the game and updates UI.
     */
    public void processMessage(Map<String, Object> msg) {
        try {
            RecordInboundMessage.record(msg);
            
            // Check if it's an "end" message and update UI if needed
            Object function = msg != null ? msg.get("function") : null;
            if ("end".equals(function) && jsonOutputEditor != null) {
                String pretty = JsonFormatter.format(msg);
                Platform.runLater(() -> jsonOutputEditor.setText(pretty));
            }
        } catch (Exception ignored) {
            // Silently ignore errors in message processing
        }
    }
}

