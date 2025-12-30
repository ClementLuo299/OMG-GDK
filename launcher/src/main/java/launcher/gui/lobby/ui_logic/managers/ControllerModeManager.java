package launcher.gui.lobby.ui_logic.managers;

import gdk.internal.Logging;
import launcher.gui.lobby.ui_logic.ControllerMode;
import launcher.gui.lobby.ui_logic.GDKGameLobbyController;
import launcher.utils.module.ModuleCompiler;

/**
 * Manager for handling controller mode logic.
 * 
 * Manages the operational mode of the controller (AUTO_LAUNCH or NORMAL)
 * and handles mode-specific initialization logic.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class ControllerModeManager {
    
    private ControllerMode mode = ControllerMode.NORMAL;
    
    /**
     * Set the controller mode.
     * 
     * @param mode The mode to set
     */
    public void setMode(ControllerMode mode) {
        this.mode = mode;
        Logging.info("Controller mode set to: " + mode);
    }
    
    /**
     * Get the current controller mode.
     * 
     * @return The current mode
     */
    public ControllerMode getMode() {
        return mode;
    }
    
    /**
     * Check if GUI setup should be skipped.
     * 
     * @return true if in AUTO_LAUNCH mode (GUI should be skipped), false otherwise
     */
    public boolean shouldSkipGuiSetup() {
        return mode == ControllerMode.AUTO_LAUNCH;
    }
    
    /**
     * Handle initialization for AUTO_LAUNCH mode.
     * Sets up the ModuleCompiler with the controller and logs initialization.
     * 
     * @param controller The controller instance to set on ModuleCompiler
     */
    public void handleAutoLaunchInitialization(GDKGameLobbyController controller) {
        if (mode == ControllerMode.AUTO_LAUNCH) {
            Logging.info("⏭Skipping GUI setup for AUTO_LAUNCH mode");
            ModuleCompiler.setUIController(controller);
            Logging.info("GDK Game Picker Controller initialized (AUTO_LAUNCH mode)");
        }
    }
    
    /**
     * Get a formatted string representation of the current mode for logging.
     * 
     * @return A string describing the current mode
     */
    public String getModeString() {
        return mode.toString();
    }
}
