package launcher.gui.lobby;

import gdk.api.GameModule;
import launcher.utils.game.TranscriptRecorder;

import java.util.Map;

/**
 * Handles game message sending operations.
 * Business logic for sending messages to game modules and handling responses.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class GameMessageHandler {
    
    /**
     * Result of sending a message to a game module.
     */
    public static class MessageResult {
        private final boolean success;
        private final Map<String, Object> response;
        private final String errorMessage;
        
        private MessageResult(boolean success, Map<String, Object> response, String errorMessage) {
            this.success = success;
            this.response = response;
            this.errorMessage = errorMessage;
        }
        
        public static MessageResult success(Map<String, Object> response) {
            return new MessageResult(true, response, null);
        }
        
        public static MessageResult noResponse() {
            return new MessageResult(true, null, null);
        }
        
        public static MessageResult error(String errorMessage) {
            return new MessageResult(false, null, errorMessage);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public Map<String, Object> getResponse() {
            return response;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public boolean hasResponse() {
            return response != null;
        }
    }
    
    /**
     * Send a message to a game module and handle the response.
     * Records messages to transcript and processes the game's response.
     * 
     * @param gameModule The game module to send the message to
     * @param messageData The message data to send
     * @return MessageResult containing the response or error information
     */
    public static MessageResult sendMessage(GameModule gameModule, Map<String, Object> messageData) {
        if (gameModule == null) {
            return MessageResult.error("No game module selected");
        }
        
        if (messageData == null) {
            return MessageResult.error("Message data is null");
        }
        
        try {
            // Record message to transcript before sending
            TranscriptRecorder.recordToGame(messageData);
            
            // Send the message to the game module
            Map<String, Object> response = gameModule.handleMessage(messageData);
            
            // Handle the response if there is one
            if (response != null) {
                // Record response to transcript
                TranscriptRecorder.recordFromGame(response);
                return MessageResult.success(response);
            } else {
                return MessageResult.noResponse();
            }
            
        } catch (Exception e) {
            return MessageResult.error("Error sending message: " + e.getMessage());
        }
    }
}

