package launcher.ui_areas.lobby.lifecycle;

import gdk.internal.Logging;
import launcher.ui_areas.lobby.ControllerMode;
import launcher.ui_areas.lobby.GDKGameLobbyController;

/**
 * Manager for handling controller mode logic.
 * 
 * Manages the operational mode of the controller (AUTO_LAUNCH or NORMAL)
 * and handles mode-specific initialization logic.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class ControllerModeManager {
    
    // ==================== STATE ====================
    
    private ControllerMode mode = ControllerMode.NORMAL;
    
    // ==================== STATE MANAGEMENT ====================
    
    /**
     * Set the controller mode.
     * 
     * @param mode The mode to set (AUTO_LAUNCH or NORMAL)
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
     * Get a formatted string representation of the current mode for logging.
     * 
     * @return A string describing the current mode
     */
    public String getModeString() {
        return mode.toString();
    }
    
    // ==================== MODE OPERATIONS ====================
    
    /**
     * Check if GUI setup should be skipped.
     * Used during initialization to determine if UI components should be created.
     * 
     * @return true if in AUTO_LAUNCH mode (GUI should be skipped), false otherwise
     */
    public boolean shouldSkipGuiSetup() {
        return mode == ControllerMode.AUTO_LAUNCH;
    }
    
    /**
     * Handle initialization for AUTO_LAUNCH mode.
     * Sets up the ModuleCompiler with the controller and logs initialization.
     * Called during controller initialization when in AUTO_LAUNCH mode.
     * 
     * @param controller The controller instance to set on ModuleCompiler
     */
    public void handleAutoLaunchInitialization(GDKGameLobbyController controller) {
        if (mode == ControllerMode.AUTO_LAUNCH) {
            Logging.info("Skipping GUI setup for AUTO_LAUNCH mode");
            Logging.info("GDK Game Picker Controller initialized (AUTO_LAUNCH mode)");
        }
    }
}

