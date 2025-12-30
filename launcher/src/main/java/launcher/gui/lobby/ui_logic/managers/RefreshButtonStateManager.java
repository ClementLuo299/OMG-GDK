package launcher.gui.lobby.ui_logic.managers;

import javafx.scene.control.Button;

/**
 * Manages the refresh button state during operations.
 * Handles enabling/disabling the refresh button.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class RefreshButtonStateManager {
    
    private final Button refreshButton;
    
    /**
     * Create a new RefreshButtonStateManager.
     * 
     * @param refreshButton The refresh button to manage
     */
    public RefreshButtonStateManager(Button refreshButton) {
        this.refreshButton = refreshButton;
    }
    
    /**
     * Disable the refresh button (e.g., during refresh operation).
     */
    public void disable() {
        if (refreshButton != null) {
            refreshButton.setDisable(true);
        }
    }
    
    /**
     * Enable the refresh button (e.g., after refresh operation completes).
     */
    public void enable() {
        if (refreshButton != null) {
            refreshButton.setDisable(false);
        }
    }
}

