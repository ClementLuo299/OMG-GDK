package launcher.ui_areas.lobby.lifecycle;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

/**
 * Optimizes JavaFX performance for scenes and stages.
 * Handles resize optimizations and scene caching for better rendering performance.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public final class LobbyOptimizer {
    
    private LobbyOptimizer() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Applies all performance optimizations for the stage and scene.
     * Configures resize listeners and scene caching for better rendering performance.
     * 
     * @param primaryApplicationStage The stage to optimize
     * @param mainLobbyScene The scene to optimize
     */
    public static void optimize(Stage primaryApplicationStage, Scene mainLobbyScene) {
        configureResizeOptimization(primaryApplicationStage, mainLobbyScene);
        configureSceneOptimization(mainLobbyScene);
    }
    
    /**
     * Configures resize listeners with performance optimizations.
     * Applies caching and performance classes during window resizing.
     * 
     * @param primaryApplicationStage The stage to configure
     * @param mainLobbyScene The scene to optimize
     */
    private static void configureResizeOptimization(Stage primaryApplicationStage, Scene mainLobbyScene) {
        // Width resize optimization
        primaryApplicationStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            optimizeDuringResize(mainLobbyScene);
        });
        
        // Height resize optimization
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
        
        // Use JavaFX's built-in resize optimization
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
    
    /**
     * Optimizes the scene's rendering performance with caching.
     * Enables caching for the root node and specific container types.
     * 
     * @param mainLobbyScene The scene to optimize
     */
    private static void configureSceneOptimization(Scene mainLobbyScene) {
        Platform.runLater(() -> {
            // Enable caching for the main container
            mainLobbyScene.getRoot().setCache(true);
            mainLobbyScene.getRoot().setCacheHint(javafx.scene.CacheHint.SPEED);
            
            // Optimize specific containers that might cause slowdown
            if (mainLobbyScene.getRoot() instanceof javafx.scene.layout.VBox) {
                javafx.scene.layout.VBox rootVBox = (javafx.scene.layout.VBox) mainLobbyScene.getRoot();
                rootVBox.setCache(true);
                rootVBox.setCacheHint(javafx.scene.CacheHint.SPEED);
            }
        });
    }
}

