package launcher.features.transcript_recording.session_management;

import launcher.features.json_processing.MessageFunctionCheck;
import launcher.features.transcript_recording.Transcript;
import launcher.features.transcript_recording.transcript_saving.TranscriptSaver;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles ending transcript sessions.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class EndSession {
    
    private EndSession() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Ends the current session.
     */
    public static void end() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("type", "meta");
        meta.put("event", "session_end");
        meta.put("timestamp", Instant.now().toString());
        Transcript.entries.add(meta);
        
        Transcript.inSession = false;
        System.out.println("📝 Session ended. Total entries: " + Transcript.entries.size());
    }
    
    /**
     * Checks if a message is an "end" message and handles it.
     * 
     * @param message The message to check
     */
    public static void endFromEndMessage(Map<String, Object> message) {
        if (!MessageFunctionCheck.isEndMessage(message)) return;
        
        System.out.println("📝 End message detected! Function: end");
        System.out.println("📝 Current transcript entries: " + Transcript.entries.size());
        System.out.println("📝 In session: " + Transcript.inSession);
        
        // First, record the actual end message to preserve its function and details
        Map<String, Object> endMessageEntry = new HashMap<>();
        endMessageEntry.put("direction", "in"); // End messages typically come from the game
        endMessageEntry.put("timestamp", Instant.now().toString());
        endMessageEntry.put("message", new HashMap<>(message));
        Transcript.entries.add(endMessageEntry);
        
        // End the session
        end();
        
        // Automatically save the complete transcript in both formats when the game ends
        System.out.println("📝 Attempting to save transcript in both formats...");
        Path[] savedTranscripts = TranscriptSaver.saveTranscriptBothFormats(null);
        if (savedTranscripts != null) {
            System.out.println("📝 Game transcript automatically saved to:");
            System.out.println("📝 JSON: " + savedTranscripts[0].toAbsolutePath());
            System.out.println("📝 Text: " + savedTranscripts[1].toAbsolutePath());
        } else {
            System.err.println("❌ Failed to automatically save game transcript");
        }
    }
}

