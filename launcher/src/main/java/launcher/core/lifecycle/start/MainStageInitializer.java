package launcher.core.lifecycle.start;

import javafx.scene.Scene;
import javafx.stage.Stage;
import gdk.internal.Logging;

/**
 * Initializes the primary application stage with basic properties and event handlers.
 * Handles core stage setup including scene assignment, window properties, and close handlers.
 * 
 * @author Clement Luo
 * @date December 26, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public final class MainStageInitializer {
    
    private MainStageInitializer() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Initializes the primary application stage with basic properties and event handlers.
     * Sets up the scene, window title, size, initial opacity, and close handler.
     * The stage is initially set to transparent (opacity 0.0) to allow smooth transition from startup window.
     * 
     * @param primaryApplicationStage The primary JavaFX stage to initialize
     * @param mainScene The main scene to set on the stage
     */
    public static void initialize(Stage primaryApplicationStage, Scene mainScene) {
        configureBasicProperties(primaryApplicationStage, mainScene);
        configureCloseHandler(primaryApplicationStage);
    }
    
    /**
     * Configures basic stage properties including scene, title, size, and initial opacity.
     * 
     * @param primaryApplicationStage The stage to configure
     * @param mainScene The scene to set on the stage
     */
    private static void configureBasicProperties(Stage primaryApplicationStage, Scene mainScene) {
        primaryApplicationStage.setScene(mainScene);
        primaryApplicationStage.setTitle("OMG Game Development Kit (GDK)");
        primaryApplicationStage.setMinWidth(800);
        primaryApplicationStage.setMinHeight(600);
        primaryApplicationStage.setWidth(1200);
        primaryApplicationStage.setHeight(900);
        primaryApplicationStage.setOpacity(0.0);
    }
    
    /**
     * Configures the close handler for proper application shutdown.
     * When the window is closed, triggers the shutdown process.
     * 
     * @param primaryApplicationStage The stage to configure
     */
    private static void configureCloseHandler(Stage primaryApplicationStage) {
        primaryApplicationStage.setOnCloseRequest(event -> {
            Logging.info("Main GDK window closing - initiating shutdown");
            try {
                // Trigger the shutdown process
                launcher.core.lifecycle.stop.Shutdown.shutdown();
            } catch (Exception e) {
                Logging.error("Error during shutdown: " + e.getMessage(), e);
                // Force exit if shutdown fails
                System.exit(1);
            }
        });
    }
}

