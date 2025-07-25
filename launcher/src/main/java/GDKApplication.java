import gdk.GameModule;
import gdk.Logging;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import javafx.stage.Stage;

/**
 * Main JavaFX Application class for the OMG Game Development Kit (GDK).
 * 
 * This class serves as the entry point for the GDK launcher application.
 * It handles the initial application startup and delegates the main startup
 * logic to the Startup class for better separation of concerns.
 * 
 * Key responsibilities:
 * - Initialize the JavaFX application lifecycle
 * - Handle application startup and shutdown
 * - Display error dialogs for critical failures
 * - Provide constants used throughout the application
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited July 25, 2025
 * @since 1.0
 */
public class GDKApplication extends Application {
    
    // ==================== APPLICATION CONSTANTS ====================
    
    /**
     * Path to the modules directory relative to the launcher
     * This is where game modules (JARs and compiled classes) are stored
     */
    public static final String MODULES_DIRECTORY_PATH = "../modules";
    
    // ==================== INSTANCE VARIABLES ====================
    
    /**
     * The primary JavaFX stage that hosts the main application window
     */
    private Stage primaryApplicationStage;

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
        this.primaryApplicationStage = primaryStage;
        
        try {
            new Startup(primaryApplicationStage).start();
        } catch (Exception startupError) {
            Logging.error("❌ Failed to start GDK application: " + startupError.getMessage(), startupError);
            displayErrorDialog("Startup Error", null, "Failed to start GDK application: " + startupError.getMessage());
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
     * Display an error dialog to the user.
     * 
     * This method creates and shows a modal error dialog with the specified
     * title, header, and content. It's used to inform users about errors
     * that prevent the application from functioning properly.
     * 
     * @param dialogTitle The title of the error dialog
     * @param headerText The header text (can be null)
     * @param contentText The error message to display
     */
    private static void displayErrorDialog(String dialogTitle, String headerText, String contentText) {
        Alert errorDialog = new Alert(Alert.AlertType.ERROR);
        errorDialog.setTitle(dialogTitle);
        errorDialog.setHeaderText(headerText);
        errorDialog.setContentText(contentText);
        errorDialog.showAndWait();
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
            displayErrorDialog("Fatal Application Error", "The application cannot start", "A fatal error occurred: " + fatalError.getMessage());
        }
    }
} 