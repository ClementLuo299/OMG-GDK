package launcher.features.transcript_recording.transcript_saving;

import launcher.features.transcript_recording.Transcript;
import launcher.features.transcript_recording.transcript_saving.save_formats.SaveTranscriptAsJson;
import launcher.features.transcript_recording.transcript_saving.save_formats.SaveTranscriptAsText;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

/**
 * Handles saving transcripts to files in JSON and text formats.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class TranscriptSaver {
    
    private TranscriptSaver() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Saves the transcript to a JSON file.
     * 
     * @param targetFile The target file path, or null for auto-generation
     * @return The path to the saved transcript file, or null if saving failed
     */
    public static Path saveTranscriptAsJson(Path targetFile) {
        return SaveTranscriptAsJson.save(targetFile);
    }
    
    /**
     * Saves the transcript in a human-readable text format.
     * 
     * @param targetFile The target file path, or null for auto-generation
     * @return The path to the saved transcript file, or null if saving failed
     */
    public static Path saveTranscriptAsText(Path targetFile) {
        return SaveTranscriptAsText.save(targetFile);
    }
    
    /**
     * Saves the transcript in both JSON and text formats.
     * 
     * @param baseFileName Base filename without extension, or null for auto-generation
     * @return Array containing paths to both saved files [JSON, Text], or null if failed
     */
    public static Path[] saveTranscriptBothFormats(String baseFileName) {
        String base = baseFileName;
        if (base == null) {
            // Get game name from transcript entries
            String gameName = "unknown";
            for (Map<String, Object> entry : Transcript.entries) {
                if ("meta".equals(entry.get("type")) && entry.containsKey("gameName")) {
                    gameName = (String) entry.get("gameName");
                    break;
                }
            }
            String timestamp = Instant.now().toString().replace(":", "-").replace("T", "_").replace("Z", "");
            base = "saved/transcripts/transcript-" + gameName.replaceAll("[^a-zA-Z0-9]", "_") + "-" + timestamp;
        }
        
        Path jsonFile = Path.of(base + ".json");
        Path textFile = Path.of(base + ".txt");
        
        Path savedJson = saveTranscriptAsJson(jsonFile);
        Path savedText = saveTranscriptAsText(textFile);
        
        if (savedJson != null && savedText != null) {
            return new Path[]{savedJson, savedText};
        }
        return null;
    }
}

