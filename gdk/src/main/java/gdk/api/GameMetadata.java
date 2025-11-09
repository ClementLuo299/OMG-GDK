package gdk.api;

import gdk.infrastructure.GameMode;
import gdk.infrastructure.DifficultyLevel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract base class for game metadata.
 * Each game module extends this class to describe its unique configuration,
 * including supported modes, player counts, and difficulty levels.
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @edited November 9, 2025
 * @since Beta 1.0
 */
public abstract class GameMetadata {

    // ==================== ABSTRACT METHODS ====================

    /** @return The display name of the game */
    public abstract String getGameName();

    /** @return The game version */
    public abstract String getGameVersion();

    /** @return A short description of the game */
    public abstract String getGameDescription();

    /** @return The author or developer of the game */
    public abstract String getGameAuthor();

    /** @return The minimum number of players supported */
    public abstract int getMinPlayers();

    /** @return The maximum number of players supported */
    public abstract int getMaxPlayers();

    /** @return The estimated average duration of a session, in minutes */
    public abstract int getEstimatedDurationMinutes();

    /** @return The set of game modes supported (e.g. SINGLEPLAYER, MULTIPLAYER) */
    public abstract Set<GameMode> getSupportedGameModes();

    /** @return The set of supported difficulty levels (may be empty if not applicable) */
    public abstract Set<DifficultyLevel> getSupportedDifficultyLevels();

    // ==================== CONVENIENCE METHODS ====================

    /**
     * Automatically computes the lowest difficulty level from the supported set.
     * Ignores unranked levels like ADAPTIVE.
     *
     * @return The lowest ranked difficulty, or null if none exist
     */
    public DifficultyLevel getMinDifficulty() {
        return getSupportedDifficultyLevels().stream()
                .filter(DifficultyLevel::isRanked)
                .min(Comparator.comparingInt(DifficultyLevel::getRank))
                .orElse(null);
    }

    /**
     * Automatically computes the highest difficulty level from the supported set.
     * Ignores unranked levels like ADAPTIVE.
     *
     * @return The highest ranked difficulty, or null if none exist
     */
    public DifficultyLevel getMaxDifficulty() {
        return getSupportedDifficultyLevels().stream()
                .filter(DifficultyLevel::isRanked)
                .max(Comparator.comparingInt(DifficultyLevel::getRank))
                .orElse(null);
    }

    // ==================== SERIALIZATION HELPERS ====================

    /**
     * Converts this metadata into a map structure for display or debugging.
     * Ideal for internal tools or UI elements.
     *
     * @return A map representation of the game's metadata
     */
    public Map<String, Object> toMap() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("name", getGameName());
        metadata.put("version", getGameVersion());
        metadata.put("description", getGameDescription());
        metadata.put("author", getGameAuthor());

        metadata.put("min_players", getMinPlayers());
        metadata.put("max_players", getMaxPlayers());
        metadata.put("estimated_duration_minutes", getEstimatedDurationMinutes());

        metadata.put("supported_game_modes", getSupportedGameModes()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toList()));

        metadata.put("supported_difficulty_levels", getSupportedDifficultyLevels()
                .stream()
                .map(DifficultyLevel::getLabel)
                .collect(Collectors.toList()));

        DifficultyLevel min = getMinDifficulty();
        DifficultyLevel max = getMaxDifficulty();
        metadata.put("min_difficulty", min != null ? min.getLabel() : null);
        metadata.put("max_difficulty", max != null ? max.getLabel() : null);

        return metadata;
    }
}
