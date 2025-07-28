import gdk.GameModule;
import gdk.GameMetadata;

public class Main implements GameModule {
    
    @Override
    public GameMetadata getMetadata() {
        return new GameMetadata(
            "Pong Classic",
            "The original arcade tennis game",
            "1.0.0",
            "pong"
        );
    }
    
    @Override
    public void startGame() {
        System.out.println("🏓 Starting Pong Classic...");
        // Placeholder for pong game logic
    }
    
    @Override
    public void stopGame() {
        System.out.println("🏓 Stopping Pong Classic...");
        // Placeholder for cleanup logic
    }
    
    @Override
    public void pauseGame() {
        System.out.println("🏓 Pausing Pong Classic...");
        // Placeholder for pause logic
    }
    
    @Override
    public void resumeGame() {
        System.out.println("🏓 Resuming Pong Classic...");
        // Placeholder for resume logic
    }
} 