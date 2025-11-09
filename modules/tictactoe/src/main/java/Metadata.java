

import gdk.api.GameMetadata;
import gdk.internal.GameMode;
import gdk.internal.DifficultyLevel;
import java.util.EnumSet;
import java.util.Set;

/**
 * Metadata for the TicTacToe Game module.
 * Provides all the metadata information for the TicTacToe Game.
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @since 1.0
 */
public class Metadata extends GameMetadata {
    
    // ==================== GAME INFORMATION ====================
    
    @Override
    public String getGameName() {
        return "Tic Tac Toe";
    }
    
    @Override
    public String getGameVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getGameDescription() {
        return "Classic 3x3 grid game for two players";
    }
    
    @Override
    public String getGameAuthor() {
        return "Clement Luo";
    }
    
    // ==================== GAME MODES ====================
    
    /** {@inheritDoc} */
    @Override
    public Set<GameMode> getSupportedGameModes() {
        // TicTacToe supports multiplayer and computer opponent modes
        return EnumSet.of(GameMode.MULTIPLAYER, GameMode.COMPUTER);
    }
    
    /** {@inheritDoc} */
    @Override
    public Set<DifficultyLevel> getSupportedDifficultyLevels() {
        // TicTacToe supports Easy, Normal, and Hard difficulty levels
        return EnumSet.of(DifficultyLevel.EASY, DifficultyLevel.NORMAL, DifficultyLevel.HARD);
    }
    
    // ==================== REQUIREMENTS ====================
    
    /** {@inheritDoc} */
    @Override
    public int getMinPlayers() {
        return 2;
    }
    
    /** {@inheritDoc} */
    @Override
    public int getMaxPlayers() {
        return 2;
    }
    
    /** {@inheritDoc} */
    @Override
    public int getEstimatedDurationMinutes() {
        return 5;
    }
} 