package launcher.features.game_launching;

import gdk.api.GameModule;
import gdk.internal.Logging;
import com.fasterxml.jackson.databind.ObjectMapper;
import launcher.features.game_messaging.StartMessageUtil;
import launcher.features.game_messaging.TranscriptRecorder;

import java.util.Map;

/**
 * Utility class for launching games with JSON configuration.
 * 
 * <p>This class has a single responsibility: providing the core launch logic
 * for games with JSON configuration that can be used by both the GDK controller
 * and auto-launch functionality.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Validating game module selection</li>
 *   <li>Applying JSON configuration to game modules</li>
 *   <li>Sending start messages to games</li>
 *   <li>Recording configuration and messages to transcripts</li>
 *   <li>Parsing JSON configuration strings</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date August 11, 2025
 * @edited August 11, 2025
 * @since 1.0
 */
public class GameLaunchUtil {
    
    // ==================== CONSTANTS ====================
    
    /** Shared ObjectMapper instance for JSON parsing. */
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private GameLaunchUtil() {
        throw new AssertionError("GameLaunchUtil should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - GAME LAUNCHING ====================
    
    /**
     * Launches a game module with JSON configuration data.
     * 
     * <p>This method replicates the exact behavior of pressing "Launch Game" in the GDK.
     * It performs the following steps:
     * <ol>
     *   <li>Validates that a game module is selected</li>
     *   <li>Applies JSON configuration to the game module (if provided)</li>
     *   <li>Loads and sends the default start message</li>
     *   <li>Records all messages to the transcript</li>
     * </ol>
     * 
     * <p>Note: The actual scene creation and stage management is handled by the ViewModel.
     * This method just prepares the game with configuration and start messages.
     * 
     * @param gameModule The game module to launch
     * @param jsonConfigurationData The JSON configuration data, or null for defaults
     * @param isAutoLaunch Whether this is an auto-launch (affects logging verbosity)
     * @return true if the launch process was initiated successfully, false otherwise
     */
    public static boolean launchGameWithConfiguration(GameModule gameModule, Map<String, Object> jsonConfigurationData, boolean isAutoLaunch) {
        try {
            // Step 1: Validate that a game is selected
            if (gameModule == null) {
                if (!isAutoLaunch) {
                    Logging.error("No game selected for launch");
                }
                return false;
            }

            // Step 2: Handle JSON configuration data
            if (jsonConfigurationData == null) {
                if (!isAutoLaunch) {
                    Logging.info("📦 No JSON data provided (game will use its own defaults)");
                }
            } else {
                // Log information about the JSON data being included
                if (!isAutoLaunch) {
                    Logging.info("📦 Including custom JSON data with " + jsonConfigurationData.size() + " fields");
                }
            }
            
            if (!isAutoLaunch) {
                Logging.info("🚀 Launching " + gameModule.getMetadata().getGameName());
            }
            
            // Step 3: Send JSON configuration to game if provided
            if (jsonConfigurationData != null) {
                try {
                    // Record configuration to transcript
                    TranscriptRecorder.recordToGame(jsonConfigurationData);
                    
                    // Send the configuration to the game
                    Map<String, Object> response = gameModule.handleMessage(jsonConfigurationData);
                    if (response != null && !isAutoLaunch) {
                        Logging.info("✅ Configuration acknowledged by " + gameModule.getMetadata().getGameName());
                        // Record response to transcript
                        TranscriptRecorder.recordFromGame(response);
                    }
                } catch (Exception e) {
                    if (!isAutoLaunch) {
                        Logging.error("⚠️ Failed to send configuration: " + e.getMessage());
                    }
                }
            }
            
            // Step 4: Load and send start message
            Map<String, Object> startMessage = StartMessageUtil.loadDefaultStartMessage();
            if (startMessage != null) {
                try {
                    // Record start message to transcript
                    TranscriptRecorder.recordToGame(startMessage);
                    
                    Map<String, Object> response = gameModule.handleMessage(startMessage);
                    if (response != null && !isAutoLaunch) {
                        Logging.info("✅ Start message acknowledged by " + gameModule.getMetadata().getGameName());
                        // Record response to transcript
                        TranscriptRecorder.recordFromGame(response);
                    }
                } catch (Exception e) {
                    if (!isAutoLaunch) {
                        Logging.error("⚠️ Failed to send start message: " + e.getMessage());
                    }
                }
            }
            
            // Step 5: Launch the game
            // Note: The actual scene creation and stage management is handled by the ViewModel
            // This method just prepares the game with configuration and start messages
            
            return true;
            
        } catch (Exception e) {
            Logging.error("Error in game launch process: " + e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== PUBLIC METHODS - JSON PARSING ====================
    
    /**
     * Parses a JSON string into a Map for configuration.
     * 
     * <p>This method parses a JSON configuration string into a Map structure
     * that can be used to configure game modules. Returns null if the input
     * is null, empty, or parsing fails.
     * 
     * @param jsonString The JSON string to parse
     * @return The parsed Map, or null if parsing fails or input is empty
     */
    public static Map<String, Object> parseJsonString(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return jsonMapper.readValue(jsonString.trim(), Map.class);
        } catch (Exception e) {
            Logging.error("Failed to parse JSON: " + e.getMessage());
            return null;
        }
    }
}

