package launcher.gui.lobby.managers;

import gdk.internal.Logging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import launcher.gui.lobby.GDKGameLobbyController;
import launcher.gui.settings_page.SettingsPageController;
import launcher.utils.gui.DialogUtil;

import java.net.URL;
import java.util.function.Supplier;

/**
 * Manages navigation to and from the settings page.
 * Handles FXML loading, scene switching, and CSS application.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class SettingsNavigationManager {
    
    private Stage mainStage;
    private final GDKGameLobbyController mainController;
    private final Supplier<Stage> stageSupplier;
    
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
    
    /**
     * Get the main stage, initializing it lazily if needed.
     */
    private Stage getMainStage() {
        if (mainStage == null && stageSupplier != null) {
            mainStage = stageSupplier.get();
        }
        return mainStage;
    }
    
    /**
     * Open the settings page.
     */
    public void openSettingsPage() {
        Stage stage = getMainStage();
        if (stage == null) {
            Logging.error("❌ Cannot open settings page: stage not available");
            DialogUtil.showError("Error", "Cannot open settings page: stage not available");
            return;
        }
        
        try {
            Logging.info("⚙️ Transitioning to settings page");
            
            // Load the settings page FXML
            URL fxmlUrl = mainController.getClass().getResource("/settings-page/settings-page.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("Could not find settings-page.fxml resource");
            }
            Logging.info("📁 Found FXML resource at: " + fxmlUrl);
            
            // Load the settings page FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Logging.info("📋 FXMLLoader created successfully");
            
            Parent settingsRoot = loader.load();
            Logging.info("📄 FXML loaded successfully");
            
            // Get the settings controller and set the main controller reference
            SettingsPageController settingsController = loader.getController();
            if (settingsController == null) {
                throw new RuntimeException("Settings controller is null");
            }
            Logging.info("🎮 Settings controller loaded successfully");
            
            settingsController.setMainController(mainController);
            Logging.info("🔗 Main controller reference set");
            
            // Store the current scene for returning later
            Scene currentScene = stage.getScene();
            
            // Create the settings scene
            Scene settingsScene = new Scene(settingsRoot);
            
            // Load the settings page CSS
            try {
                URL cssUrl = mainController.getClass().getResource("/settings-page/settings-page.css");
                if (cssUrl != null) {
                    settingsRoot.getStylesheets().add(cssUrl.toExternalForm());
                    Logging.info("🎨 CSS loaded successfully");
                } else {
                    Logging.warning("⚠️ Could not find settings-page.css resource");
                }
            } catch (Exception cssError) {
                Logging.warning("⚠️ Could not load settings page CSS: " + cssError.getMessage());
            }
            
            // Store the current scene in the settings controller for navigation back
            settingsController.setMainScene(currentScene);
            
            // Change the stage to show settings
            stage.setScene(settingsScene);
            stage.setTitle("GDK Settings");
            
            Logging.info("🔄 Stage transitioned to settings page");
            Logging.info("✅ Settings page transition completed successfully");
            
        } catch (Exception e) {
            Logging.error("❌ Error transitioning to settings page: " + e.getMessage(), e);
            DialogUtil.showError("Error", "Failed to open settings page: " + e.getMessage());
        }
    }
}

