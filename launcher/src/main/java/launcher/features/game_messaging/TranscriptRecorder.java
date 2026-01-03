package launcher.features.game_messaging;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for recording game session transcripts.
 * 
 * <p>This class has a single responsibility: recording and managing transcripts
 * of game sessions, including messages sent to and received from games.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Starting and ending transcript sessions</li>
 *   <li>Recording messages to and from games</li>
 *   <li>Detecting end messages and automatically saving transcripts</li>
 *   <li>Saving transcripts in both JSON and text formats</li>
 *   <li>Providing session status and summary information</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date August 9, 2025
 * @edited August 10, 2025 
 * @since 1.0
 */
public final class TranscriptRecorder {
    
    // ==================== STATE ====================
    
    /** Flag indicating whether a transcript session is currently active. */
    private static volatile boolean inSession = false;
    
    /** Thread-safe list of transcript entries. */
    private static final List<Map<String, Object>> transcriptEntries = Collections.synchronizedList(new ArrayList<>());
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private TranscriptRecorder() {
        throw new AssertionError("TranscriptRecorder should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - SESSION MANAGEMENT ====================
    
    /**
     * Starts a basic transcript session without game metadata.
     * 
     * <p>This method initializes a new transcript session and clears any
     * previous entries. A session init meta entry is added to the transcript.
     */
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
     * Starts a transcript session with game metadata.
     * 
     * <p>This method initializes a new transcript session with game information
     * and clears any previous entries. A session init meta entry is added
     * to the transcript with the game name and version.
     * 
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

    // ==================== PUBLIC METHODS - MESSAGE RECORDING ====================
    
    /**
     * Ends the session if an "end" message is detected.
     * 
     * <p>This method checks if the provided message is an "end" message.
     * If so, it records the end message, adds a session end meta entry,
     * and automatically saves the transcript in both formats.
     * 
     * @param message The message to check for end detection
     */
    public static void endSessionIfEndDetected(Map<String, Object> message) {
        if (message == null) return;
        Object fn = message.get("function");
        if (fn != null && "end".equals(String.valueOf(fn))) {
            System.out.println("📝 End message detected! Function: " + fn);
            System.out.println("📝 Current transcript entries: " + transcriptEntries.size());
            System.out.println("📝 In session: " + inSession);
            
            // First, record the actual end message to preserve its function and details
            // This ensures the end message is included in the transcript with proper function
            Map<String, Object> endMessageEntry = new HashMap<>();
            endMessageEntry.put("direction", "in"); // End messages typically come from the game
            endMessageEntry.put("timestamp", Instant.now().toString());
            endMessageEntry.put("message", new HashMap<>(message));
            transcriptEntries.add(endMessageEntry);
            
            // Then add the meta entry for session end
            Map<String, Object> meta = new HashMap<>();
            meta.put("type", "meta");
            meta.put("event", "session_end");
            meta.put("timestamp", Instant.now().toString());
            transcriptEntries.add(meta);
            
            // Automatically save the complete transcript in both formats when the game ends
            System.out.println("📝 Attempting to save transcript in both formats...");
            Path[] savedTranscripts = saveTranscriptBothFormats(null);
            if (savedTranscripts != null) {
                System.out.println("📝 Game transcript automatically saved to:");
                System.out.println("📝 JSON: " + savedTranscripts[0].toAbsolutePath());
                System.out.println("📝 Text: " + savedTranscripts[1].toAbsolutePath());
            } else {
                System.err.println("❌ Failed to automatically save game transcript");
            }
            
            inSession = false;
            System.out.println("📝 Session ended. Total entries: " + transcriptEntries.size());
        }
    }

    /**
     * Records a message sent to the game.
     * 
     * <p>This method records a message with direction "out" (to game).
     * The message is only recorded if a session is active and the message is not null.
     * After recording, it checks if the message is an end message.
     * 
     * @param message The message map to record
     */
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

    /**
     * Records a message received from the game.
     * 
     * <p>This method records a message with direction "in" (from game).
     * The message is only recorded if a session is active and the message is not null.
     * It first checks if the message is an end message before recording.
     * 
     * @param message The message map to record
     */
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

    // ==================== PUBLIC METHODS - TRANSCRIPT SAVING ====================
    
    /**
     * Saves the transcript to a JSON file.
     * 
     * <p>This method saves the transcript in JSON format with a structured format
     * including header and messages. If no target file is provided, it generates
     * a filename based on the game name and timestamp.
     * 
     * @param targetFile The target file file_paths, or null for auto-generation
     * @return The file_paths to the saved transcript file, or null if saving failed
     */
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
                
                // Create filename with game name and timestamp in transcripts subdirectory
                String timestamp = Instant.now().toString().replace(":", "-").replace("T", "_").replace("Z", "");
                String fileName = "saved/transcripts/transcript-" + gameName.replaceAll("[^a-zA-Z0-9]", "_") + "-" + timestamp + ".json_processing";
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
            
            // Create a simple transcript structure with just header and messages
            Map<String, Object> transcript = new HashMap<>();
            
            // Simple header section
            Map<String, Object> header = new HashMap<>();
            header.put("generatedAt", formatTimestamp(Instant.now()));
            header.put("title", "Game Session Transcript");
            transcript.put("header", header);
            
            // Simple messages list - just the essential information
            List<Map<String, Object>> messages = new ArrayList<>();
            for (Map<String, Object> entry : transcriptEntries) {
                if (!"meta".equals(entry.get("type"))) {
                    Map<String, Object> messageEntry = new HashMap<>();
                    
                    // Basic message information
                    String timestamp = (String) entry.get("timestamp");
                    messageEntry.put("timestamp", formatTimestamp(Instant.parse(timestamp)));
                    messageEntry.put("direction", entry.get("direction"));
                    
                    // Extract function and details from the message
                    if (entry.get("message") instanceof Map) {
                        Map<String, Object> msg = (Map<String, Object>) entry.get("message");
                        String function = (String) msg.get("function");
                        messageEntry.put("function", function);
                        
                        // Copy all fields from the original message
                        for (Map.Entry<String, Object> field : msg.entrySet()) {
                            messageEntry.put(field.getKey(), field.getValue());
                        }
                    }
                    
                    messages.add(messageEntry);
                }
            }
            
            // Add game init message if available
            for (Map<String, Object> entry : transcriptEntries) {
                if ("meta".equals(entry.get("type")) && "session_start".equals(entry.get("event"))) {
                    Map<String, Object> startMessage = new HashMap<>();
                    String timestamp = (String) entry.get("timestamp");
                                            startMessage.put("timestamp", formatTimestamp(Instant.parse(timestamp)));
                        startMessage.put("direction", "in");
                        startMessage.put("function", "init");
                    startMessage.put("gameName", entry.get("gameName"));
                    startMessage.put("gameVersion", entry.get("gameVersion"));
                    startMessage.put("event", "session_start");
                    messages.add(0, startMessage); // Add at the beginning
                    break;
                }
            }
            
            transcript.put("messages", messages);
            
            // Write the simple transcript with proper pretty printing and extra spacing
            String transcriptJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(transcript);
            
            // Add new lines after each message for better readability
            transcriptJson = transcriptJson.replaceAll("\\},\\s*\\{", "},\n\n    {");
            
            Files.writeString(targetFile, transcriptJson);
            
            System.out.println("📝 Successfully wrote transcript to file. File size: " + Files.size(targetFile) + " bytes");
            
            return targetFile;
        } catch (IOException e) {
            System.err.println("❌ Error in saveTranscript: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Saves the transcript in a human-readable text format.
     * 
     * <p>This method saves the transcript in a plain text format with clear
     * formatting, headers, and message details. If no target file is provided,
     * it generates a filename based on the game name and timestamp.
     * 
     * @param targetFile The target file file_paths, or null for auto-generation
     * @return The file_paths to the saved transcript file, or null if saving failed
     */
    public static Path saveTranscriptAsText(Path targetFile) {
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
                
                // Create filename with game name and timestamp in transcripts subdirectory
                String timestamp = Instant.now().toString().replace(":", "-").replace("T", "_").replace("Z", "");
                String fileName = "saved/transcripts/transcript-" + gameName.replaceAll("[^a-zA-Z0-9]", "_") + "-" + timestamp + ".txt";
                targetFile = Path.of(fileName);
            }
            
            // Ensure parent directory exists
            if (targetFile.getParent() != null) {
                Files.createDirectories(targetFile.getParent());
            }
            Files.deleteIfExists(targetFile);
            
            StringBuilder textTranscript = new StringBuilder();
            
            // Header
            textTranscript.append("=".repeat(80)).append("\n");
            textTranscript.append("GAME SESSION TRANSCRIPT\n");
            textTranscript.append("=".repeat(80)).append("\n\n");
            
            // Session info
            String gameName = "unknown";
            String gameVersion = "unknown";
            String sessionStart = null;
            String sessionEnd = null;
            
            for (Map<String, Object> entry : transcriptEntries) {
                if ("meta".equals(entry.get("type"))) {
                    if ("session_start".equals(entry.get("event"))) {
                        sessionStart = (String) entry.get("timestamp");
                        if (entry.containsKey("gameName")) {
                            gameName = (String) entry.get("gameName");
                        }
                        if (entry.containsKey("gameVersion")) {
                            gameVersion = (String) entry.get("gameVersion");
                        }
                    } else if ("session_end".equals(entry.get("event"))) {
                        sessionEnd = (String) entry.get("timestamp");
                    }
                }
            }
            
            textTranscript.append("GAME: ").append(gameName).append(" (v").append(gameVersion).append(")\n");
            textTranscript.append("SESSION START: ").append(sessionStart != null ? formatTimestamp(Instant.parse(sessionStart)) : "unknown").append("\n");
            textTranscript.append("SESSION END: ").append(sessionEnd != null ? formatTimestamp(Instant.parse(sessionEnd)) : "unknown").append("\n");
            textTranscript.append("\n");
            
            // Messages
            textTranscript.append("MESSAGE FLOW:\n");
            textTranscript.append("-".repeat(80)).append("\n\n");
            
            // Add game init message first if available
            for (Map<String, Object> entry : transcriptEntries) {
                if ("meta".equals(entry.get("type")) && "session_start".equals(entry.get("event"))) {
                                            textTranscript.append("Message #1\n");
                        textTranscript.append("Time: ").append(formatTimestamp(Instant.parse((String) entry.get("timestamp")))).append("\n");
                        textTranscript.append("Direction: IN\n");
                        textTranscript.append("Function: init\n");
                    textTranscript.append("Summary: Game session started\n");
                    textTranscript.append("Details:\n");
                    textTranscript.append("  gameName: ").append(entry.get("gameName")).append("\n");
                    textTranscript.append("  gameVersion: ").append(entry.get("gameVersion")).append("\n");
                    textTranscript.append("  event: session_start\n");
                    textTranscript.append("\n");
                    break;
                }
            }
            
            int messageNumber = 2; // Start from 2 since we already added the init message
            for (Map<String, Object> entry : transcriptEntries) {
                if (!"meta".equals(entry.get("type"))) {
                    String direction = (String) entry.get("direction");
                    String timestamp = (String) entry.get("timestamp");
                    
                    textTranscript.append("Message #").append(messageNumber++).append("\n");
                    textTranscript.append("Time: ").append(formatTimestamp(Instant.parse(timestamp))).append("\n");
                    textTranscript.append("Direction: ").append(direction.toUpperCase()).append("\n");
                    
                    if (entry.get("message") instanceof Map) {
                        Map<String, Object> msg = (Map<String, Object>) entry.get("message");
                        String function = (String) msg.get("function");
                        
                        textTranscript.append("Function: ").append(function != null ? function : "none").append("\n");
                        
                        // Add summary if available
                        String summary = generateMessageSummary(function, msg);
                        if (summary != null) {
                            textTranscript.append("Summary: ").append(summary).append("\n");
                        }
                        
                        // Add details
                        textTranscript.append("Details:\n");
                        for (Map.Entry<String, Object> field : msg.entrySet()) {
                            if (!"function".equals(field.getKey())) {
                                textTranscript.append("  ").append(field.getKey()).append(": ").append(field.getValue()).append("\n");
                            }
                        }
                    }
                    
                    textTranscript.append("\n");
                }
            }
            
            // Add end message if available (it should be the last message before meta entries)
            for (Map<String, Object> entry : transcriptEntries) {
                if ("meta".equals(entry.get("type")) && "session_end".equals(entry.get("event"))) {
                    // Look for the end message that was recorded just before this meta entry
                    if (messageNumber > 2) { // Only add if we have other messages
                        textTranscript.append("Message #").append(messageNumber++).append("\n");
                        textTranscript.append("Time: ").append(formatTimestamp(Instant.now())).append("\n");
                        textTranscript.append("Direction: IN\n");
                        textTranscript.append("Function: end\n");
                        textTranscript.append("Summary: Game session ended\n");
                        textTranscript.append("Details:\n");
                        textTranscript.append("  event: session_end\n");
                        textTranscript.append("\n");
                    }
                    break;
                }
            }
            
            // Footer
            textTranscript.append("-".repeat(80)).append("\n");
            textTranscript.append("END OF TRANSCRIPT\n");
            textTranscript.append("Generated at: ").append(formatTimestamp(Instant.now())).append("\n");
            textTranscript.append("=".repeat(80)).append("\n");
            
            Files.writeString(targetFile, textTranscript.toString());
            
            System.out.println("📝 Successfully wrote text transcript to file: " + targetFile.toAbsolutePath());
            
            return targetFile;
        } catch (IOException e) {
            System.err.println("❌ Error in saveTranscriptAsText: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Saves the transcript in both JSON and text formats.
     * 
     * <p>This method saves the transcript in both JSON and text formats for
     * maximum readability and programmatic access. If no base filename is provided,
     * it generates one based on the game name and timestamp.
     * 
     * @param baseFileName Base filename without extension, or null for auto-generation
     * @return Array containing paths to both saved files [JSON, Text], or null if failed
     */
    public static Path[] saveTranscriptBothFormats(String baseFileName) {
        try {
            if (baseFileName == null) {
                // Try to get game name from transcript entries for better file naming
                String gameName = "unknown";
                for (Map<String, Object> entry : transcriptEntries) {
                    if ("meta".equals(entry.get("type")) && entry.containsKey("gameName")) {
                        gameName = (String) entry.get("gameName");
                        break;
                    }
                }
                
                // Create base filename with game name and timestamp in transcripts subdirectory
                String timestamp = Instant.now().toString().replace(":", "-").replace("T", "_").replace("Z", "");
                baseFileName = "saved/transcripts/transcript-" + gameName.replaceAll("[^a-zA-Z0-9]", "_") + "-" + timestamp;
            }
            
            Path jsonFile = Path.of(baseFileName + ".json_processing");
            Path textFile = Path.of(baseFileName + ".txt");
            
            // Save both formats
            Path savedJson = saveTranscript(jsonFile);
            Path savedText = saveTranscriptAsText(textFile);
            
            if (savedJson != null && savedText != null) {
                System.out.println("📝 Successfully saved transcript in both formats:");
                System.out.println("📝 JSON: " + savedJson.toAbsolutePath());
                System.out.println("📝 Text: " + savedText.toAbsolutePath());
                return new Path[]{savedJson, savedText};
            } else {
                System.err.println("❌ Failed to save one or both transcript formats");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in saveTranscriptBothFormats: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Manually saves the current transcript to files in both formats.
     * 
     * <p>This method saves the current transcript if there is an active session
     * and entries exist. This is useful for manual saving before session end.
     * 
     * @return Array containing paths to both saved files [JSON, Text], or null if failed
     */
    public static Path[] saveCurrentTranscript() {
        if (!inSession || transcriptEntries.isEmpty()) {
            System.out.println("📝 No active transcript session to save");
            return null;
        }
        
        Path[] savedFiles = saveTranscriptBothFormats(null);
        if (savedFiles != null) {
            System.out.println("📝 Manually saved transcript in both formats");
        } else {
            System.err.println("❌ Failed to manually save transcript in both formats");
        }
        return savedFiles;
    }

    // ==================== PUBLIC METHODS - SESSION QUERY ====================
    
    /**
     * Clears the transcript and ends the session.
     * 
     * <p>This method clears all transcript entries and sets the session flag to false.
     */
    public static void clear() {
        transcriptEntries.clear();
        inSession = false;
    }

    /**
     * Checks if a transcript session is currently active.
     * 
     * @return true if a session is active, false otherwise
     */
    public static boolean isInSession() { 
        return inSession; 
    }
    
    /**
     * Gets the current number of transcript entries.
     * 
     * @return The number of entries in the current transcript
     */
    public static int getTranscriptEntryCount() { 
        return transcriptEntries.size(); 
    }
    
    /**
     * Prints current transcript status for debugging.
     * 
     * <p>This method prints detailed information about the current transcript
     * session, including session state, entry count, and first/last entries.
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
     * 
     * <p>This method is used for testing and debugging transcript saving functionality.
     * It prints status information and attempts to save the transcript.
     */
    public static void testSave() {
        System.out.println("📝 === TESTING TRANSCRIPT SAVE ===");
        printStatus();
        
        if (inSession && !transcriptEntries.isEmpty()) {
            System.out.println("📝 Attempting test save in both formats...");
            Path[] savedFiles = saveTranscriptBothFormats(null);
            if (savedFiles != null) {
                System.out.println("📝 Test save successful:");
                System.out.println("📝 JSON: " + savedFiles[0].toAbsolutePath());
                System.out.println("📝 Text: " + savedFiles[1].toAbsolutePath());
            } else {
                System.err.println("❌ Test save failed");
            }
        } else {
            System.out.println("📝 No active session or no entries to save");
        }
        System.out.println("📝 ================================");
    }
    
    /**
     * Gets a summary of the current transcript session.
     * 
     * <p>This method returns a map containing session summary information including
     * session state, total entries, and init/end timestamps.
     * 
     * @return A map containing session summary information
     */
    public static Map<String, Object> getSessionSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("inSession", inSession);
        summary.put("totalEntries", transcriptEntries.size());
        
        if (!transcriptEntries.isEmpty()) {
            // Find init and end timestamps
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
    
    // ==================== PRIVATE METHODS - UTILITY ====================
    
    /**
     * Calculates the duration between two timestamps.
     * 
     * <p>This method calculates and formats the duration between two timestamps
     * in a human-readable format (seconds, minutes, or hours).
     * 
     * @param startTime Start timestamp string
     * @param endTime End timestamp string
     * @return Duration string in human-readable format, or "unknown" if calculation fails
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
     * Generates a summary of message flow patterns.
     * 
     * <p>This method analyzes message flow patterns including direction counts,
     * function breakdown, and communication patterns.
     * 
     * @param messages List of message entries to analyze
     * @return Map containing flow analysis information
     */
    private static Map<String, Object> generateMessageFlowSummary(List<Map<String, Object>> messages) {
        Map<String, Object> flowSummary = new HashMap<>();
        
        if (messages.isEmpty()) {
            flowSummary.put("pattern", "No messages recorded");
            return flowSummary;
        }
        
        // Count message directions
        int outboundCount = 0;
        int inboundCount = 0;
        Map<String, Integer> functionCounts = new HashMap<>();
        
        for (Map<String, Object> message : messages) {
            String direction = (String) message.get("direction");
            if ("out".equals(direction)) {
                outboundCount++;
            } else if ("in".equals(direction)) {
                inboundCount++;
            }
            
            String function = (String) message.get("function");
            if (function != null) {
                functionCounts.put(function, functionCounts.getOrDefault(function, 0) + 1);
            }
        }
        
        flowSummary.put("outboundMessages", outboundCount);
        flowSummary.put("inboundMessages", inboundCount);
        flowSummary.put("functionBreakdown", functionCounts);
        flowSummary.put("totalExchanges", Math.min(outboundCount, inboundCount));
        
        // Determine flow pattern
        if (outboundCount == 0 && inboundCount == 0) {
            flowSummary.put("pattern", "No communication");
        } else if (outboundCount == 0) {
            flowSummary.put("pattern", "Game-only communication");
        } else if (inboundCount == 0) {
            flowSummary.put("pattern", "Launcher-only communication");
        } else {
            flowSummary.put("pattern", "Two-way communication");
        }
        
        return flowSummary;
    }
    
    /**
     * Gets the message type based on the function.
     * 
     * <p>This method categorizes messages by their function into types such as
     * game_initialization, game_termination, acknowledgment, etc.
     * 
     * @param function The message function
     * @return The message type string, or "unknown" if function is null
     */
    private static String getMessageType(String function) {
        if (function == null) return "unknown";
        
        switch (function) {
            case "init": return "game_initialization";
            case "end": return "game_termination";
            case "ack": return "acknowledgment";
            case "message": return "player_communication";
            case "chat": return "chat_message";
            case "metadata": return "metadata_request";
            default: return "other";
        }
    }
    
    /**
     * Generates a human-readable summary for a message based on its function and details.
     * 
     * <p>This method creates a summary string for different message types to make
     * transcripts more readable. Returns null for message types that don't need summaries.
     * 
     * @param function The message function
     * @param details The message details map
     * @return A human-readable summary string, or null if no summary is needed
     */
    private static String generateMessageSummary(String function, Map<String, Object> details) {
        if (function == null) return null;
        
        switch (function) {
            case "init":
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
                    
            case "metadata":
                return "Metadata request";
                
            default:
                return null; // No summary for unknown functions
        }
    }

    /**
     * Formats an Instant object into a human-readable string.
     * 
     * <p>This method formats timestamps in the format "YYYY-MM-DD HH:MM:SS.SSS"
     * by removing the 'T' separator and 'Z' timezone indicator, and truncating
     * to millisecond precision.
     * 
     * @param instant The Instant object to format
     * @return A string in the format "YYYY-MM-DD HH:MM:SS.SSS"
     */
    private static String formatTimestamp(Instant instant) {
        String timestamp = instant.toString();
        // Remove the 'T' and 'Z', and truncate to millisecond precision
        timestamp = timestamp.replace("T", " ").replace("Z", "");
        // If there are more than 3 decimal places after the second, truncate to 3
        if (timestamp.contains(".")) {
            String[] parts = timestamp.split("\\.");
            if (parts.length == 2 && parts[1].length() > 3) {
                timestamp = parts[0] + "." + parts[1].substring(0, 3);
            }
        }
        return timestamp;
    }
    
}

