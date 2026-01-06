package launcher.features.transcript_recording.transcript_saving.save_formats;

import launcher.features.file_handling.directory_existence.ParentDirectoryExistenceCheck;
import launcher.features.file_handling.file_paths.GenerateTranscriptFilePath;
import launcher.features.transcript_recording.Transcript;
import launcher.features.transcript_recording.transcript_saving.save_formats.helpers.FormatTimestamp;
import launcher.features.transcript_recording.transcript_saving.save_formats.helpers.MessageSummary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

/**
 * Handles saving transcripts in text format.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class SaveTranscriptAsText {
    
    private SaveTranscriptAsText() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Saves the transcript in a human-readable text format.
     * 
     * @param targetFile The target file path, or null for auto-generation
     * @return The path to the saved transcript file, or null if saving failed
     */
    public static Path save(Path targetFile) {
        try {
            if (targetFile == null) {
                targetFile = GenerateTranscriptFilePath.generate(".txt");
            }
            
            ParentDirectoryExistenceCheck.exists(targetFile);
            Files.deleteIfExists(targetFile);
            
            StringBuilder textTranscript = new StringBuilder();
            
            textTranscript.append("=".repeat(80)).append("\n");
            textTranscript.append("GAME SESSION TRANSCRIPT\n");
            textTranscript.append("=".repeat(80)).append("\n\n");
            
            String gameName = "unknown";
            String gameVersion = "unknown";
            String sessionStart = null;
            String sessionEnd = null;
            
            for (Map<String, Object> entry : Transcript.entries) {
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
            textTranscript.append("SESSION START: ").append(sessionStart != null ? FormatTimestamp.format(Instant.parse(sessionStart)) : "unknown").append("\n");
            textTranscript.append("SESSION END: ").append(sessionEnd != null ? FormatTimestamp.format(Instant.parse(sessionEnd)) : "unknown").append("\n");
            textTranscript.append("\n");
            
            textTranscript.append("MESSAGE FLOW:\n");
            textTranscript.append("-".repeat(80)).append("\n\n");
            
            // Add game init message first if available
            for (Map<String, Object> entry : Transcript.entries) {
                if ("meta".equals(entry.get("type")) && "session_start".equals(entry.get("event"))) {
                    textTranscript.append("Message #1\n");
                    textTranscript.append("Time: ").append(FormatTimestamp.format(Instant.parse((String) entry.get("timestamp")))).append("\n");
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
            
            int messageNumber = 2;
            for (Map<String, Object> entry : Transcript.entries) {
                if (!"meta".equals(entry.get("type"))) {
                    String direction = (String) entry.get("direction");
                    String timestamp = (String) entry.get("timestamp");
                    
                    textTranscript.append("Message #").append(messageNumber++).append("\n");
                    textTranscript.append("Time: ").append(FormatTimestamp.format(Instant.parse(timestamp))).append("\n");
                    textTranscript.append("Direction: ").append(direction.toUpperCase()).append("\n");
                    
                    if (entry.get("message") instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> msg = (Map<String, Object>) entry.get("message");
                        String function = (String) msg.get("function");
                        
                        textTranscript.append("Function: ").append(function != null ? function : "none").append("\n");
                        
                        String summary = MessageSummary.generate(function, msg);
                        if (summary != null) {
                            textTranscript.append("Summary: ").append(summary).append("\n");
                        }
                        
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
            
            // Add end message if available
            for (Map<String, Object> entry : Transcript.entries) {
                if ("meta".equals(entry.get("type")) && "session_end".equals(entry.get("event"))) {
                    if (messageNumber > 2) {
                        textTranscript.append("Message #").append(messageNumber++).append("\n");
                        textTranscript.append("Time: ").append(FormatTimestamp.format(Instant.now())).append("\n");
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
            
            textTranscript.append("-".repeat(80)).append("\n");
            textTranscript.append("END OF TRANSCRIPT\n");
            textTranscript.append("Generated at: ").append(FormatTimestamp.format(Instant.now())).append("\n");
            textTranscript.append("=".repeat(80)).append("\n");
            
            Files.writeString(targetFile, textTranscript.toString());
            return targetFile;
        } catch (IOException e) {
            System.err.println("❌ Error saving text transcript: " + e.getMessage());
            return null;
        }
    }
}

