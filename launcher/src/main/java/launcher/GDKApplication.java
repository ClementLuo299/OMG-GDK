package launcher;

import javafx.application.Application;
import javafx.stage.Stage;

import launcher.lifecycle.start.Startup;
import launcher.lifecycle.stop.Shutdown;


/**
 * Main JavaFX Application class for the OMG Game Development Kit (GDK).
 * This class serves as the entry point for the GDK launcher application.
 * Modules directory path is here.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited August 6, 2025    
 * @since 1.0
 */
public class GDKApplication extends Application {
    
    // ==================== APPLICATION CONSTANTS ====================
    
    /**
     * Path to the modules directory relative to the launcher
     * This is where game modules (JARs and compiled classes) are stored
     */
    public static final String MODULES_DIRECTORY_PATH = "../modules";
    
    // ==================== JAVAFX LIFECYCLE METHODS ====================
    
    /**
     * JavaFX application startup method called by the framework.
     * 
     * This method delegates the startup process to the Startup class.
     * 
     * @param primaryStage The primary stage provided by JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        new Startup(primaryStage).start();
    }

    /**
     * JavaFX application shutdown method called by the framework.
     * 
     * This method delegates the shutdown process to the Shutdown class.
     */
    @Override
    public void stop() {
        new Shutdown().shutdown();
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
        launch(args);
    }
} 