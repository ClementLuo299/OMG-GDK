package launcher.gui.lobby.ui_logic.subcontrollers;

import gdk.internal.Logging;
import javafx.application.Platform;
import javafx.scene.control.Button;

/**
 * Subcontroller for the Application Control UI area (top bar).
 * 
 * Handles exit button, refresh button, and settings button in the header.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class TopBarController {
    
    // UI Components
    private final Button exitButton;
    private final Button refreshButton;
    private final Button settingsButton;
    
    // Callbacks
    private Runnable onExit;
    private Runnable onRefresh;
    private Runnable onOpenSettings;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a new TopBarController.
     * 
     * @param exitButton The exit button (top-left)
     * @param refreshButton The refresh button (top-right)
     * @param settingsButton The settings button (top-right)
     */
    public TopBarController(
            Button exitButton,
            Button refreshButton,
            Button settingsButton) {
        
        this.exitButton = exitButton;
        this.refreshButton = refreshButton;
        this.settingsButton = settingsButton;
    }
    
    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize the application control UI and event handlers.
     * Sets up action handlers for all three top bar buttons.
     */
    public void initialize() {
        // Exit button: Close the entire application
        exitButton.setOnAction(event -> {
            Logging.info("GDK Game Lobby closing");
            if (onExit != null) {
                onExit.run();
            }
            Platform.exit();
        });
        
        // Refresh button: Reload the list of available games
        refreshButton.setOnAction(event -> {
            if (onRefresh != null) {
                onRefresh.run();
            }
        });
        
        // Settings button: Open settings page
        settingsButton.setOnAction(event -> {
            if (onOpenSettings != null) {
                onOpenSettings.run();
            }
        });
    }
    
    // ==================== CALLBACK SETTERS ====================
    
    /**
     * Set callback for when exit is requested.
     * Called before Platform.exit() is executed.
     * 
     * @param onExit The callback to execute (e.g., for cleanup operations)
     */
    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }
    
    /**
     * Set callback for when refresh is requested.
     * Typically delegates to GameModuleRefreshManager.handleRefresh().
     * 
     * @param onRefresh The callback to execute (e.g., refresh game modules)
     */
    public void setOnRefresh(Runnable onRefresh) {
        this.onRefresh = onRefresh;
    }
    
    /**
     * Set callback for when settings page should be opened.
     * Typically delegates to SettingsNavigationManager.openSettingsPage().
     * 
     * @param onOpenSettings The callback to execute (e.g., open settings window)
     */
    public void setOnOpenSettings(Runnable onOpenSettings) {
        this.onOpenSettings = onOpenSettings;
    }
}

