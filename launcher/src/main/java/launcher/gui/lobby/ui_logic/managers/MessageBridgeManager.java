package launcher.gui.lobby.ui_logic.managers;

import gdk.internal.MessagingBridge;
import javafx.application.Platform;
import launcher.gui.lobby.ui_logic.subcontrollers.JsonConfigurationController;

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
    
    private final JsonConfigurationController jsonConfigurationController;
    private final JsonConfigurationHandler jsonConfigurationHandler;
    
    /**
     * Create a new MessageBridgeManager.
     * 
     * @param jsonConfigurationController The JSON configuration controller
     * @param jsonConfigurationHandler The JSON configuration handler for formatting
     */
    public MessageBridgeManager(JsonConfigurationController jsonConfigurationController,
                                JsonConfigurationHandler jsonConfigurationHandler) {
        this.jsonConfigurationController = jsonConfigurationController;
        this.jsonConfigurationHandler = jsonConfigurationHandler;
    }
    
    /**
     * Subscribe to game end messages and mirror them to JSON output editor.
     */
    public void subscribeToEndMessageMirror() {
        try {
            MessagingBridge.addConsumer(msg -> {
                try {
                    // Record the message to the transcript
                    launcher.utils.game.TranscriptRecorder.recordFromGame(msg);
                    
                    Object fn = (msg != null) ? msg.get("function") : null;
                    if (fn != null && "end".equals(String.valueOf(fn)) && jsonConfigurationController != null) {
                        String pretty = jsonConfigurationHandler.formatJsonResponse((Map<String, Object>) msg);
                        Platform.runLater(() -> jsonConfigurationController.getJsonOutputEditor().setText(pretty));
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

