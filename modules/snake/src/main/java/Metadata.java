import gdk.GameMetadata;

public class Metadata {
    
    public static GameMetadata getMetadata() {
        return new GameMetadata(
            "Snake Game",
            "Classic snake game with modern graphics",
            "1.0.0",
            "snake"
        );
    }
} 