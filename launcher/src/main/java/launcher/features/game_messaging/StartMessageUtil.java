package launcher.features.game_messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdk.internal.Logging;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for managing init messages for different game modes.
 * 
 * <p>This class has a single responsibility: ui_loading and managing init messages
 * for different game modes (single player, local multiplayer, all modes).
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Loading init messages from JSON files</li>
 *   <li>Creating default init messages if files don't exist</li>
 *   <li>Ensuring required fields (like localPlayerId) are present</li>
 *   <li>Managing the saved directory for init message files</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 12, 2025
 * @since 1.0
 */
public final class StartMessageUtil {
    
    // ==================== CONSTANTS ====================
    
    /** Path to the default init message file. */
    private static final String DEFAULT_FILE_NAME = "saved/init-message.example.json_processing";
    
    /** Path to the local multiplayer init message file. */
    private static final String LOCAL_MULTIPLAYER_FILE = "saved/init-message-local-multiplayer.json_processing";
    
    /** Path to the single player init message file. */
    private static final String SINGLE_PLAYER_FILE = "saved/init-message-single-player.json_processing";
    
    /** Path to the all modes init message file. */
    private static final String ALL_MODES_FILE = "saved/init-message-all-modes.json_processing";
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private StartMessageUtil() {
        throw new AssertionError("StartMessageUtil should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - LOADING ====================
    
    /**
     * Loads the default init message.
     * 
     * <p>This method loads the default init message from file. If the file doesn't exist,
     * it creates a default init message and saves it for future use. The method also
     * ensures that the localPlayerId field is present in the message.
     * 
     * @return The init message map, or null if ui_loading fails
     */
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
            // If file doesn't exist, create a default init message
            msg = createDefaultStartMessage();
            // Try to save it for future use
            try {
                ensureSavedDirectoryExists();
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(msg);
                Files.writeString(Path.of(DEFAULT_FILE_NAME), json);
                Logging.info("📝 Created default init message file: " + DEFAULT_FILE_NAME);
            } catch (Exception e) {
                Logging.error("Failed to save default init message: " + e.getMessage(), e);
            }
        }
        return msg;
    }
    
    /**
     * Loads a init message for local multiplayer mode.
     * 
     * @return The init message map for local multiplayer, or null if ui_loading fails
     */
    public static Map<String, Object> loadLocalMultiplayerStartMessage() {
        return loadStartMessage(Path.of(LOCAL_MULTIPLAYER_FILE));
    }
    
    /**
     * Loads a init message for single player mode.
     * 
     * @return The init message map for single player, or null if ui_loading fails
     */
    public static Map<String, Object> loadSinglePlayerStartMessage() {
        return loadStartMessage(Path.of(SINGLE_PLAYER_FILE));
    }
    
    /**
     * Loads a init message for all modes (multi player).
     * 
     * @return The init message map for all modes, or null if ui_loading fails
     */
    public static Map<String, Object> loadAllModesStartMessage() {
        return loadStartMessage(Path.of(ALL_MODES_FILE));
    }
    
    // ==================== PUBLIC METHODS - VALIDATION ====================
    
    /**
     * Parses and validates a start message string.
     * 
     * @param jsonText The JSON string to parse and validate
     * @return The parsed and validated start message map
     * @throws IllegalStateException If the string is empty, invalid JSON, or not a valid start message
     */
    public static Map<String, Object> parseAndValidateStartMessage(String jsonText) {
        if (jsonText == null || jsonText.trim().isEmpty()) {
            throw new IllegalStateException("Start message is required");
        }
        
        Map<String, Object> message = launcher.features.json_processing.JsonParser.parse(jsonText);
        if (message == null) {
            throw new IllegalStateException("Invalid JSON in start message");
        }
        
        validateStartMessage(message);
        return message;
    }
    
    /**
     * Validates that a message map is a valid start message.
     * A valid start message must have function="start" or function="init".
     * 
     * @param message The message map to validate
     * @throws IllegalStateException If the message is not a valid start message
     */
    public static void validateStartMessage(Map<String, Object> message) {
        if (message == null) {
            throw new IllegalStateException("Start message is required");
        }
        
        Object function = message.get("function");
        if (!(function instanceof String)) {
            throw new IllegalStateException("Start message must have a 'function' field");
        }
        
        String func = (String) function;
        if (!"start".equals(func) && !"init".equals(func)) {
            throw new IllegalStateException("Start message must have function='start' or function='init', got: " + func);
        }
    }
    
    // ==================== PRIVATE METHODS - LOADING ====================
    
    /**
     * Loads a init message from a file file_handling.
     * 
     * <p>This method reads a JSON file and parses it into a Map structure.
     * Returns null if the file doesn't exist, is not readable, or parsing fails.
     * 
     * @param filePath The file_handling to the init message file
     * @return The parsed init message map, or null if ui_loading fails
     */
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
            Logging.error("Failed to load init message: " + e.getMessage(), e);
            return null;
        }
    }

    // ==================== PRIVATE METHODS - CREATION ====================
    
    /**
     * Creates a default init message with standard game configuration.
     * 
     * <p>This method creates a default init message with:
     * <ul>
     *   <li>Function: "init"</li>
     *   <li>Game mode: "multi_player"</li>
     *   <li>Local player ID: "p1"</li>
     *   <li>Two players (p1 as host, p2 as guest)</li>
     * </ul>
     * 
     * @return A default init message map
     */
    private static Map<String, Object> createDefaultStartMessage() {
        Map<String, Object> defaultMsg = new HashMap<>();
        defaultMsg.put("function", "init");
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
    
    // ==================== PRIVATE METHODS - UTILITY ====================
    
    /**
     * Ensures the saved directory exists, creating it if necessary.
     * 
     * <p>This method creates the "saved" directory if it doesn't exist.
     * Errors are logged but do not throw exceptions.
     */
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

