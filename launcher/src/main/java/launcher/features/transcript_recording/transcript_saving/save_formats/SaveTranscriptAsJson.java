package launcher.features.transcript_recording.transcript_saving.save_formats;

import com.fasterxml.jackson.databind.ObjectMapper;
import launcher.features.file_handling.directory_existence.ParentDirectoryExistenceCheck;
import launcher.features.file_handling.file_paths.GenerateTranscriptFilePath;
import launcher.features.transcript_recording.Transcript;
import launcher.features.transcript_recording.transcript_saving.save_formats.helpers.FormatTimestamp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles saving transcripts in JSON format.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class SaveTranscriptAsJson {
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    private SaveTranscriptAsJson() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Saves the transcript to a JSON file.
     * 
     * @param targetFile The target file path, or null for auto-generation
     * @return The path to the saved transcript file, or null if saving failed
     */
    public static Path save(Path targetFile) {
        try {
            if (targetFile == null) {
                targetFile = GenerateTranscriptFilePath.generate(".json");
            }
            
            ParentDirectoryExistenceCheck.exists(targetFile);
            Files.deleteIfExists(targetFile);
            
            Map<String, Object> transcript = new HashMap<>();
            
            Map<String, Object> header = new HashMap<>();
            header.put("generatedAt", FormatTimestamp.format(Instant.now()));
            header.put("title", "Game Session Transcript");
            transcript.put("header", header);
            
            List<Map<String, Object>> messages = new ArrayList<>();
            for (Map<String, Object> entry : Transcript.entries) {
                if (!"meta".equals(entry.get("type"))) {
                    Map<String, Object> messageEntry = new HashMap<>();
                    
                    String timestamp = (String) entry.get("timestamp");
                    messageEntry.put("timestamp", FormatTimestamp.format(Instant.parse(timestamp)));
                    messageEntry.put("direction", entry.get("direction"));
                    
                    if (entry.get("message") instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> msg = (Map<String, Object>) entry.get("message");
                        String function = (String) msg.get("function");
                        messageEntry.put("function", function);
                        
                        for (Map.Entry<String, Object> field : msg.entrySet()) {
                            messageEntry.put(field.getKey(), field.getValue());
                        }
                    }
                    
                    messages.add(messageEntry);
                }
            }
            
            // Add game ui_initialization message if available
            for (Map<String, Object> entry : Transcript.entries) {
                if ("meta".equals(entry.get("type")) && "session_start".equals(entry.get("event"))) {
                    Map<String, Object> startMessage = new HashMap<>();
                    String timestamp = (String) entry.get("timestamp");
                    startMessage.put("timestamp", FormatTimestamp.format(Instant.parse(timestamp)));
                    startMessage.put("direction", "in");
                    startMessage.put("function", "ui_initialization");
                    startMessage.put("gameName", entry.get("gameName"));
                    startMessage.put("gameVersion", entry.get("gameVersion"));
                    startMessage.put("event", "session_start");
                    messages.add(0, startMessage);
                    break;
                }
            }
            
            transcript.put("messages", messages);
            
            String transcriptJson = JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(transcript);
            transcriptJson = transcriptJson.replaceAll("\\},\\s*\\{", "},\n\n    {");
            
            Files.writeString(targetFile, transcriptJson);
            return targetFile;
        } catch (IOException e) {
            System.err.println("❌ Error saving JSON transcript: " + e.getMessage());
            return null;
        }
    }
}

