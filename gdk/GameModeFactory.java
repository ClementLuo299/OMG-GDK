package gdk;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating custom game modes.
 * Provides utility methods for games to easily create their own unique modes.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class GameModeFactory {
    
    /**
     * Creates a custom single-player mode.
     * 
     * @param id Unique identifier
     * @param displayName Human-readable name
     * @param description Description of the mode
     * @param icon Icon emoji
     * @return The custom game mode
     */
    public static GameMode createSinglePlayerMode(String id, String displayName, String description, String icon) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "Single Player");
        properties.put("maxPlayers", 1);
        properties.put("requiresAI", true);
        
        return new GameMode(id, displayName, description, icon, "#007bff", properties);
    }
    
    /**
     * Creates a custom local multiplayer mode.
     * 
     * @param id Unique identifier
     * @param displayName Human-readable name
     * @param description Description of the mode
     * @param icon Icon emoji
     * @param maxPlayers Maximum number of players
     * @return The custom game mode
     */
    public static GameMode createLocalMultiplayerMode(String id, String displayName, String description, String icon, int maxPlayers) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "Local Multiplayer");
        properties.put("maxPlayers", maxPlayers);
        properties.put("requiresNetwork", false);
        
        return new GameMode(id, displayName, description, icon, "#28a745", properties);
    }
    
    /**
     * Creates a custom online multiplayer mode.
     * 
     * @param id Unique identifier
     * @param displayName Human-readable name
     * @param description Description of the mode
     * @param icon Icon emoji
     * @param maxPlayers Maximum number of players
     * @param isRanked Whether this is a ranked mode
     * @return The custom game mode
     */
    public static GameMode createOnlineMultiplayerMode(String id, String displayName, String description, String icon, int maxPlayers, boolean isRanked) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "Online Multiplayer");
        properties.put("maxPlayers", maxPlayers);
        properties.put("requiresNetwork", true);
        properties.put("isRanked", isRanked);
        
        String colorCode = isRanked ? "#dc3545" : "#fd7e14";
        return new GameMode(id, displayName, description, icon, colorCode, properties);
    }
    
    /**
     * Creates a custom time-based mode.
     * 
     * @param id Unique identifier
     * @param displayName Human-readable name
     * @param description Description of the mode
     * @param icon Icon emoji
     * @param timeLimit Time limit in seconds
     * @return The custom game mode
     */
    public static GameMode createTimeBasedMode(String id, String displayName, String description, String icon, int timeLimit) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "Time Based");
        properties.put("timeLimit", timeLimit);
        properties.put("hasTimer", true);
        
        return new GameMode(id, displayName, description, icon, "#6f42c1", properties);
    }
    
    /**
     * Creates a custom survival mode.
     * 
     * @param id Unique identifier
     * @param displayName Human-readable name
     * @param description Description of the mode
     * @param icon Icon emoji
     * @param endless Whether the mode is endless
     * @return The custom game mode
     */
    public static GameMode createSurvivalMode(String id, String displayName, String description, String icon, boolean endless) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "Survival");
        properties.put("endless", endless);
        properties.put("hasLives", true);
        
        return new GameMode(id, displayName, description, icon, "#6f42c1", properties);
    }
    
    /**
     * Creates a custom puzzle mode.
     * 
     * @param id Unique identifier
     * @param displayName Human-readable name
     * @param description Description of the mode
     * @param icon Icon emoji
     * @param puzzleType Type of puzzle (e.g., "logic", "pattern", "word")
     * @return The custom game mode
     */
    public static GameMode createPuzzleMode(String id, String displayName, String description, String icon, String puzzleType) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "Puzzle");
        properties.put("puzzleType", puzzleType);
        properties.put("hasHints", true);
        
        return new GameMode(id, displayName, description, icon, "#6f42c1", properties);
    }
    
    /**
     * Creates a custom AI mode.
     * 
     * @param id Unique identifier
     * @param displayName Human-readable name
     * @param description Description of the mode
     * @param icon Icon emoji
     * @param aiType Type of AI (e.g., "versus", "coop", "training")
     * @return The custom game mode
     */
    public static GameMode createAIMode(String id, String displayName, String description, String icon, String aiType) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("category", "AI");
        properties.put("aiType", aiType);
        properties.put("requiresAI", true);
        
        return new GameMode(id, displayName, description, icon, "#fd7e14", properties);
    }
    
    /**
     * Creates a completely custom game mode with full control.
     * 
     * @param id Unique identifier
     * @param displayName Human-readable name
     * @param description Description of the mode
     * @param icon Icon emoji
     * @param colorCode CSS color code
     * @param properties Custom properties
     * @return The custom game mode
     */
    public static GameMode createCustomMode(String id, String displayName, String description, String icon, String colorCode, Map<String, Object> properties) {
        return new GameMode(id, displayName, description, icon, colorCode, properties);
    }
} 