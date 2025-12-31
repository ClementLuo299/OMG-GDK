package launcher.utils.game;

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
 * Utility class for managing start messages for different game modes.
 * 
 * <p>This class has a single responsibility: loading and managing start messages
 * for different game modes (single player, local multiplayer, all modes).
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Loading start messages from JSON files</li>
 *   <li>Creating default start messages if files don't exist</li>
 *   <li>Ensuring required fields (like localPlayerId) are present</li>
 *   <li>Managing the saved directory for start message files</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 12, 2025
 * @since 1.0
 */
public final class StartMessageUtil {
    
    // ==================== CONSTANTS ====================
    
    /** Path to the default start message file. */
    private static final String DEFAULT_FILE_NAME = "saved/start-message.example.json";
    
    /** Path to the local multiplayer start message file. */
    private static final String LOCAL_MULTIPLAYER_FILE = "saved/start-message-local-multiplayer.json";
    
    /** Path to the single player start message file. */
    private static final String SINGLE_PLAYER_FILE = "saved/start-message-single-player.json";
    
    /** Path to the all modes start message file. */
    private static final String ALL_MODES_FILE = "saved/start-message-all-modes.json";
    
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
     * Loads the default start message.
     * 
     * <p>This method loads the default start message from file. If the file doesn't exist,
     * it creates a default start message and saves it for future use. The method also
     * ensures that the localPlayerId field is present in the message.
     * 
     * @return The start message map, or null if loading fails
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
     * Loads a start message for local multiplayer mode.
     * 
     * @return The start message map for local multiplayer, or null if loading fails
     */
    public static Map<String, Object> loadLocalMultiplayerStartMessage() {
        return loadStartMessage(Path.of(LOCAL_MULTIPLAYER_FILE));
    }
    
    /**
     * Loads a start message for single player mode.
     * 
     * @return The start message map for single player, or null if loading fails
     */
    public static Map<String, Object> loadSinglePlayerStartMessage() {
        return loadStartMessage(Path.of(SINGLE_PLAYER_FILE));
    }
    
    /**
     * Loads a start message for all modes (multi player).
     * 
     * @return The start message map for all modes, or null if loading fails
     */
    public static Map<String, Object> loadAllModesStartMessage() {
        return loadStartMessage(Path.of(ALL_MODES_FILE));
    }
    
    // ==================== PRIVATE METHODS - LOADING ====================
    
    /**
     * Loads a start message from a file path.
     * 
     * <p>This method reads a JSON file and parses it into a Map structure.
     * Returns null if the file doesn't exist, is not readable, or parsing fails.
     * 
     * @param filePath The path to the start message file
     * @return The parsed start message map, or null if loading fails
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
            Logging.error("Failed to load start message: " + e.getMessage(), e);
            return null;
        }
    }

    // ==================== PRIVATE METHODS - CREATION ====================
    
    /**
     * Creates a default start message with standard game configuration.
     * 
     * <p>This method creates a default start message with:
     * <ul>
     *   <li>Function: "start"</li>
     *   <li>Game mode: "multi_player"</li>
     *   <li>Local player ID: "p1"</li>
     *   <li>Two players (p1 as host, p2 as guest)</li>
     * </ul>
     * 
     * @return A default start message map
     */
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

