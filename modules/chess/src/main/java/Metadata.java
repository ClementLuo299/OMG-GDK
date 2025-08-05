import gdk.GameMetadata;
import java.util.Arrays;
import java.util.List;

/**
 * Metadata for the Chess Game module.
 * Provides all the metadata information for the Chess Game.
 *
 * @authors Clement Luo
 * @date August 2025
 * @since 1.0
 */
public class Metadata extends GameMetadata {
    
    // ==================== GAME INFORMATION ====================
    
    @Override
    public String getGameName() {
        return "Chess";
    }
    
    @Override
    public String getGameVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getGameDescription() {
        return "Classic chess - the game of kings";
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
        return "Beginner";
    }
    
    @Override
    public String getMaxDifficulty() {
        return "Master";
    }
    
    @Override
    public int getEstimatedDurationMinutes() {
        return 30;
    }
    
    @Override
    public List<String> getRequiredResources() {
        return Arrays.asList("display", "mouse", "keyboard");
    }
} 