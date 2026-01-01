package launcher.features.module_handling.loading;

import gdk.internal.Logging;
import launcher.features.module_handling.progress.ModuleLoadingProgressManager;
import launcher.features.development_features.StartupDelayUtil;

/**
 * Checks if modules need to be built before ui_loading.
 * Responsible for checking build status and preparing for discovery.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class ModuleBuildChecker {
    
    private ModuleBuildChecker() {}
    
    /**
     * Checks if modules need to be built and initializes the ui_loading process.
     * 
     * @param progressManager The progress manager for updates
     */
    public static void checkAndInitialize(ModuleLoadingProgressManager progressManager) {
        // Update progress to show initialization has started
        progressManager.updateProgress("Initializing game modules");
        StartupDelayUtil.addDevelopmentDelay("After initializing game modules");
        
        // Check if modules need to be compiled before ui_loading
        if (ModuleCompiler.needToBuildModules()) {
            Logging.info("Modules need to be built");
            progressManager.updateProgress("Building modules");
            StartupDelayUtil.addDevelopmentDelay("After checking if modules need to be built");
        } else {
            // Modules are already compiled, use existing builds
            Logging.info("Using existing builds (recent compilation detected)");
            progressManager.updateProgress("Using existing builds (recent compilation detected)");
            StartupDelayUtil.addDevelopmentDelay("After checking if modules need to be built");
        }
        
        // Prepare for module discovery phase
        Logging.info("Preparing module discovery...");
        progressManager.updateProgress("Preparing module discovery");
        StartupDelayUtil.addDevelopmentDelay("After preparing module discovery");
    }
}

