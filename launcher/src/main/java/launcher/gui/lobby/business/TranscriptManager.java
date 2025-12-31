package launcher.gui.lobby.business;

import launcher.utils.game.TranscriptRecorder;

import java.util.Map;

/**
 * Manages transcript recording operations.
 * 
 * <p>This class has a single responsibility: providing a business logic interface
 * for recording messages to and from games in transcripts.
 * 
 * <p>This is a thin wrapper around TranscriptRecorder that provides a business
 * logic layer interface for the lobby package.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class TranscriptManager {
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Records a message sent to the game in the transcript.
     * 
     * @param messageData The message data being sent to the game
     */
    public static void recordToGame(Map<String, Object> messageData) {
        TranscriptRecorder.recordToGame(messageData);
    }
    
    /**
     * Records a message received from the game in the transcript.
     * 
     * @param messageData The message data received from the game
     */
    public static void recordFromGame(Map<String, Object> messageData) {
        TranscriptRecorder.recordFromGame(messageData);
    }
}

