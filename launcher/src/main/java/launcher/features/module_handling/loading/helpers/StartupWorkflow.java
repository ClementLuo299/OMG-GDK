package launcher.features.module_handling.loading.helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.discovery.helpers.ModuleDiscoverySteps;
import launcher.ui_areas.startup_window.StartupWindow;
import launcher.features.development_features.StartupDelayUtil;

/**
 * Orchestrates the startup workflow phases.
 * Coordinates module ui_loading, compilation checking, UI updates, and completion.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class StartupWorkflow {
    
    private StartupWorkflow() {}
    
    /**
     * Executes the complete startup workflow.
     * 
     * @param windowManager The startup window
     */
    public static void executeWorkflow(StartupWindow windowManager) {
        // Execute the module loading workflow
        loadModules(windowManager);
        Logging.info("Module ui_loading completed");
        StartupDelayUtil.addDevelopmentDelay("After module ui_loading process completed");
    }
    
    /**
     * Loads game modules.
     * 
     * @param windowManager The startup window
     */
    private static void loadModules(StartupWindow windowManager) {
        try {
            Logging.info("Starting module loading process");
            
            // Step 1: Check if modules need to be built and initialize the loading process
            ModuleLoadingSteps.initializeModuleLoading();
            
            // Step 2: Discover available modules in the modules directory
            StartupDelayUtil.addDevelopmentDelay("After 'Discovering modules' message");
            ModuleDiscoverySteps.DiscoveryResult discoveryResult = 
                ModuleDiscoverySteps.discover();
            
            // Step 3: Handle discovery results
            if (!discoveryResult.isSuccess()) {
                // Discovery failed - continue
                StartupDelayUtil.addDevelopmentDelay("After discovery error");
            } else if (discoveryResult.getValidModuleDirectories().isEmpty()) {
                // No modules found - continue without loading anything
                Logging.info("No modules discovered - continuing without modules");
            } else {
                // Modules found - proceed with loading
                StartupDelayUtil.addDevelopmentDelay("After module discovery completed - found " + 
                    discoveryResult.getValidModuleDirectories().size() + " modules");
                
                // Step 4: Load the discovered modules into memory
                ModuleLoadingSteps.executeLoading(discoveryResult);
            }
            
            // Step 5: Finalize the module loading process
            ModuleLoadingSteps.finalizeModuleLoading();
            
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
        StartupDelayUtil.addDevelopmentDelay("After 'Error during module loading' message");
    }
}

