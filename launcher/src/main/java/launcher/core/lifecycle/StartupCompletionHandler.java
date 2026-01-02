package launcher.core.lifecycle;

import launcher.ui_areas.startup_window.StartupWindowManager;
import launcher.features.development_features.StartupDelayUtil;

/**
 * Handles startup completion and final progress updates.
 * Manages the "Startup complete" and "Ready!" messages.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class StartupCompletionHandler {
    
    private StartupCompletionHandler() {}
    
    /**
     * Marks the startup process as complete.
     * 
     * @param windowManager The startup window manager
     */
    public static void markStartupComplete(StartupWindowManager windowManager) {
        StartupDelayUtil.addDevelopmentDelay("After startup complete");
    }
}

