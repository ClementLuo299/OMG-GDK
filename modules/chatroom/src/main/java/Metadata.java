

import gdk.GameMetadata;
import java.util.Arrays;
import java.util.List;

/**
 * Metadata for the Chatroom Module.
 * Provides all the metadata information for the Chatroom application.
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @since 1.0
 */
public class Metadata extends GameMetadata {
    
    // ==================== GAME INFORMATION ====================
    
    @Override
    public String getGameName() {
        return "Chatroom";
    }
    
    @Override
    public String getGameVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getGameDescription() {
        return "A multiplayer chatroom application for testing the GDK framework";
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
        return 2;
    }
    
    @Override
    public int getMaxPlayers() {
        return 10;
    }
    
    @Override
    public String getMinDifficulty() {
        return "Easy";
    }
    
    @Override
    public String getMaxDifficulty() {
        return "Easy";
    }
    
    @Override
    public int getEstimatedDurationMinutes() {
        return 0; // Variable duration for chat
    }
    
    @Override
    public List<String> getRequiredResources() {
        return Arrays.asList("display", "input_device", "network");
    }
} 