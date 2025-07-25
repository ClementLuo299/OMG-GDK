import gdk.GameModule;
import gdk.Logging;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.scene.control.Alert;
import javafx.stage.Stage;


/**
 * Simple Game Development Kit (GDK) Application.
 * Handles all startup and shutdown logic in one place.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited July 25, 2025
 * @since 1.0
 */
public class GDKApplication extends Application {
    
    // ==================== CONSTANTS ====================
    
    public static final String MODULES_DIR = "../modules";
    
    // ==================== DEPENDENCIES ====================
    
    private Stage primaryStage; // The main stage for the GDK application

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try 
        {
            // Initialize logging
            Logging.info("🔄 Initializing GDK");
            
            // Delegate startup to Startup class
            new Startup(primaryStage).start();
            
        } 
        catch (Exception e) 
        {
            // Log the error and show an error dialog
            Logging.error("❌ Failed to start GDK: " + e.getMessage(), e);
            showError("Startup Error", "Failed to start GDK: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        Logging.info("🔄 GDK shutdown completed");
    }

    // ==================== UTILITY METHODS ====================
    
    /**
     * Shows an error dialog
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Main entry point for the GDK application
     */
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            Logging.error("❌ Fatal error: " + e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 