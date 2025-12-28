package launcher.gui.lobby.subcontrollers;

import gdk.internal.Logging;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * Subcontroller for the Application Control UI area.
 * 
 * Handles exit button, status label, and settings navigation.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class ApplicationControlController {
    
    // UI Components
    private final Button exitButton;
    private final Button settingsButton;
    // Note: statusLabel, loadingProgressBar, and loadingStatusLabel are managed by UIStateManager and LoadingAnimationManager
    
    // Callbacks
    private Runnable onExit;
    private Runnable onOpenSettings;
    
    /**
     * Create a new ApplicationControlController.
     * 
     * @param exitButton The exit button
     * @param settingsButton The settings button
     * @param statusLabel The status label
     * @param loadingProgressBar The loading progress bar
     * @param loadingStatusLabel The loading status label
     */
    public ApplicationControlController(
            Button exitButton,
            Button settingsButton,
            @SuppressWarnings("unused") Label statusLabel,
            @SuppressWarnings("unused") ProgressBar loadingProgressBar,
            @SuppressWarnings("unused") Label loadingStatusLabel) {
        
        this.exitButton = exitButton;
        this.settingsButton = settingsButton;
        // Note: statusLabel, loadingProgressBar, and loadingStatusLabel are managed by other managers
    }
    
    /**
     * Set callback for when exit is requested.
     * 
     * @param onExit The callback to execute
     */
    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }
    
    /**
     * Set callback for when settings page should be opened.
     * 
     * @param onOpenSettings The callback to execute
     */
    public void setOnOpenSettings(Runnable onOpenSettings) {
        this.onOpenSettings = onOpenSettings;
    }
    
    /**
     * Initialize the application control UI and event handlers.
     */
    public void initialize() {
        // Exit button: Close the entire application
        exitButton.setOnAction(event -> {
            Logging.info("🔒 GDK Game Lobby closing");
            if (onExit != null) {
                onExit.run();
            }
            Platform.exit();
        });
        
        // Settings button: Open settings page
        settingsButton.setOnAction(event -> {
            if (onOpenSettings != null) {
                onOpenSettings.run();
            }
        });
    }
}

