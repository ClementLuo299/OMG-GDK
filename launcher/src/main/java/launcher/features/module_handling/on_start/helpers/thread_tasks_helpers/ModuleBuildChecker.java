package launcher.features.module_handling.loading.helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.compilation.ModuleCompiler;

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
     * Checks if modules need to be built and initializes the loading process.
     */
    public static void checkAndInitialize() {
        // Check if modules need to be compiled before loading
        if (ModuleCompiler.needToBuildModules()) {
            Logging.info("Modules need to be built");
        } else {
            // Modules are already compiled, use existing builds
            Logging.info("Using existing builds (recent compilation detected)");
        }
        
        // Prepare for module discovery phase
        Logging.info("Preparing module discovery...");
    }
}

