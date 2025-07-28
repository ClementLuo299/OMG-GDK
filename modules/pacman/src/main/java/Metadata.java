import gdk.GameMetadata;

public class Metadata {
    
    public static GameMetadata getMetadata() {
        return new GameMetadata(
            "Pac-Man Adventure",
            "Eat dots and avoid ghosts in this classic maze game",
            "1.0.0",
            "pacman"
        );
    }
} 