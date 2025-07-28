import gdk.GameMetadata;

public class Metadata {
    
    public static GameMetadata getMetadata() {
        return new GameMetadata(
            "Pong Classic",
            "The original arcade tennis game",
            "1.0.0",
            "pong"
        );
    }
} 