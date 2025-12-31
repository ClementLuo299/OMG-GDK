package launcher.ui.lobby.gui.lobby.ui_logic.managers.messaging;

import gdk.internal.MessagingBridge;
import launcher.features.messaging.MessageProcessingService;
import launcher.ui.lobby.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;

import java.util.Map;

/**
 * Manages messaging bridge subscriptions for the lobby.
 * Handles subscription to game messages and delegates processing.
 * 
 * @author Clement Luo
 * @date December 30, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class MessageBridgeManager {
    
    private final MessageBridgeProcessor messageProcessor;
    
    /**
     * Creates a new MessageBridgeManager.
     * 
     * @param jsonActionButtonsController The JSON action buttons controller
     */
    public MessageBridgeManager(JsonActionButtonsController jsonActionButtonsController) {
        // Create business service for message processing
        MessageProcessingService messageProcessingService = new MessageProcessingService();
        this.messageProcessor = new MessageBridgeProcessor(
            messageProcessingService,
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

