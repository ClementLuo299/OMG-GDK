package launcher;

import javafx.application.Application;
import javafx.stage.Stage;
import gdk.infrastructure.Logging;

import launcher.lifecycle.start.Startup;
import launcher.lifecycle.stop.Shutdown;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Main JavaFX Application class for the OMG Game Development Kit (GDK).
 * This class serves as the entry point for the GDK launcher application.
 * Modules directory path is here.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited August 12, 2025     
 * @since 1.0
 */
public class GDKApplication extends Application {
    
    // ==================== APPLICATION CONSTANTS ====================
    
    /**
     * Path to the modules directory relative to the launcher
     * This is where game modules (JARs and compiled classes) are stored.
     * The resolver makes sure IDE launches (project root) and CLI launches
     * (launcher directory) can both find the modules folder.
     */
    public static final String MODULES_DIRECTORY_PATH = resolveModulesDirectoryPath();
    
    /**
     * Helper accessor so other classes can fetch the already resolved path.
     */
    public static String getModulesDirectoryPath() {
        return MODULES_DIRECTORY_PATH;
    }
    
    /**
     * Resolve the modules directory path for both CLI and IDE runs.
     */
    private static String resolveModulesDirectoryPath() {
        String userDir = System.getProperty("user.dir");
        String[] candidates = {
            "../modules",   // Works when launcher/ is the working directory
            "modules",      // Works when project root is the working directory
            "../../modules" // Fallback for IDEs launching from submodules
        };
        
        for (String candidate : candidates) {
            Path candidatePath = Paths.get(candidate).toAbsolutePath().normalize();
            if (candidatePath.toFile().isDirectory()) {
                Logging.info("Modules directory resolved to " + candidatePath + " (user.dir=" + userDir + ")");
                return candidatePath.toString();
            }
        }
        
        Path fallback = Paths.get("../modules").toAbsolutePath().normalize();
        Logging.warning("Unable to locate modules directory from user.dir=" + userDir + "; defaulting to " + fallback);
        return fallback.toString();
    }
    
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
