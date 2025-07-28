import gdk.GameModule;
import gdk.GameMetadata;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Map;

public class Main implements GameModule {
    
    private final Metadata metadata;
    
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public GameMetadata getMetadata() {
        return metadata;
    }
    
    @Override
    public Scene launchGame(Stage primaryStage) {
        System.out.println("👻 Starting Pac-Man Adventure...");
        
        // Create a simple placeholder scene
        VBox root = new VBox(10);
        root.getStyleClass().add("game-container");
        
        Label titleLabel = new Label("👻 Pac-Man Adventure");
        titleLabel.getStyleClass().add("game-title");
        
        Label descriptionLabel = new Label("Eat dots and avoid ghosts in this classic maze game");
        descriptionLabel.getStyleClass().add("game-description");
        
        Label placeholderLabel = new Label("Game implementation coming soon...");
        placeholderLabel.getStyleClass().add("placeholder");
        
        root.getChildren().addAll(titleLabel, descriptionLabel, placeholderLabel);
        
        Scene scene = new Scene(root, 800, 600);
        
        primaryStage.setTitle("Pac-Man Adventure");
        primaryStage.setScene(scene);
        
        return scene;
    }
    
    @Override
    public void stopGame() {
        System.out.println("🔄 Pac-Man Adventure closing - cleaning up resources");
    }
    
    @Override
    public Map<String, Object> handleMessage(Map<String, Object> message) {
        if (message == null) {
            return null;
        }
        
        String function = (String) message.get("function");
        if ("metadata".equals(function)) {
            System.out.println("📋 Returning metadata for Pac-Man Adventure");
            return metadata.toMap();
        }
        
        return null;
    }
} 