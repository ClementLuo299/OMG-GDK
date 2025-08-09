package launcher.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdk.Logging;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class StartMessageUtil {
    private static final String DEFAULT_FILE_NAME = "start-message.example.json";

    private StartMessageUtil() {}

    public static Map<String, Object> loadDefaultStartMessage() {
        Map<String, Object> msg = loadStartMessage(Path.of(DEFAULT_FILE_NAME));
        if (msg != null) {
            // Ensure localPlayerId is present for convenience
            if (!msg.containsKey("localPlayerId")) {
                Object players = msg.get("players");
                if (players instanceof java.util.List && !((java.util.List<?>) players).isEmpty()) {
                    Object first = ((java.util.List<?>) players).get(0);
                    if (first instanceof java.util.Map) {
                        Object id = ((java.util.Map<?, ?>) first).get("id");
                        if (id != null) {
                            msg.put("localPlayerId", String.valueOf(id));
                        }
                    }
                }
            }
        }
        return msg;
    }

    public static Map<String, Object> loadStartMessage(Path filePath) {
        try {
            if (filePath == null) {
                return null;
            }
            File file = filePath.toFile();
            if (!file.exists() || !file.isFile() || !file.canRead()) {
                Logging.info("Start message file not found: " + file.getAbsolutePath());
                return null;
            }
            String json = Files.readString(file.toPath());
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> data = mapper.readValue(json, Map.class);
            return data;
        } catch (Exception e) {
            Logging.error("Failed to load start message: " + e.getMessage(), e);
            return null;
        }
    }
} 