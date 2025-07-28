import gdk.GameMetadata;

public class Metadata {
    
    public static GameMetadata getMetadata() {
        return new GameMetadata(
            "Breakout Bricks",
            "Smash bricks with a bouncing ball and paddle",
            "1.0.0",
            "breakout"
        );
    }
} 