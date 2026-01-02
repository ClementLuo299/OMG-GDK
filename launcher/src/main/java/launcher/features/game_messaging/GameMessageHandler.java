package launcher.features.game_messaging;

import gdk.api.GameModule;

import java.util.Map;

/**
 * Handles game message sending operations.
 * 
 * <p>This class has a single responsibility: sending messages to game modules
 * and handling their responses, including transcript recording.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Validating message parameters</li>
 *   <li>Sending messages to game modules</li>
 *   <li>Recording messages to transcripts</li>
 *   <li>Handling responses and errors</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class GameMessageHandler {
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Sends a message to a game module and handles the response.
     * 
     * <p>This method:
     * <ul>
     *   <li>Validates that a game module is selected and message data is provided</li>
     *   <li>Records the message to the transcript before sending</li>
     *   <li>Sends the message to the game module</li>
     *   <li>Records the response to the transcript if one is returned</li>
     *   <li>Returns a MessageResult with the response or error information</li>
     * </ul>
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
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Result of sending a message to a game module.
     * 
     * <p>This class encapsulates the result of a message send operation,
     * including success status, response data, and error messages.
     */
    public static class MessageResult {
        
        // ==================== FIELDS ====================
        
        /** Whether the message was sent successfully. */
        private final boolean success;
        
        /** The response from the game module, if any. */
        private final Map<String, Object> response;
        
        /** Error message if the operation failed. */
        private final String errorMessage;
        
        // ==================== CONSTRUCTOR ====================
        
        /**
         * Creates a new MessageResult.
         * 
         * @param success Whether the message was sent successfully
         * @param response The response from the game module, or null
         * @param errorMessage Error message if operation failed, or null
         */
        private MessageResult(boolean success, Map<String, Object> response, String errorMessage) {
            this.success = success;
            this.response = response;
            this.errorMessage = errorMessage;
        }
        
        // ==================== FACTORY METHODS ====================
        
        /**
         * Creates a successful MessageResult with a response.
         * 
         * @param response The response from the game module
         * @return A successful MessageResult with the response
         */
        public static MessageResult success(Map<String, Object> response) {
            return new MessageResult(true, response, null);
        }
        
        /**
         * Creates a successful MessageResult with no response.
         * 
         * <p>This is used when the game module successfully processed the message
         * but did not return a response.
         * 
         * @return A successful MessageResult with no response
         */
        public static MessageResult noResponse() {
            return new MessageResult(true, null, null);
        }
        
        /**
         * Creates an error MessageResult.
         * 
         * @param errorMessage The error message describing what went wrong
         * @return An error MessageResult with the error message
         */
        public static MessageResult error(String errorMessage) {
            return new MessageResult(false, null, errorMessage);
        }
        
        // ==================== PUBLIC METHODS ====================
        
        /**
         * Checks if the message was sent successfully.
         * 
         * @return true if successful, false otherwise
         */
        public boolean isSuccess() {
            return success;
        }
        
        /**
         * Gets the response from the game module.
         * 
         * @return The response map, or null if no response was received
         */
        public Map<String, Object> getResponse() {
            return response;
        }
        
        /**
         * Gets the error message if the operation failed.
         * 
         * @return The error message, or null if the operation was successful
         */
        public String getErrorMessage() {
            return errorMessage;
        }
        
        /**
         * Checks if a response was received from the game module.
         * 
         * @return true if a response was received, false otherwise
         */
        public boolean hasResponse() {
            return response != null;
        }
    }
}

