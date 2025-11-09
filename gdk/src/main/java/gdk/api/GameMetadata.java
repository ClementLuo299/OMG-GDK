package gdk.api;

import gdk.infrastructure.GameMode;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Metadata class for game modules.
 * Contains all the metadata information that every game module must provide.
 * This class is designed to be extended by each game module to provide
 * their specific metadata information.
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @edited November 8, 2025
 * @since Beta 1.0
 */
public abstract class GameMetadata {
    
    // ==================== ABSTRACT METHODS ====================
    
    /**
     * Get the display name of the game.
     * This is the name that will be shown in the UI.
     * 
     * @return The display name of the game
     */
    public abstract String getGameName();
    
    /**
     * Get the version of the game.
     * 
     * @return The game version
     */
    public abstract String getGameVersion();
    
    /**
     * Get the description of the game.
     * 
     * @return The game description
     */
    public abstract String getGameDescription();
    
    /**
     * Get the author of the game.
     * 
     * @return The game author
     */
    public abstract String getGameAuthor();
    
    // ==================== REQUIREMENTS ====================
    
    /**
     * Get the minimum number of players required.
     * 
     * @return The minimum number of players
     */
    public abstract int getMinPlayers();
    
    /**
     * Get the maximum number of players supported.
     * 
     * @return The maximum number of players
     */
    public abstract int getMaxPlayers();
    
    /**
     * Get the minimum difficulty level.
     * 
     * @return The minimum difficulty level
     */
    public abstract String getMinDifficulty();
    
    /**
     * Get the maximum difficulty level.
     * 
     * @return The maximum difficulty level
     */
    public abstract String getMaxDifficulty();
    
    /**
     * Get the estimated duration of a game in minutes.
     * 
     * @return The estimated duration in minutes
     */
    public abstract int getEstimatedDurationMinutes();
    
    // ==================== UTILITY METHODS ====================

    /**
     * Get the set of supported game modes for this game.
     *
     * @return A set of supported GameMode values
     */
    public abstract Set<GameMode> getSupportedGameModes();


    /**
     * Get the game requirements as a Map.
     * 
     * @return Map containing game requirements
     */
    public Map<String, Object> getRequirements() {
        Map<String, Object> requirements = new HashMap<>();
        requirements.put("min_players", getMinPlayers());
        requirements.put("max_players", getMaxPlayers());
        requirements.put("min_difficulty", getMinDifficulty());
        requirements.put("max_difficulty", getMaxDifficulty());
        requirements.put("estimated_duration_minutes", getEstimatedDurationMinutes());
        return requirements;
    }
    
    /**
     * Get the complete metadata as a Map for JSON responses.
     * 
     * @return Map containing all metadata
     */
    public Map<String, Object> toMap() {
        Map<String, Object> metadata = new HashMap<>();

        // Basic info
        metadata.put("name", getGameName());
        metadata.put("version", getGameVersion());
        metadata.put("description", getGameDescription());
        metadata.put("author", getGameAuthor());

        // Game modes and requirements
        metadata.put("supported_game_modes", getSupportedGameModes()
                .stream()
                .map(Enum::name)
                .toList());
        metadata.put("requirements", getRequirements());

        return metadata;
    }
} 