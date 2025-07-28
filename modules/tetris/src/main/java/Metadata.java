import gdk.GameMetadata;

public class Metadata {
    
    public static GameMetadata getMetadata() {
        return new GameMetadata(
            "Tetris Blocks",
            "The classic falling blocks puzzle game",
            "1.0.0",
            "tetris"
        );
    }
} 