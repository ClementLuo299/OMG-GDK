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

/**
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
        
        System.out.println("📝 Basic transcript session started");
        System.out.println("📝 Session ID: " + System.identityHashCode(transcriptEntries));
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
        
        System.out.println("📝 Transcript session started for: " + gameName + " (v" + gameVersion + ")");
        System.out.println("📝 Session ID: " + System.identityHashCode(transcriptEntries));
    }

    public static void endSessionIfEndDetected(Map<String, Object> message) {
        if (message == null) return;
        Object fn = message.get("function");
        if (fn != null && "end".equals(String.valueOf(fn))) {
            System.out.println("📝 End message detected! Function: " + fn);
            System.out.println("📝 Current transcript entries: " + transcriptEntries.size());
            System.out.println("📝 In session: " + inSession);
            
            Map<String, Object> meta = new HashMap<>();
            meta.put("type", "meta");
            meta.put("event", "session_end");
            meta.put("timestamp", Instant.now().toString());
            transcriptEntries.add(meta);
            
            // Automatically save the complete transcript when the game ends
            System.out.println("📝 Attempting to save transcript...");
            Path savedTranscript = saveTranscript(null);
            if (savedTranscript != null) {
                System.out.println("📝 Game transcript automatically saved to: " + savedTranscript.toAbsolutePath());
            } else {
                System.err.println("❌ Failed to automatically save game transcript");
            }
            
            inSession = false;
            System.out.println("📝 Session ended. Total entries: " + transcriptEntries.size());
        }
    }

    public static void recordToGame(Map<String, Object> message) {
        if (!inSession || message == null) return;
        Map<String, Object> entry = new HashMap<>();
        entry.put("direction", "out");
        entry.put("timestamp", Instant.now().toString());
        entry.put("message", new HashMap<>(message));
        transcriptEntries.add(entry);
        
        // Log transcript recording (only for non-meta messages to avoid spam)
        if (!"meta".equals(message.get("type"))) {
            System.out.println("📝 Recording message OUT to game: " + message.get("function"));
        }
        
        endSessionIfEndDetected(message);
    }

    public static void recordFromGame(Map<String, Object> message) {
        if (!inSession || message == null) return;
        
        System.out.println("📝 recordFromGame called with: " + (message != null ? message.get("function") : "null"));
        System.out.println("📝 Current session state - inSession: " + inSession + ", entries: " + transcriptEntries.size());
        System.out.println("📝 Full message content: " + message);
        
        endSessionIfEndDetected(message);
        
        Map<String, Object> entry = new HashMap<>();
        entry.put("direction", "in");
        entry.put("timestamp", Instant.now().toString());
        entry.put("message", new HashMap<>(message));
        transcriptEntries.add(entry);
        
        // Log transcript recording (only for non-meta messages to avoid spam)
        if (!"meta".equals(message.get("type"))) {
            System.out.println("📝 Recording message IN from game: " + message.get("function"));
        }
    }

    public static Path saveTranscript(Path targetFile) {
        try {
            System.out.println("📝 saveTranscript called with targetFile: " + targetFile);
            System.out.println("📝 Current transcript entries: " + transcriptEntries.size());
            
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
            String fileName = "saved/transcript-" + gameName.replaceAll("[^a-zA-Z0-9]", "_") + "-" + timestamp + ".json";
            targetFile = Path.of(fileName);
            System.out.println("📝 Generated filename: " + fileName);
            }
            
            System.out.println("📝 Final target file: " + targetFile.toAbsolutePath());
            
            ObjectMapper mapper = new ObjectMapper();
            // Ensure parent directory exists
            if (targetFile.getParent() != null) {
                Files.createDirectories(targetFile.getParent());
                System.out.println("📝 Created/verified parent directory: " + targetFile.getParent());
            }
            Files.deleteIfExists(targetFile);
            
            // Create a well-formatted transcript structure
            Map<String, Object> transcript = new HashMap<>();
            
            // Header section
            Map<String, Object> header = new HashMap<>();
            header.put("generatedAt", Instant.now().toString());
            transcript.put("header", header);
            

            // Messages section - organized chronologically
            List<Map<String, Object>> messages = new ArrayList<>();
            for (Map<String, Object> entry : transcriptEntries) {
                if (!"meta".equals(entry.get("type"))) {
                    Map<String, Object> messageEntry = new HashMap<>();
                    messageEntry.put("timestamp", entry.get("timestamp"));
                    messageEntry.put("direction", entry.get("direction"));
                    
                    // Extract function and other details from the message
                    if (entry.get("message") instanceof Map) {
                        Map<String, Object> msg = (Map<String, Object>) entry.get("message");
                        messageEntry.put("function", msg.get("function"));
                        messageEntry.put("details", new HashMap<>(msg));
                    }
                    
                    messages.add(messageEntry);
                }
            }
            transcript.put("messages", messages);
            
            // Write the formatted transcript with pretty printing
            String transcriptJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(transcript);
            Files.writeString(targetFile, transcriptJson);
            
            System.out.println("📝 Successfully wrote transcript to file. File size: " + Files.size(targetFile) + " bytes");
            
            return targetFile;
        } catch (IOException e) {
            System.err.println("❌ Error in saveTranscript: " + e.getMessage());
            e.printStackTrace();
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
     * Print current transcript status for debugging.
     */
    public static void printStatus() {
        System.out.println("📝 === TRANSCRIPT STATUS ===");
        System.out.println("📝 In session: " + inSession);
        System.out.println("📝 Total entries: " + transcriptEntries.size());
        System.out.println("📝 Session ID: " + System.identityHashCode(transcriptEntries));
        
        if (!transcriptEntries.isEmpty()) {
            System.out.println("📝 First entry: " + transcriptEntries.get(0));
            System.out.println("📝 Last entry: " + transcriptEntries.get(transcriptEntries.size() - 1));
        }
        System.out.println("📝 =========================");
    }
    
    /**
     * Test method to manually trigger transcript saving for debugging.
     */
    public static void testSave() {
        System.out.println("📝 === TESTING TRANSCRIPT SAVE ===");
        printStatus();
        
        if (inSession && !transcriptEntries.isEmpty()) {
            System.out.println("📝 Attempting test save...");
            Path savedFile = saveTranscript(null);
            if (savedFile != null) {
                System.out.println("📝 Test save successful: " + savedFile.toAbsolutePath());
            } else {
                System.err.println("❌ Test save failed");
            }
        } else {
            System.out.println("📝 No active session or no entries to save");
        }
        System.out.println("📝 ================================");
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
    
    /**
     * Calculate the duration between two timestamps.
     * @param startTime Start timestamp string
     * @param endTime End timestamp string
     * @return Duration string in human-readable format
     */
    private static String calculateSessionDuration(Object startTime, Object endTime) {
        if (startTime == null || endTime == null) {
            return "unknown";
        }
        
        try {
            Instant start = Instant.parse(startTime.toString());
            Instant end = Instant.parse(endTime.toString());
            long durationSeconds = end.getEpochSecond() - start.getEpochSecond();
            
            if (durationSeconds < 60) {
                return durationSeconds + " seconds";
            } else if (durationSeconds < 3600) {
                long minutes = durationSeconds / 60;
                long seconds = durationSeconds % 60;
                return minutes + "m " + seconds + "s";
            } else {
                long hours = durationSeconds / 3600;
                long minutes = (durationSeconds % 3600) / 60;
                long seconds = durationSeconds % 60;
                return hours + "h " + minutes + "m " + seconds + "s";
            }
        } catch (Exception e) {
            return "error calculating duration";
        }
    }
    
    /**
     * Generate a summary of message flow patterns.
     * @param messages List of message entries
     * @return Map containing flow analysis
     */
    private static Map<String, Object> generateMessageFlowSummary(List<Map<String, Object>> messages) {
        Map<String, Object> flowSummary = new HashMap<>();
        
        if (messages.isEmpty()) {
            flowSummary.put("pattern", "No messages recorded");
            return flowSummary;
        }
        
        // Count message directions
        int toGameCount = 0;
        int fromGameCount = 0;
        Map<String, Integer> functionCounts = new HashMap<>();
        
        for (Map<String, Object> message : messages) {
            String direction = (String) message.get("direction");
            if ("toGame".equals(direction)) {
                toGameCount++;
            } else if ("fromGame".equals(direction)) {
                fromGameCount++;
            }
            
            String function = (String) message.get("function");
            if (function != null) {
                functionCounts.put(function, functionCounts.getOrDefault(function, 0) + 1);
            }
        }
        
        flowSummary.put("toGameMessages", toGameCount);
        flowSummary.put("fromGameMessages", fromGameCount);
        flowSummary.put("functionBreakdown", functionCounts);
        flowSummary.put("totalExchanges", Math.min(toGameCount, fromGameCount));
        
        // Determine flow pattern
        if (toGameCount == 0 && fromGameCount == 0) {
            flowSummary.put("pattern", "No communication");
        } else if (toGameCount == 0) {
            flowSummary.put("pattern", "Game-only communication");
        } else if (fromGameCount == 0) {
            flowSummary.put("pattern", "Launcher-only communication");
        } else {
            flowSummary.put("pattern", "Two-way communication");
        }
        
        return flowSummary;
    }
} 