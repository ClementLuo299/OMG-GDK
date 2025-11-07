package launcher.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdk.infrastructure.Logging;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for managing start messages for different game modes.
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 12, 2025
 * @since 1.0
 */
public final class StartMessageUtil {
    private static final String DEFAULT_FILE_NAME = "saved/start-message.example.json";
    private static final String LOCAL_MULTIPLAYER_FILE = "saved/start-message-local-multiplayer.json";
    private static final String SINGLE_PLAYER_FILE = "saved/start-message-single-player.json";
    private static final String ALL_MODES_FILE = "saved/start-message-all-modes.json";

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
        } else {
            // If file doesn't exist, create a default start message
            msg = createDefaultStartMessage();
            // Try to save it for future use
            try {
                ensureSavedDirectoryExists();
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(msg);
                Files.writeString(Path.of(DEFAULT_FILE_NAME), json);
                Logging.info("📝 Created default start message file: " + DEFAULT_FILE_NAME);
            } catch (Exception e) {
                Logging.error("Failed to save default start message: " + e.getMessage(), e);
            }
        }
        return msg;
    }
    
    /**
     * Load a start message for local multiplayer mode
     */
    public static Map<String, Object> loadLocalMultiplayerStartMessage() {
        return loadStartMessage(Path.of(LOCAL_MULTIPLAYER_FILE));
    }
    
    /**
     * Load a start message for single player mode
     */
    public static Map<String, Object> loadSinglePlayerStartMessage() {
        return loadStartMessage(Path.of(SINGLE_PLAYER_FILE));
    }
    
    /**
     * Load a start message for all modes (multi player)
     */
    public static Map<String, Object> loadAllModesStartMessage() {
        return loadStartMessage(Path.of(ALL_MODES_FILE));
    }

    private static Map<String, Object> createDefaultStartMessage() {
        Map<String, Object> defaultMsg = new HashMap<>();
        defaultMsg.put("function", "start");
        defaultMsg.put("gameMode", "multi_player");
        defaultMsg.put("localPlayerId", "p1");
        
        List<Map<String, Object>> players = new ArrayList<>();
        Map<String, Object> player1 = new HashMap<>();
        player1.put("id", "p1");
        player1.put("name", "Player1");
        player1.put("role", "host");
        players.add(player1);
        
        Map<String, Object> player2 = new HashMap<>();
        player2.put("id", "p2");
        player2.put("name", "Player2");
        player2.put("role", "guest");
        players.add(player2);
        
        defaultMsg.put("players", players);
        return defaultMsg;
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

    public static void ensureSavedDirectoryExists() {
        try {
            Path savedDir = Path.of("saved");
            if (!Files.exists(savedDir)) {
                Files.createDirectories(savedDir);
                Logging.info("📁 Created saved directory: " + savedDir.toAbsolutePath());
            }
        } catch (Exception e) {
            Logging.error("Failed to create saved directory: " + e.getMessage(), e);
        }
    }
} 