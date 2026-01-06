package launcher.features.transcript_recording.recording;

import launcher.features.transcript_recording.Transcript;
import launcher.features.transcript_recording.session_management.EndSession;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Records messages received from games.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class RecordInboundMessage {
    
    private RecordInboundMessage() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Records a message received from the game.
     * 
     * @param message The message map to record
     */
    public static void record(Map<String, Object> message) {
        if (!Transcript.inSession || message == null) return;
        
        System.out.println("📝 recordFromGame called with: " + (message != null ? message.get("function") : "null"));
        System.out.println("📝 Current session state - inSession: " + Transcript.inSession + ", entries: " + Transcript.entries.size());
        System.out.println("📝 Full message content: " + message);
        
        // Check if this is an end message before recording
        EndSession.endFromEndMessage(message);
        
        Map<String, Object> entry = new HashMap<>();
        entry.put("direction", "in");
        entry.put("timestamp", Instant.now().toString());
        entry.put("message", new HashMap<>(message));
        Transcript.entries.add(entry);
        
        // Log transcript recording (only for non-meta messages to avoid spam)
        if (!"meta".equals(message.get("type"))) {
            System.out.println("📝 Recording message IN from game: " + message.get("function"));
        }
    }
}

