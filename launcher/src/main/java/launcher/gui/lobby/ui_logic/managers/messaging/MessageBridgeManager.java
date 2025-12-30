package launcher.gui.lobby.ui_logic.managers.messaging;

import gdk.internal.MessagingBridge;
import launcher.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;

import java.util.Map;

/**
 * Manages messaging bridge subscriptions for the lobby.
 * Handles subscription to game messages and delegates processing.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class MessageBridgeManager {
    
    private final MessageBridgeProcessor messageProcessor;
    
    /**
     * Create a new MessageBridgeManager.
     * 
     * @param jsonActionButtonsController The JSON action buttons controller
     */
    public MessageBridgeManager(JsonActionButtonsController jsonActionButtonsController) {
        this.messageProcessor = new MessageBridgeProcessor(
            jsonActionButtonsController != null ? jsonActionButtonsController.getJsonOutputEditor() : null
        );
    }
    
    /**
     * Subscribe to game end messages and mirror them to JSON output editor.
     */
    public void subscribeToEndMessageMirror() {
        try {
            MessagingBridge.addConsumer(msg -> {
                messageProcessor.processMessage((Map<String, Object>) msg);
            });
        } catch (Exception ignored) {
            // Silently ignore subscription errors
        }
    }
}

