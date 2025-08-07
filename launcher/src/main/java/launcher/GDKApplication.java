package launcher;

import gdk.Logging;

import javafx.application.Application;
import javafx.stage.Stage;

import launcher.utils.DialogUtil;
import launcher.lifecycle.start.Startup;
import launcher.lifecycle.start.PreStartupProgressWindow;
import launcher.gui.GDKViewModel;


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
     * This method is invoked when the JavaFX application is starting up.
     * It initializes the GDK and delegates the main startup process to
     * the Startup class for better organization.
     * 
     * @param primaryStage The primary stage provided by JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        // Show pre-startup progress window immediately
        PreStartupProgressWindow preProgressWindow = new PreStartupProgressWindow();
        preProgressWindow.setTotalSteps(6); // Pre-JavaFX + JavaFX steps
        preProgressWindow.show();
        preProgressWindow.updateProgress(0, "Starting GDK application...");
        
        try {
            // Update progress for JavaFX initialization
            preProgressWindow.updateProgress(1, "Initializing JavaFX components...");
            
            // Keep pre-startup window visible and delegate to Startup class
            // The Startup class will handle the transition
            new Startup(primaryStage, preProgressWindow).start();
        } catch (Exception startupError) {
            Logging.error("❌ Failed to start GDK application: " + startupError.getMessage(), startupError);
            
            // Show error in pre-startup progress window
            preProgressWindow.updateProgress(6, "Startup failed - check error messages");
            
            // Keep progress window visible for a moment to show error
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            preProgressWindow.hide();
            
            DialogUtil.showStartupError("Startup Error", null, "Failed to start GDK application: " + startupError.getMessage());
        }
    }

    /**
     * JavaFX application shutdown method called by the framework.
     * 
     * This method is invoked when the JavaFX application is shutting down.
     * It performs any necessary cleanup and logging.
     */
    @Override
    public void stop() {
        Logging.info("🔄 GDK application shutdown completed");
    }
    
    /**
     * Main entry point for the GDK application.
     * 
     * This method is the standard Java main method that launches the
     * JavaFX application. It includes error handling for fatal application
     * errors that might occur during the launch process.
     * 
     * @param commandLineArguments Command line arguments passed to the application
     */
    public static void main(String[] commandLineArguments) {
        try {
            launch(commandLineArguments);
        } catch (Exception fatalError) {
            Logging.error("❌ Fatal application error: " + fatalError.getMessage(), fatalError);
            DialogUtil.showFatalError("Fatal Application Error", "The application cannot start", "A fatal error occurred: " + fatalError.getMessage());
        }
    }
} 