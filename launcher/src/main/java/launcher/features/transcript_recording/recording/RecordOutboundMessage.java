package launcher.features.transcript_recording.recording;

import launcher.features.transcript_recording.Transcript;
import launcher.features.transcript_recording.session_management.EndSession;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Records messages sent to games.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class RecordOutboundMessage {
    
    private RecordOutboundMessage() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Records a message sent to the game.
     * 
     * @param message The message map to record
     */
    public static void record(Map<String, Object> message) {
        if (!Transcript.inSession || message == null) return;
        Map<String, Object> entry = new HashMap<>();
        entry.put("direction", "out");
        entry.put("timestamp", Instant.now().toString());
        entry.put("message", new HashMap<>(message));
        Transcript.entries.add(entry);
        
        // Log transcript recording (only for non-meta messages to avoid spam)
        if (!"meta".equals(message.get("type"))) {
            System.out.println("📝 Recording message OUT to game: " + message.get("function"));
        }
        
        // Check if this is an end message
        EndSession.endFromEndMessage(message);
    }
}

