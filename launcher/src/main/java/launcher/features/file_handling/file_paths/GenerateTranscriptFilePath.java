package launcher.features.file_handling.file_paths;

import launcher.features.transcript_recording.Transcript;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

/**
 * Generates file paths for transcript files.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class GenerateTranscriptFilePath {
    
    private GenerateTranscriptFilePath() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Generates a file path for a transcript file.
     * 
     * @param extension The file extension (e.g., ".json", ".txt")
     * @return The generated file path
     */
    public static Path generate(String extension) {
        String gameName = getGameName();
        String timestamp = Instant.now().toString().replace(":", "-").replace("T", "_").replace("Z", "");
        String fileName = "saved/transcripts/transcript-" + gameName.replaceAll("[^a-zA-Z0-9]", "_") + "-" + timestamp + extension;
        return Path.of(fileName);
    }
    
    private static String getGameName() {
        for (Map<String, Object> entry : Transcript.entries) {
            if ("meta".equals(entry.get("type")) && entry.containsKey("gameName")) {
                return (String) entry.get("gameName");
            }
        }
        return "unknown";
    }
}

