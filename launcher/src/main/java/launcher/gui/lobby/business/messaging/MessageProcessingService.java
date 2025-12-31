package launcher.gui.lobby.business.messaging;

import launcher.gui.lobby.business.JsonFormatter;
import launcher.gui.lobby.business.TranscriptManager;

import java.util.Map;

/**
 * Business logic service for processing messages from games.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>Recording messages to transcripts</li>
 *   <li>Formatting JSON responses</li>
 *   <li>Determining message types</li>
 * </ul>
 * 
 * <p>This service does NOT handle UI updates.
 * Those responsibilities belong to UI logic classes.
 * 
 * @author Clement Luo
 * @date December 30, 2025
 * @since Beta 1.0
 */
public class MessageProcessingService {
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Records a message from the game to the transcript.
     * 
     * @param msg The message map from the game
     */
    public void recordMessageToTranscript(Map<String, Object> msg) {
        TranscriptManager.recordFromGame(msg);
    }
    
    /**
     * Formats a JSON response for display.
     * 
     * @param response The response map to format
     * @return Formatted JSON string
     */
    public String formatJsonResponse(Map<String, Object> response) {
        return JsonFormatter.formatJsonResponse(response);
    }
    
    /**
     * Checks if a message is an "end" message.
     * 
     * <p>An "end" message indicates that the game session should be terminated.
     * This is determined by checking if the "function" field equals "end".
     * 
     * @param msg The message map to check
     * @return true if the message is an "end" message, false otherwise
     */
    public boolean isEndMessage(Map<String, Object> msg) {
        if (msg == null) {
            return false;
        }
        Object fn = msg.get("function");
        return fn != null && "end".equals(String.valueOf(fn));
    }
}

