package launcher.ui_areas.lobby.managers.ui;

import javafx.scene.control.Button;

/**
 * Manages the launch button UI component.
 * Handles updating the launch button state and styling.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class LaunchButtonManager {
    
    private final Button launchGameButton;
    
    private static final String ENABLED_STYLE = "-fx-alignment: center; -fx-content-display: text-only; -fx-min-height: 45px; -fx-pref-height: 45px; -fx-max-height: 45px; -fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 10 20; -fx-min-width: 120;";
    private static final String DISABLED_STYLE = "-fx-alignment: center; -fx-content-display: text-only; -fx-min-height: 45px; -fx-pref-height: 45px; -fx-max-height: 45px; -fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: transparent; -fx-cursor: default; -fx-padding: 10 20; -fx-min-width: 120; -fx-opacity: 0.6;";
    
    /**
     * Create a new LaunchButtonManager.
     * 
     * @param launchGameButton The launch button to update
     */
    public LaunchButtonManager(Button launchGameButton) {
        this.launchGameButton = launchGameButton;
    }
    
    /**
     * Update the launch button state based on whether a game is selected.
     * 
     * @param hasSelectedGame Whether a game is currently selected
     */
    public void updateLaunchButtonState(boolean hasSelectedGame) {
        if (launchGameButton != null) {
            launchGameButton.setDisable(!hasSelectedGame);
            launchGameButton.setStyle(hasSelectedGame ? ENABLED_STYLE : DISABLED_STYLE);
        }
    }
}

