package launcher.core.lifecycle.start.module_loading.startup;

import gdk.internal.Logging;
import launcher.core.lifecycle.start.module_loading.discovery.ModuleDiscoverySteps;
import launcher.core.lifecycle.start.module_loading.loading.ModuleLoadingSteps;
import launcher.core.lifecycle.start.module_loading.progress.ModuleLoadingProgressManager;
import launcher.core.lifecycle.start.startup_window.StartupWindowManager;
import launcher.utils.StartupDelayUtil;

/**
 * Orchestrates the startup workflow phases.
 * Coordinates module loading, compilation checking, UI updates, and completion.
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
     * @param totalSteps Total number of progress steps
     * @return The current step number after module loading completes
     */
    public static int executeWorkflow(StartupWindowManager windowManager, int totalSteps) {
        // Execute the module loading workflow and get the step number after completion
        int currentStep = loadModulesWithProgress(windowManager, totalSteps);
        Logging.info("Module loading completed");
        StartupDelayUtil.addDevelopmentDelay("After module loading process completed");
        
        // Return the current step so the caller knows where to continue
        return currentStep;
    }
    
    /**
     * Loads game modules with progress updates.
     * 
     * @param windowManager The startup window manager for progress updates
     * @param totalSteps The total number of steps in the startup process
     * @return The current step number after module loading completes
     */
    private static int loadModulesWithProgress(StartupWindowManager windowManager, int totalSteps) {
        // Start at step 3 because:
        // - Step 0: "Starting GDK application"
        // - Step 1: "Loading user interface"
        // - Step 2: "Starting module loading"
        // - Step 3+: Module loading steps
        // Create a progress manager starting at step 3 (steps 0-2 are used before module loading)
        ModuleLoadingProgressManager progressManager = new ModuleLoadingProgressManager(windowManager, 3, totalSteps);
        
        try {
            Logging.info("Starting module loading process with " + totalSteps + " total steps");
            
            // Step 1: Check if modules need to be built and initialize the loading process
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
                // No modules found - continue without loading anything
                Logging.info("No modules discovered - continuing without modules");
            } else {
                // Modules found - proceed with loading
                StartupDelayUtil.addDevelopmentDelay("After module discovery completed - found " + 
                    discoveryResult.getValidModuleDirectories().size() + " modules");
                
                // Step 4: Load the discovered modules into memory
                ModuleLoadingSteps.executeLoading(progressManager, discoveryResult, totalSteps);
            }
            
            // Step 5: Finalize the module loading process
            ModuleLoadingSteps.finalizeModuleLoading(progressManager);
            
            // Return the current step number after all module loading is complete
            return progressManager.getCurrentStep();
            
        } catch (Exception e) {
            handleModuleLoadingException(progressManager, e);
            // Return current step even on error
            return progressManager.getCurrentStep();
        }
    }
    
    /**
     * Handles exceptions during module loading.
     * 
     * @param progressManager The progress manager
     * @param e The exception that occurred
     */
    private static void handleModuleLoadingException(ModuleLoadingProgressManager progressManager,
                                                    Exception e) {
        // Log the error and update progress to show the error message
        // Continue execution even if module loading fails
        Logging.error("Critical error during module loading: " + e.getMessage(), e);
        e.printStackTrace();
        progressManager.updateProgress("Error during module loading - continuing");
        StartupDelayUtil.addDevelopmentDelay("After 'Error during module loading' message");
    }
}

