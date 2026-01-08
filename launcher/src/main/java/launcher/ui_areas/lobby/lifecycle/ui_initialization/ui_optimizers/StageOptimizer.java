package launcher.ui_areas.lobby.lifecycle.init.ui_optimizers;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

/**
 * Optimizes JavaFX stage performance.
 * Handles resize optimizations for better rendering performance during window resizing.
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 2, 2026
 * @since Beta 1.0
 */
public final class StageOptimizer {
    
    private StageOptimizer() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Configures resize listeners with performance optimizations.
     * Applies caching and performance classes during window resizing.
     * 
     * @param primaryApplicationStage The stage to configure
     * @param mainLobbyScene The scene to optimize during resize
     */
    public static void optimize(Stage primaryApplicationStage, Scene mainLobbyScene) {
        // Width resize ui_optimizers
        primaryApplicationStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            optimizeDuringResize(mainLobbyScene);
        });
        
        // Height resize ui_optimizers
        primaryApplicationStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            optimizeDuringResize(mainLobbyScene);
        });
    }
    
    /**
     * Applies performance optimizations during window resize operations.
     * Uses caching and CSS classes to improve resize performance.
     * 
     * @param mainLobbyScene The scene to optimize
     */
    private static void optimizeDuringResize(Scene mainLobbyScene) {
        // Add performance class during resize
        mainLobbyScene.getRoot().getStyleClass().add("resize-active");
        
        // Use JavaFX's built-in resize ui_optimizers
        Platform.runLater(() -> {
            // Reduce layout complexity during resize
            mainLobbyScene.getRoot().setCache(true);
            mainLobbyScene.getRoot().setCacheHint(javafx.scene.CacheHint.SPEED);
        });
        
        // Remove after a short delay to re-enable effects
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(150));
        pause.setOnFinished(event -> {
            mainLobbyScene.getRoot().getStyleClass().remove("resize-active");
            
            // Restore normal rendering after resize
            Platform.runLater(() -> {
                mainLobbyScene.getRoot().setCache(false);
            });
        });
        pause.play();
    }
}

