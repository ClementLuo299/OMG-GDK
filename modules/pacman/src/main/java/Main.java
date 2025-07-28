import gdk.GameModule;
import gdk.GameMetadata;

public class Main implements GameModule {
    
    @Override
    public GameMetadata getMetadata() {
        return new GameMetadata(
            "Pac-Man Adventure",
            "Eat dots and avoid ghosts in this classic maze game",
            "1.0.0",
            "pacman"
        );
    }
    
    @Override
    public void startGame() {
        System.out.println("👻 Starting Pac-Man Adventure...");
        // Placeholder for pac-man game logic
    }
    
    @Override
    public void stopGame() {
        System.out.println("👻 Stopping Pac-Man Adventure...");
        // Placeholder for cleanup logic
    }
    
    @Override
    public void pauseGame() {
        System.out.println("👻 Pausing Pac-Man Adventure...");
        // Placeholder for pause logic
    }
    
    @Override
    public void resumeGame() {
        System.out.println("👻 Resuming Pac-Man Adventure...");
        // Placeholder for resume logic
    }
} 