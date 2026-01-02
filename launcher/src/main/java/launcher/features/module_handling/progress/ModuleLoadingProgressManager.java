package launcher.features.module_handling.progress;

import launcher.ui_areas.startup_window.StartupWindowManager;

/**
 * Handles progress updates for the module loading process.
 * Simplified version - no longer tracks steps since we only show a spinner.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited January 2025
 * @since Beta 1.0
 */
public final class ModuleLoadingProgressManager {
    
    private final StartupWindowManager windowManager;
    
    /**
     * Creates a new progress manager.
     * 
     * @param windowManager The startup window manager (kept for compatibility)
     */
    public ModuleLoadingProgressManager(StartupWindowManager windowManager) {
        this.windowManager = windowManager;
    }
    
    /**
     * Updates progress with a message.
     * No-op since we no longer track steps.
     * 
     * @param message The progress message to display
     */
    public void updateProgress(String message) {
        // No-op: we only show a spinner, no progress updates needed
    }
}

