package launcher;

import gdk.internal.Logging;

import launcher.lifecycle.start.Startup;
import launcher.lifecycle.stop.Shutdown;

import javafx.application.Application;
import javafx.stage.Stage;


/**
 * Main JavaFX Application class for the OMG Game Development Kit (GDK).
 * This class serves as the entry point for the GDK launcher application.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited December 19, 2025
 * @since Beta 1.0
 */
public class GDKApplication extends Application {
    
    /**
     * JavaFX application startup method called by the framework.
     * 
     * This method delegates the startup process to the Startup class.
     * 
     * @param primaryStage The primary stage provided by JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        Startup.start(primaryStage);
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
        // Add shutdown hook for unexpected termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logging.info("🚨 Shutdown hook triggered - cleaning up resources");
            try {
                Shutdown.forceShutdown();
            } catch (Exception e) {
                Logging.error("💥 Error in shutdown hook: " + e.getMessage(), e);
                System.exit(1);
            }
        }, "ShutdownHook"));
        
        launch(args);
    }
} 
