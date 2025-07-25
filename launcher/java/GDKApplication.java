import gdk.GameModule;
import gdk.Logging;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Simple Game Development Kit (GDK) Application.
 * Coordinates initialization and shutdown using dedicated classes.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited July 24, 2025
 * @since 1.0
 */
public class GDKApplication extends Application {
    
    private Stage primaryStage; // The main stage for the GDK application
    private Scene lobbyScene; // The scene for the GDK lobby
    private GDKViewModel viewModel; // The ViewModel that handles business logic
    private Stop stopper; // The stop class that handles shutdown

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            // Initialize logging
            Logging.info("🔄 Initializing GDK");

            // Initialize GDK using the start class
            Start start = new Start(primaryStage);
            Start.GDKStartResult result = start.start();
            
            if (result.isSuccess()) {
                // Store references
                this.lobbyScene = result.getLobbyScene();
                this.viewModel = result.getViewModel();
                
                // Create stop class
                this.stopper = new Stop(viewModel, primaryStage, lobbyScene);
                
                // Wire up controller with ViewModel
                wireUpController();
                
                // Show the stage
                primaryStage.show();
                
                Logging.info("✅ GDK started successfully");
                
            } else {
                throw new RuntimeException("GDK initialization failed: " + result.getErrorMessage());
            }
            
        } catch (Exception e) {
            Logging.error("❌ Failed to start GDK: " + e.getMessage(), e);
            showError("Startup Error", "Failed to start GDK: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        Logging.info("🔄 GDK shutting down");
        
        if (stopper != null) {
            stopper.stop();
        } else {
            Logging.warning("⚠️ No stop class available for shutdown");
        }
    }

    /**
     * Wires up the controller with the ViewModel
     */
    private void wireUpController() {
        try {
            // Get the controller from the scene
            GDKGameLobbyController controller = getController();
            if (controller != null) {
                controller.setGDKApplication(this);
                controller.setViewModel(viewModel);
                Logging.info("✅ Controller wired up with ViewModel");
            } else {
                Logging.warning("⚠️ Controller not found for wiring");
            }
        } catch (Exception e) {
            Logging.error("❌ Error wiring up controller: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the controller from the current scene
     */
    private GDKGameLobbyController getController() {
        if (lobbyScene != null) {
            Object userData = lobbyScene.getUserData();
            if (userData instanceof GDKGameLobbyController) {
                return (GDKGameLobbyController) userData;
            }
        }
        return null;
    }
    
    /**
     * Launches a game by delegating to the ViewModel.
     * This method is called by the FXML controller.
     */
    public void launchGame(GameModule selectedGame) {
        if (viewModel != null) {
            viewModel.handleLaunchGame(selectedGame);
        } else {
            Logging.error("❌ ViewModel not initialized");
        }
    }
    
    /**
     * Gets the ViewModel
     */
    public GDKViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * Gets the stop class
     */
    public Stop getStopper() {
        return stopper;
    }

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