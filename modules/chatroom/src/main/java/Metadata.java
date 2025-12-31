import gdk.api.GameMetadata;
import gdk.internal.GameMode;
import gdk.internal.DifficultyLevel;

import java.util.EnumSet;
import java.util.Set;

/**
 * Metadata for the Chatroom module.
 * <p>
 * The Chatroom application is a simple multiplayer communication environment
 * built to test networking, player connectivity, and real-time game_messaging
 * within the GDK framework.
 * </p>
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @edited November 9, 2025
 * @since 1.0
 */
public class Metadata extends GameMetadata {

    // ==================== BASIC INFORMATION ====================

    /** {@inheritDoc} */
    @Override
    public String getGameName() {
        return "Chatroom";
    }

    /** {@inheritDoc} */
    @Override
    public String getGameVersion() {
        return "1.0.0";
    }

    /** {@inheritDoc} */
    @Override
    public String getGameDescription() {
        return "A multiplayer chatroom module for testing the GDK framework’s networking layer.";
    }

    /** {@inheritDoc} */
    @Override
    public String getGameAuthor() {
        return "Clement Luo";
    }

    // ==================== PLAYER & SESSION SETTINGS ====================

    /** {@inheritDoc} */
    @Override
    public int getMinPlayers() {
        // Minimum two players required to have a conversation
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxPlayers() {
        // Allow up to 10 concurrent users for testing scalability
        return 10;
    }

    /** {@inheritDoc} */
    @Override
    public int getEstimatedDurationMinutes() {
        // Chatrooms are open-ended; duration is variable
        return 0;
    }

    // ==================== GAME MODES & DIFFICULTY ====================

    /** {@inheritDoc} */
    @Override
    public Set<GameMode> getSupportedGameModes() {
        // The chatroom supports networked multiplayer and local network play
        return EnumSet.of(GameMode.MULTIPLAYER, GameMode.COMPUTER);
    }

    /** {@inheritDoc} */
    @Override
    public Set<DifficultyLevel> getSupportedDifficultyLevels() {
        // Chatrooms have no real difficulty progression, so use EASY as placeholder
        return EnumSet.of(DifficultyLevel.EASY);
    }
}
