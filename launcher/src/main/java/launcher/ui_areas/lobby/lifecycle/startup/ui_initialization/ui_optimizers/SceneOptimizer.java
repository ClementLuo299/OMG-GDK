package launcher.ui_areas.lobby.lifecycle.startup.ui_initialization.ui_optimizers;

import javafx.scene.Scene;
import javafx.application.Platform;

/**
 * Optimizes JavaFX scene rendering performance.
 * Handles scene caching for better rendering performance.
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 2, 2026
 * @since Beta 1.0
 */
public final class SceneOptimizer {
    
    private SceneOptimizer() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Optimizes the scene's rendering performance with caching.
     * Enables caching for the root node and specific container types.
     * 
     * @param mainLobbyScene The scene to optimize
     */
    public static void optimize(Scene mainLobbyScene) {
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

