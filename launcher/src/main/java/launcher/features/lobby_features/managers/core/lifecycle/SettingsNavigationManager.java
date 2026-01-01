package launcher.features.lobby_features.managers.core.lifecycle;

import gdk.internal.Logging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.settings_page.SettingsPageController;
import launcher.core.ui.pop_up_dialogs.DialogUtil;

import java.net.URL;
import java.util.function.Supplier;

/**
 * Manages navigation to and from the settings page.
 * Handles FXML ui_loading, scene switching, and CSS application.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class SettingsNavigationManager {
    
    // ==================== STATE ====================
    
    private Stage mainStage;
    private final GDKGameLobbyController mainController;
    private final Supplier<Stage> stageSupplier;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a new SettingsNavigationManager with lazy stage initialization.
     * 
     * @param mainController The main lobby controller
     * @param stageSupplier Supplier to get the stage (called when needed)
     */
    public SettingsNavigationManager(GDKGameLobbyController mainController, Supplier<Stage> stageSupplier) {
        this.mainController = mainController;
        this.stageSupplier = stageSupplier;
    }
    
    // ==================== NAVIGATION ====================
    
    /**
     * Open the settings page.
     * Loads the settings FXML, creates the scene, applies CSS, and transitions the stage.
     * Called when the settings button is clicked.
     */
    public void openSettingsPage() {
        Stage stage = getMainStage();
        if (stage == null) {
            Logging.error("Cannot open settings page: stage not available");
            DialogUtil.showError("Error", "Cannot open settings page: stage not available");
            return;
        }
        
        try {
            Logging.info("Transitioning to settings page");
            
            // ==================== LOAD FXML ====================
            
            URL fxmlUrl = mainController.getClass().getResource("/settings-page/settings-page.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("Could not find settings-page.fxml resource");
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent settingsRoot = loader.load();
            
            // ==================== CONFIGURE CONTROLLER ====================
            
            SettingsPageController settingsController = loader.getController();
            if (settingsController == null) {
                throw new RuntimeException("Settings controller is null");
            }
            
            // Store the current scene for returning later
            Scene currentScene = stage.getScene();
            
            // Set controller references for navigation
            settingsController.setMainController(mainController);
            settingsController.setMainScene(currentScene);
            
            // ==================== CREATE AND CONFIGURE SCENE ====================
            
            Scene settingsScene = new Scene(settingsRoot);
            
            // Load CSS if available
            URL cssUrl = mainController.getClass().getResource("/settings-page/settings-page.css");
            if (cssUrl != null) {
                settingsRoot.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                Logging.warning("Could not find settings-page.css resource");
            }
            
            // ==================== TRANSITION TO SETTINGS ====================
            
            stage.setScene(settingsScene);
            stage.setTitle("GDK Settings");
            
            Logging.info("Settings page transition completed successfully");
            
        } catch (Exception e) {
            Logging.error("Error transitioning to settings page: " + e.getMessage(), e);
            DialogUtil.showError("Error", "Failed to open settings page: " + e.getMessage());
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Get the main stage, initializing it lazily if needed.
     * Uses the stage supplier to get the stage when first accessed.
     * 
     * @return The main stage, or null if not available
     */
    private Stage getMainStage() {
        if (mainStage == null && stageSupplier != null) {
            mainStage = stageSupplier.get();
        }
        return mainStage;
    }
}

