package launcher.features.file_paths;

/**
 * Utility class for centralized file file_paths constants.
 * 
 * This class contains all file paths used throughout the GDK application
 * for persistence, configuration, and saved data. Centralizing these paths
 * makes it easier to maintain and update file locations.
 * 
 * @authors Clement Luo
 * @date December 19, 2025
 * @since 1.0
 */
public final class FilePaths {
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static constants.
     */
    private FilePaths() {
        throw new AssertionError("FilePaths should not be instantiated");
    }
    
    // ==================== PERSISTENCE PATHS ====================
    
    /**
     * Path to the JSON persistence file for game configuration.
     * Stores the JSON input that was last used to launch a game.
     */
    public static final String JSON_PERSISTENCE_FILE = "saved/gdk-json_processing-persistence.txt";
    
    /**
     * Path to the persistence toggle state file.
     * Stores whether JSON persistence is enabled or disabled.
     */
    public static final String PERSISTENCE_TOGGLE_FILE = "saved/gdk-persistence-toggle.txt";
    
    /**
     * Path to the selected game file.
     * Stores the name of the last selected game module.
     */
    public static final String SELECTED_GAME_FILE = "saved/gdk-selected-game.txt";
    
    /**
     * Path to the auto-launch enabled flag file.
     * Stores whether auto-launch functionality is enabled.
     */
    public static final String AUTO_LAUNCH_ENABLED_FILE = "saved/gdk-auto-launch-enabled.txt";
    
    /**
     * Path to the auto-select enabled flag file.
     * Stores whether auto-select functionality is enabled.
     */
    public static final String AUTO_SELECT_ENABLED_FILE = "saved/gdk-auto-select-enabled.txt";
    
    // ==================== SERVER SIMULATOR PATHS ====================
    
    /**
     * Path to the server simulator input persistence file.
     * Stores the last input sent to the server simulator.
     */
    public static final String SERVER_SIMULATOR_INPUT_FILE = "saved/server-simulator-input.txt";
    
    // ==================== START MESSAGE PATHS ====================
    
    /**
     * Path to the example start message file.
     * Contains a template/example start message configuration.
     */
    public static final String START_MESSAGE_EXAMPLE_FILE = "saved/start-message.example.json_processing";
    
    /**
     * Path to the local multiplayer start message file.
     * Contains the start message configuration for local multiplayer mode.
     */
    public static final String START_MESSAGE_LOCAL_MULTIPLAYER_FILE = "saved/start-message-local-multiplayer.json_processing";
    
    /**
     * Path to the single player start message file.
     * Contains the start message configuration for single player mode.
     */
    public static final String START_MESSAGE_SINGLE_PLAYER_FILE = "saved/start-message-single-player.json_processing";
    
    /**
     * Path to the all modes start message file.
     * Contains the start message configuration for all game modes.
     */
    public static final String START_MESSAGE_ALL_MODES_FILE = "saved/start-message-all-modes.json_processing";
}

