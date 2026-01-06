package launcher.features.game_messaging.sending;

import gdk.api.GameModule;
import launcher.features.transcript_recording.TranscriptRecorder;

import java.util.Map;

/**
 * Handles sending messages to game modules during gameplay.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public class SendMessageToGame {
    
    /**
     * Sends a message to a game module.
     * 
     * @param gameModule The game module to send the message to
     * @param messageData The message data to send
     * @return The response from the game module, or null if no response
     * @throws IllegalStateException If validation fails
     * @throws Exception If sending the message fails
     */
    public static Map<String, Object> sendMessage(GameModule gameModule, Map<String, Object> messageData) throws Exception {
        // Validate parameters
        if (gameModule == null) {
            throw new IllegalStateException("No game module selected");
        }
        if (messageData == null) {
            throw new IllegalStateException("Message data is null");
        }
        
        // Record message to transcript before sending
        TranscriptRecorder.recordToGame(messageData);
        
        // Send the message to the game module
        Map<String, Object> response = gameModule.handleMessage(messageData);
        
        // Record response if present
        if (response != null) {
            TranscriptRecorder.recordFromGame(response);
        }
        
        return response;
    }
}
