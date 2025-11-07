package gdk.api;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Metadata class for game modules.
 * Contains all the metadata information that every game module must provide.
 * This class is designed to be extended by each game module to provide
 * their specific metadata information.
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @edited August 12, 2025
 * @since 1.0
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
    
    // ==================== GAME MODES ====================
    
    /**
     * Get whether the game supports single player mode.
     * 
     * @return true if single player is supported, false otherwise
     */
    public abstract boolean supportsSinglePlayer();
    
    /**
     * Get whether the game supports multi player mode.
     * 
     * @return true if multi player is supported, false otherwise
     */
    public abstract boolean supportsMultiPlayer();
    
    /**
     * Get whether the game supports AI opponents.
     * 
     * @return true if AI opponents are supported, false otherwise
     */
    public abstract boolean supportsAIOpponent();
    
    /**
     * Get whether the game supports local multiplayer mode.
     * 
     * @return true if local multiplayer is supported, false otherwise
     */
    public abstract boolean supportsLocalMultiPlayer();
    
    /**
     * Get whether the game supports tournament mode.
     * 
     * @return true if tournament mode is supported, false otherwise
     */
    public abstract boolean supportsTournament();
    
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
    
    /**
     * Get the list of required resources for the game.
     * 
     * @return List of required resources
     */
    public abstract List<String> getRequiredResources();
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get the supported game modes as a Map.
     * 
     * @return Map containing supported game modes
     */
    public Map<String, Object> getSupportedGameModes() {
        Map<String, Object> gameModes = new HashMap<>();
        gameModes.put("single_player", supportsSinglePlayer());
        gameModes.put("multi_player", supportsMultiPlayer());
        gameModes.put("local_multiplayer", supportsLocalMultiPlayer());
        gameModes.put("ai_opponent", supportsAIOpponent());
        gameModes.put("tournament", supportsTournament());
        return gameModes;
    }
    
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
        requirements.put("required_resources", getRequiredResources());
        return requirements;
    }
    
    /**
     * Get the complete metadata as a Map for JSON responses.
     * 
     * @return Map containing all metadata
     */
    public Map<String, Object> toMap() {
        Map<String, Object> metadata = new HashMap<>();
        
        // Basic game information
        metadata.put("name", getGameName());
        metadata.put("version", getGameVersion());
        metadata.put("description", getGameDescription());
        metadata.put("author", getGameAuthor());
        
        // Game modes and requirements
        metadata.put("supported_game_modes", getSupportedGameModes());
        metadata.put("requirements", getRequirements());
        
        return metadata;
    }
} 