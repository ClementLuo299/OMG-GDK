

import gdk.GameMetadata;
import java.util.Arrays;
import java.util.List;

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
    
    @Override
    public boolean supportsSinglePlayer() {
        return false;
    }
    
    @Override
    public boolean supportsMultiPlayer() {
        return true;
    }
    
    @Override
    public boolean supportsLocalMultiPlayer() {
        return true;
    }
    
    @Override
    public boolean supportsAIOpponent() {
        return true;
    }
    
    @Override
    public boolean supportsTournament() {
        return true;
    }
    
    // ==================== REQUIREMENTS ====================
    
    @Override
    public int getMinPlayers() {
        return 2;
    }
    
    @Override
    public int getMaxPlayers() {
        return 2;
    }
    
    @Override
    public String getMinDifficulty() {
        return "Easy";
    }
    
    @Override
    public String getMaxDifficulty() {
        return "Hard";
    }
    
    @Override
    public int getEstimatedDurationMinutes() {
        return 5;
    }
    
    @Override
    public List<String> getRequiredResources() {
        return Arrays.asList("display", "input_device");
    }
} 