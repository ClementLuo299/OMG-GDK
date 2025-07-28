import gdk.GameMetadata;
import java.util.List;
import java.util.Arrays;

public class Metadata extends GameMetadata {
    
    @Override
    public String getGameName() {
        return "Pac-Man Adventure";
    }
    
    @Override
    public String getGameVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getGameDescription() {
        return "Eat dots and avoid ghosts in this classic maze game";
    }
    
    @Override
    public String getGameAuthor() {
        return "GDK Team";
    }
    
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
        return "Hard";
    }
    
    @Override
    public int getEstimatedDurationMinutes() {
        return 8;
    }
    
    @Override
    public List<String> getRequiredResources() {
        return Arrays.asList("keyboard");
    }
} 