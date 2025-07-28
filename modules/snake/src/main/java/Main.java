import gdk.GameModule;
import gdk.GameMetadata;

public class Main implements GameModule {
    
    @Override
    public GameMetadata getMetadata() {
        return new GameMetadata(
            "Snake Game",
            "Classic snake game with modern graphics",
            "1.0.0",
            "snake"
        );
    }
    
    @Override
    public void startGame() {
        System.out.println("🐍 Starting Snake Game...");
        // Placeholder for snake game logic
    }
    
    @Override
    public void stopGame() {
        System.out.println("🐍 Stopping Snake Game...");
        // Placeholder for cleanup logic
    }
    
    @Override
    public void pauseGame() {
        System.out.println("🐍 Pausing Snake Game...");
        // Placeholder for pause logic
    }
    
    @Override
    public void resumeGame() {
        System.out.println("🐍 Resuming Snake Game...");
        // Placeholder for resume logic
    }
} 