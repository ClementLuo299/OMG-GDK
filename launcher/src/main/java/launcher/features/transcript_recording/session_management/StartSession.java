package launcher.features.transcript_recording.session_management;

import launcher.features.transcript_recording.Transcript;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles starting transcript sessions.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class StartSession {
    
    private StartSession() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Starts a transcript session with game metadata.
     * 
     * @param gameName The name of the game being played
     * @param gameVersion The version of the game
     */
    public static void start(String gameName, String gameVersion) {
        Transcript.inSession = true;
        Transcript.entries.clear();
        Map<String, Object> meta = new HashMap<>();
        meta.put("type", "meta");
        meta.put("event", "session_start");
        meta.put("timestamp", Instant.now().toString());
        meta.put("gameName", gameName);
        meta.put("gameVersion", gameVersion);
        Transcript.entries.add(meta);
        
        System.out.println("📝 Transcript session started for: " + gameName + " (v" + gameVersion + ")");
        System.out.println("📝 Session ID: " + System.identityHashCode(Transcript.entries));
    }
}

