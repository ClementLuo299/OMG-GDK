package launcher.gui.lobby.ui_logic.managers;

import gdk.internal.MessagingBridge;
import javafx.application.Platform;
import launcher.gui.lobby.JsonFormatter;
import launcher.gui.lobby.TranscriptManager;
import launcher.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;

import java.util.Map;

/**
 * Manages messaging bridge subscriptions for the lobby.
 * Handles game end message mirroring to JSON output editor.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class MessageBridgeManager {
    
    private final JsonActionButtonsController jsonActionButtonsController;
    
    /**
     * Create a new MessageBridgeManager.
     * 
     * @param jsonActionButtonsController The JSON action buttons controller
     */
    public MessageBridgeManager(JsonActionButtonsController jsonActionButtonsController) {
        this.jsonActionButtonsController = jsonActionButtonsController;
    }
    
    /**
     * Subscribe to game end messages and mirror them to JSON output editor.
     */
    public void subscribeToEndMessageMirror() {
        try {
            MessagingBridge.addConsumer(msg -> {
                try {
                    // Record the message to the transcript (business logic)
                    TranscriptManager.recordFromGame((Map<String, Object>) msg);
                    
                    Object fn = (msg != null) ? msg.get("function") : null;
                    if (fn != null && "end".equals(String.valueOf(fn)) && jsonActionButtonsController != null) {
                        // Format using business logic formatter
                        String pretty = JsonFormatter.formatJsonResponse((Map<String, Object>) msg);
                        Platform.runLater(() -> jsonActionButtonsController.getJsonOutputEditor().setText(pretty));
                    }
                } catch (Exception ignored) {
                    // Silently ignore errors in message processing
                }
            });
        } catch (Exception ignored) {
            // Silently ignore subscription errors
        }
    }
}

