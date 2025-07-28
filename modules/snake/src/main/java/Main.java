// import gdk.GameModule;
// import gdk.GameMetadata;
// import javafx.scene.Scene;
// import javafx.scene.control.Label;
// import javafx.scene.layout.VBox;
// import javafx.stage.Stage;
// import java.util.Map;

// public class Main implements GameModule {
    
//     private final Metadata metadata;
    
//     public Main() {
//         this.metadata = new Metadata();
//     }
    
//     @Override
//     public GameMetadata getMetadata() {
//         return metadata;
//     }
    
//     @Override
//     public Scene launchGame(Stage primaryStage) {
//         System.out.println("🐍 Starting Snake Game...");
        
//         // Create a simple placeholder scene
//         VBox root = new VBox(10);
//         root.getStyleClass().add("game-container");
        
//         Label titleLabel = new Label("🐍 Snake Game");
//         titleLabel.getStyleClass().add("game-title");
        
//         Label descriptionLabel = new Label("Classic snake game with modern graphics");
//         descriptionLabel.getStyleClass().add("game-description");
        
//         Label placeholderLabel = new Label("Game implementation coming soon...");
//         placeholderLabel.getStyleClass().add("placeholder");
        
//         root.getChildren().addAll(titleLabel, descriptionLabel, placeholderLabel);
        
//         Scene scene = new Scene(root, 800, 600);
//         scene.getStylesheets().add(getClass().getResource("/games/snake/css/snake.css").toExternalForm());
        
//         primaryStage.setTitle("Snake Game");
//         primaryStage.setScene(scene);
        
//         return scene;
//     }
// } 