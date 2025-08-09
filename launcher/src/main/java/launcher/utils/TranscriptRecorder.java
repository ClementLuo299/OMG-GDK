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

    public static void endSessionIfEndDetected(Map<String, Object> message) {
        if (message == null) return;
        Object fn = message.get("function");
        if (fn != null && "end".equals(String.valueOf(fn))) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("type", "meta");
            meta.put("event", "session_end");
            meta.put("timestamp", Instant.now().toString());
            transcriptEntries.add(meta);
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
        endSessionIfEndDetected(message);
    }

    public static void recordFromGame(Map<String, Object> message) {
        if (!inSession || message == null) return;
        Map<String, Object> entry = new HashMap<>();
        entry.put("direction", "fromGame");
        entry.put("timestamp", Instant.now().toString());
        entry.put("message", new HashMap<>(message));
        transcriptEntries.add(entry);
        endSessionIfEndDetected(message);
    }

    public static Path saveTranscript(Path targetFile) {
                try {
            if (targetFile == null) {
                String fileName = "saved/transcript-" + Instant.now().toString().replace(":", "-") + ".jsonl";
                targetFile = Path.of(fileName);
            }
            ObjectMapper mapper = new ObjectMapper();
            // Ensure parent directory exists
            if (targetFile.getParent() != null) {
                Files.createDirectories(targetFile.getParent());
            }
            Files.deleteIfExists(targetFile);
            for (Map<String, Object> entry : new ArrayList<>(transcriptEntries)) {
                String line = mapper.writeValueAsString(entry) + System.lineSeparator();
                Files.writeString(targetFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
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
} 