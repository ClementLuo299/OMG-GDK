package launcher;

import gdk.internal.Logging;

import launcher.lifecycle.start.StartupProcess;
import launcher.lifecycle.stop.Shutdown;
import launcher.utils.FontLoader;

import javafx.application.Application;
import javafx.stage.Stage;


/**
 * Main JavaFX Application class for the OMG Game Development Kit (GDK).
 * This class serves as the entry point for the GDK launcher application.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited December 20, 2025
 * @since Beta 1.0
 */
public class GDKApplication extends Application {
    
    /**
     * JavaFX application initialization method called before start().
     * Loads the Inter font for use throughout the application.
     */
    @Override
    public void init() {
        // Load Inter font early so it's available for all JavaFX and Swing components
        FontLoader.loadFonts();
    }
    
    /**
     * JavaFX application startup method called by the framework.
     * 
     * This method delegates the startup process to the StartupProcess class.
     * 
     * @param primaryStage The primary stage provided by JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        StartupProcess.start(primaryStage);
    }

    /**
     * JavaFX application shutdown method called by the framework.
     * 
     * This method delegates the shutdown process to the Shutdown class.
     */
    @Override
    public void stop() {
        Shutdown.shutdown();
    }
    
    /**
     * Main entry point for the GDK application.
     * 
     * This method is the standard Java main method that launches the
     * JavaFX application.
     * 
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        
        /**
         * Add shutdown hook for unexpected termination.
         * 
         * This is a safety mechanism to ensure that all resources are properly cleaned up,
         * even if the application is terminated unexpectedly.
         */
        Runtime.getRuntime().addShutdownHook(
            new Thread(
                () -> {
                    Logging.info("Shutdown hook triggered - cleaning up resources");
                    try {
                        Shutdown.forceShutdown();
                    } catch (Exception e) {
                        Logging.error("Error in shutdown hook: " + e.getMessage(), e);
                        System.exit(1);
                    }
                },
                "ShutdownHook"
            )
        );
        
        // Launch the application
        launch(args);
    }
} 
