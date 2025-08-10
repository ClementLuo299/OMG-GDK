package launcher.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 
 * @author Clement Luo
 * @date August 9, 2025
 * @edited August 9, 2025
 * @since 1.0
 */
public final class TranscriptRecorder {
    private static volatile boolean inSession = false;
    private static final List<Map<String, Object>> transcriptEntries = Collections.synchronizedList(new ArrayList<>());

    private TranscriptRecorder() {}

    public static void startSession() {
        inSession = true;
        transcriptEntries.clear();
        Map<String, Object> meta = new HashMap<>();
        meta.put("type", "meta");
        meta.put("event", "session_start");
        meta.put("timestamp", Instant.now().toString());
        transcriptEntries.add(meta);
    }
    
    /**
     * Start a session with game metadata.
     * @param gameName The name of the game being played
     * @param gameVersion The version of the game
     */
    public static void startSession(String gameName, String gameVersion) {
        inSession = true;
        transcriptEntries.clear();
        Map<String, Object> meta = new HashMap<>();
        meta.put("type", "meta");
        meta.put("event", "session_start");
        meta.put("timestamp", Instant.now().toString());
        meta.put("gameName", gameName);
        meta.put("gameVersion", gameVersion);
        transcriptEntries.add(meta);
    }

    public static void endSessionIfEndDetected(Map<String, Object> message) {
        if (message == null) return;
        Object fn = message.get("function");
        if (fn != null && "end".equals(String.valueOf(fn))) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("type", "meta");
            meta.put("event", "session_end");
            meta.put("timestamp", Instant.now().toString());
            transcriptEntries.add(meta);
            
            // Automatically save the complete transcript when the game ends
            Path savedTranscript = saveTranscript(null);
            if (savedTranscript != null) {
                System.out.println("📝 Game transcript automatically saved to: " + savedTranscript.toAbsolutePath());
            } else {
                System.err.println("❌ Failed to automatically save game transcript");
            }
            
            inSession = false;
        }
    }

    public static void recordToGame(Map<String, Object> message) {
        if (!inSession || message == null) return;
        Map<String, Object> entry = new HashMap<>();
        entry.put("direction", "toGame");
        entry.put("timestamp", Instant.now().toString());
        entry.put("message", new HashMap<>(message));
        transcriptEntries.add(entry);
        
        // Log transcript recording (only for non-meta messages to avoid spam)
        if (!"meta".equals(message.get("type"))) {
            System.out.println("📝 Recording message TO game: " + message.get("function"));
        }
        
        endSessionIfEndDetected(message);
    }

    public static void recordFromGame(Map<String, Object> message) {
        if (!inSession || message == null) return;
        endSessionIfEndDetected(message);
        
        Map<String, Object> entry = new HashMap<>();
        entry.put("direction", "fromGame");
        entry.put("timestamp", Instant.now().toString());
        entry.put("message", new HashMap<>(message));
        transcriptEntries.add(entry);
        
        // Log transcript recording (only for non-meta messages to avoid spam)
        if (!"meta".equals(message.get("type"))) {
            System.out.println("📝 Recording message FROM game: " + message.get("function"));
        }
    }

    public static Path saveTranscript(Path targetFile) {
        try {
            if (targetFile == null) {
                // Try to get game name from transcript entries for better file naming
                String gameName = "unknown";
                for (Map<String, Object> entry : transcriptEntries) {
                    if ("meta".equals(entry.get("type")) && entry.containsKey("gameName")) {
                        gameName = (String) entry.get("gameName");
                        break;
                    }
                }
                
                // Create filename with game name and timestamp
                String timestamp = Instant.now().toString().replace(":", "-").replace("T", "_").replace("Z", "");
                String fileName = "saved/transcript-" + gameName.replaceAll("[^a-zA-Z0-9]", "_") + "-" + timestamp + ".jsonl";
                targetFile = Path.of(fileName);
            }
            
            ObjectMapper mapper = new ObjectMapper();
            // Ensure parent directory exists
            if (targetFile.getParent() != null) {
                Files.createDirectories(targetFile.getParent());
            }
            Files.deleteIfExists(targetFile);
            
            // Add a summary entry at the beginning
            Map<String, Object> summary = new HashMap<>();
            summary.put("type", "summary");
            summary.put("totalEntries", transcriptEntries.size());
            summary.put("savedAt", Instant.now().toString());
            summary.put("format", "JSONL - One JSON object per line");
            summary.put("description", "Complete game session transcript from start to end");
            summary.put("entries", new ArrayList<>(transcriptEntries));
            
            // Write the summary as a single JSON object
            String summaryJson = mapper.writeValueAsString(summary);
            Files.writeString(targetFile, summaryJson);
            
            return targetFile;
        } catch (IOException e) {
            return null;
        }
    }

    public static void clear() {
        transcriptEntries.clear();
        inSession = false;
    }

    public static boolean isInSession() { return inSession; }
    
    /**
     * Get the current number of transcript entries.
     * @return The number of entries in the current transcript
     */
    public static int getTranscriptEntryCount() { 
        return transcriptEntries.size(); 
    }
    
    /**
     * Manually save the current transcript to a file.
     * @return The path to the saved transcript file, or null if failed
     */
    public static Path saveCurrentTranscript() {
        if (!inSession || transcriptEntries.isEmpty()) {
            System.out.println("📝 No active transcript session to save");
            return null;
        }
        
        Path savedFile = saveTranscript(null);
        if (savedFile != null) {
            System.out.println("📝 Manually saved transcript to: " + savedFile.toAbsolutePath());
        } else {
            System.err.println("❌ Failed to manually save transcript");
        }
        return savedFile;
    }
    
    /**
     * Get a summary of the current transcript session.
     * @return A map containing session summary information
     */
    public static Map<String, Object> getSessionSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("inSession", inSession);
        summary.put("totalEntries", transcriptEntries.size());
        
        if (!transcriptEntries.isEmpty()) {
            // Find start and end timestamps
            String startTime = null;
            String endTime = null;
            
            for (Map<String, Object> entry : transcriptEntries) {
                if ("meta".equals(entry.get("type"))) {
                    if ("session_start".equals(entry.get("event"))) {
                        startTime = (String) entry.get("timestamp");
                    } else if ("session_end".equals(entry.get("event"))) {
                        endTime = (String) entry.get("timestamp");
                    }
                }
            }
            
            summary.put("sessionStart", startTime);
            summary.put("sessionEnd", endTime);
        }
        
        return summary;
    }
} 