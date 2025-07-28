import gdk.GameModule;
import gdk.GameMetadata;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
        System.out.println("🚫 Starting Broken Game...");
        
        // Create a simple placeholder scene
        VBox root = new VBox(10);
        root.getStyleClass().add("game-container");
        
        Label titleLabel = new Label("🚫 Broken Game");
        titleLabel.getStyleClass().add("game-title");
        
        Label descriptionLabel = new Label("This game is missing required methods");
        descriptionLabel.getStyleClass().add("game-description");
        
        Label placeholderLabel = new Label("This module should not be detected!");
        placeholderLabel.getStyleClass().add("placeholder");
        
        root.getChildren().addAll(titleLabel, descriptionLabel, placeholderLabel);
        
        Scene scene = new Scene(root, 800, 600);
        
        primaryStage.setTitle("Broken Game");
        primaryStage.setScene(scene);
        
        return scene;
    }
    
    // Missing: stopGame() method
    // Missing: handleMessage() method
} 