package launcher.gui.lobby;

import launcher.utils.game.TranscriptRecorder;

import java.util.Map;

/**
 * Manages transcript recording operations.
 * Business logic wrapper for transcript recording functionality.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class TranscriptManager {
    
    /**
     * Record a message sent to the game.
     * 
     * @param messageData The message data being sent to the game
     */
    public static void recordToGame(Map<String, Object> messageData) {
        TranscriptRecorder.recordToGame(messageData);
    }
    
    /**
     * Record a message received from the game.
     * 
     * @param messageData The message data received from the game
     */
    public static void recordFromGame(Map<String, Object> messageData) {
        TranscriptRecorder.recordFromGame(messageData);
    }
}

