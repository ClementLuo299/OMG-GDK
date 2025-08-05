import gdk.GameMetadata;
import java.util.Arrays;
import java.util.List;

/**
 * Metadata for the Puzzle Game module.
 * Provides all the metadata information for the Puzzle Game.
 *
 * @authors Clement Luo
 * @date August 2025
 * @since 1.0
 */
public class Metadata extends GameMetadata {
    
    // ==================== GAME INFORMATION ====================
    
    @Override
    public String getGameName() {
        return "Sliding Puzzle";
    }
    
    @Override
    public String getGameVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getGameDescription() {
        return "Classic sliding puzzle - arrange tiles in order";
    }
    
    @Override
    public String getGameAuthor() {
        return "Clement Luo";
    }
    
    // ==================== GAME MODES ====================
    
    @Override
    public boolean supportsSinglePlayer() {
        return true;
    }
    
    @Override
    public boolean supportsMultiPlayer() {
        return false;
    }
    
    @Override
    public boolean supportsAIOpponent() {
        return false;
    }
    
    @Override
    public boolean supportsTournament() {
        return false;
    }
    
    // ==================== REQUIREMENTS ====================
    
    @Override
    public int getMinPlayers() {
        return 1;
    }
    
    @Override
    public int getMaxPlayers() {
        return 1;
    }
    
    @Override
    public String getMinDifficulty() {
        return "Easy";
    }
    
    @Override
    public String getMaxDifficulty() {
        return "Expert";
    }
    
    @Override
    public int getEstimatedDurationMinutes() {
        return 15;
    }
    
    @Override
    public List<String> getRequiredResources() {
        return Arrays.asList("display", "mouse");
    }
} 