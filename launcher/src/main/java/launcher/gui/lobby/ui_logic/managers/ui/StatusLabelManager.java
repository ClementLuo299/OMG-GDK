package launcher.gui.lobby.ui_logic.managers.ui;

import gdk.internal.Logging;
import javafx.scene.control.Label;

/**
 * Manages the status label UI component.
 * Handles updating the game count status display.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class StatusLabelManager {
    
    private final Label statusLabel;
    
    /**
     * Create a new StatusLabelManager.
     * 
     * @param statusLabel The status label to update
     */
    public StatusLabelManager(Label statusLabel) {
        this.statusLabel = statusLabel;
    }
    
    /**
     * Update the game count status label.
     * 
     * @param gameCount The number of available games
     */
    public void updateGameCountStatus(int gameCount) {
        if (statusLabel != null) {
            statusLabel.setText("Available Games: " + gameCount);
            Logging.info("UI Status updated: " + gameCount + " games available");
        }
    }
}

