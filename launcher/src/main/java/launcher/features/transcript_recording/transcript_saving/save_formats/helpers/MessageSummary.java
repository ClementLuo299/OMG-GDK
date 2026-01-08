package launcher.features.transcript_recording.transcript_saving.save_formats.helpers;

import java.util.List;
import java.util.Map;

/**
 * Generates human-readable summaries for transcript messages.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class MessageSummary {
    
    private MessageSummary() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Generates a human-readable summary for a message based on its function and details.
     * 
     * @param function The message function
     * @param details The message details map
     * @return A summary string, or null if no summary is available
     */
    public static String generate(String function, Map<String, Object> details) {
        if (function == null) return null;
        
        switch (function) {
            case "ui_initialization":
                String gameMode = (String) details.get("gameMode");
                Object players = details.get("players");
                String localPlayer = (String) details.get("localPlayerId");
                return String.format("Game started: %s mode with %s players (you are: %s)", 
                    gameMode != null ? gameMode : "unknown", 
                    players instanceof List ? ((List<?>) players).size() : "unknown",
                    localPlayer != null ? localPlayer : "unknown");
                    
            case "end":
                String reason = (String) details.get("reason");
                return String.format("Game ended: %s", reason != null ? reason : "unknown reason");
                
            case "ack":
                String status = (String) details.get("status");
                String of = (String) details.get("of");
                return String.format("Acknowledgment: %s for %s", 
                    status != null ? status : "unknown", 
                    of != null ? of : "unknown function");
                    
            case "message":
                String from = (String) details.get("from");
                String text = (String) details.get("text");
                if (text != null && text.length() > 50) {
                    text = text.substring(0, 47) + "...";
                }
                return String.format("Message from %s: %s", 
                    from != null ? from : "unknown", 
                    text != null ? text : "no content");
                    
            case "chat":
                String chatFrom = (String) details.get("from");
                String chatText = (String) details.get("text");
                if (chatText != null && chatText.length() > 50) {
                    chatText = chatText.substring(0, 47) + "...";
                }
                return String.format("Chat from %s: %s", 
                    chatFrom != null ? chatFrom : "unknown", 
                    chatText != null ? chatText : "no content");
                    
            case "extract_metadata":
                return "Metadata request";
                
            default:
                return null;
        }
    }
}

