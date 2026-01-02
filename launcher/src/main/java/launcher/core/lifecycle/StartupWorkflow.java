package launcher.core.lifecycle;

import gdk.internal.Logging;
import launcher.features.module_handling.discovery.ModuleDiscoverySteps;
import launcher.features.module_handling.loading.ModuleLoadingSteps;
import launcher.features.module_handling.progress.ModuleLoadingProgressManager;
import launcher.ui_areas.startup_window.StartupWindowManager;
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
     * @param windowManager The startup window manager
     */
    public static void executeWorkflow(StartupWindowManager windowManager) {
        // Execute the module ui_loading workflow
        loadModules(windowManager);
        Logging.info("Module ui_loading completed");
        StartupDelayUtil.addDevelopmentDelay("After module ui_loading process completed");
    }
    
    /**
     * Loads game modules.
     * 
     * @param windowManager The startup window manager
     */
    private static void loadModules(StartupWindowManager windowManager) {
        // Create a progress manager (no-op, kept for compatibility with existing code)
        ModuleLoadingProgressManager progressManager = new ModuleLoadingProgressManager(windowManager);
        
        try {
            Logging.info("Starting module ui_loading process");
            
            // Step 1: Check if modules need to be built and initialize the ui_loading process
            ModuleLoadingSteps.initializeModuleLoading(progressManager);
            
            // Step 2: Discover available modules in the modules directory
            progressManager.updateProgress("Discovering modules");
            StartupDelayUtil.addDevelopmentDelay("After 'Discovering modules' message");
            ModuleDiscoverySteps.DiscoveryResult discoveryResult = 
                ModuleDiscoverySteps.discover();
            
            // Step 3: Handle discovery results
            if (!discoveryResult.isSuccess()) {
                // Discovery failed - show error and continue
                progressManager.updateProgress(discoveryResult.getErrorMessage());
                StartupDelayUtil.addDevelopmentDelay("After discovery error");
            } else if (discoveryResult.getValidModuleDirectories().isEmpty()) {
                // No modules found - continue without ui_loading anything
                Logging.info("No modules discovered - continuing without modules");
            } else {
                // Modules found - proceed with ui_loading
                StartupDelayUtil.addDevelopmentDelay("After module discovery completed - found " + 
                    discoveryResult.getValidModuleDirectories().size() + " modules");
                
                // Step 4: Load the discovered modules into memory
                ModuleLoadingSteps.executeLoading(progressManager, discoveryResult);
            }
            
            // Step 5: Finalize the module ui_loading process
            ModuleLoadingSteps.finalizeModuleLoading(progressManager);
            
        } catch (Exception e) {
            handleModuleLoadingException(progressManager, e);
        }
    }
    
    /**
     * Handles exceptions during module ui_loading.
     * 
     * @param progressManager The progress manager
     * @param e The exception that occurred
     */
    private static void handleModuleLoadingException(ModuleLoadingProgressManager progressManager,
                                                    Exception e) {
        // Log the error and continue execution even if module ui_loading fails
        Logging.error("Critical error during module ui_loading: " + e.getMessage(), e);
        e.printStackTrace();
        progressManager.updateProgress("Error during module ui_loading - continuing");
        StartupDelayUtil.addDevelopmentDelay("After 'Error during module ui_loading' message");
    }
}

