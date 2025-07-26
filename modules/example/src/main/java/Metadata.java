package example;

import gdk.GameMetadata;
import java.util.Arrays;
import java.util.List;

/**
 * Metadata for the Example Game module.
 * Provides all the metadata information for the Example Game.
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @since 1.0
 */
public class Metadata extends GameMetadata {
    
    // ==================== GAME INFORMATION ====================
    
    @Override
    public String getGameName() {
        return "Example Game";
    }
    
    @Override
    public String getGameVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getGameDescription() {
        return "A simple example game for testing the GDK framework";
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
        return false;
    }
    
    // ==================== REQUIREMENTS ====================
    
    @Override
    public int getMinPlayers() {
        return 1;
    }
    
    @Override
    public int getMaxPlayers() {
        return 4;
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
        return 15;
    }
    
    @Override
    public List<String> getRequiredResources() {
        return Arrays.asList("display", "input_device");
    }
} 