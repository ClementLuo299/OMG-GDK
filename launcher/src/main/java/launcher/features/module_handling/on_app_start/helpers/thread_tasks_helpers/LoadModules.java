package launcher.features.module_handling.on_app_start.helpers.thread_tasks_helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.discovery.ModuleDiscovery;

/**
 * Orchestrates the startup workflow phases.
 * Coordinates module ui_loading, compilation checking, UI updates, and completion.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class LoadModules {
    
    private LoadModules() {}
    
    /**
     * Loads game modules.
     * Executes the complete module loading workflow including initialization,
     * discovery, loading, and finalization.
     */
    public static void loadModules() {
        try {
            Logging.info("Starting module loading process");
            
            // Step 1: Check if modules need to be built and initialize the loading process
            ModuleLoadingSteps.initializeModuleLoading();
            
            // Step 2: Discover available modules in the modules directory
            ModuleDiscovery.DiscoveryResult discoveryResult = 
                ModuleDiscovery.discoverModuleDirectories();
            
            // Step 3: Handle discovery results
            if (!discoveryResult.isSuccess()) {
                // Discovery failed - continue
            } else if (discoveryResult.getValidModuleDirectories().isEmpty()) {
                // No modules found - continue without loading anything
                Logging.info("No modules discovered - continuing without modules");
            } else {
                // Modules found - proceed with loading
                // Step 4: Load the discovered modules into memory
                ModuleRuntimeLoader.ModuleLoadResult loadResult = ModuleLoadingSteps.executeLoading(discoveryResult);
                
                // Store failures for later reporting to UI
                CompilationChecker.storeStartupFailures(loadResult.getCompilationFailures());
            }
            
            // Step 5: Finalize the module loading process
            ModuleLoadingSteps.finalizeModuleLoading();
            
            Logging.info("Module loading completed");
            
        } catch (Exception e) {
            handleModuleLoadingException(e);
        }
    }
    
    /**
     * Handles exceptions during module loading.
     * 
     * @param e The exception that occurred
     */
    private static void handleModuleLoadingException(Exception e) {
        // Log the error and continue execution even if module loading fails
        Logging.error("Critical error during module loading: " + e.getMessage(), e);
        e.printStackTrace();
    }
}

