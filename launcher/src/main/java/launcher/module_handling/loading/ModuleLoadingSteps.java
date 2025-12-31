package launcher.module_handling.loading;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.module_handling.discovery.ModuleDiscoverySteps;
import launcher.module_handling.progress.ModuleLoadingProgressManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the module loading workflow.
 * Coordinates initialization, module processing, loading, and finalization.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class ModuleLoadingSteps {
    
    private ModuleLoadingSteps() {}
    
    /**
     * Orchestrates the complete module loading process.
     * Coordinates initialization, processing, loading, and reporting.
     * 
     * @param progressManager The progress manager for updates
     * @param discoveryResult The result from module discovery
     * @param totalSteps Total number of progress steps
     * @return List of successfully loaded GameModule instances
     */
    public static List<GameModule> executeLoading(ModuleLoadingProgressManager progressManager,
                                                  ModuleDiscoverySteps.DiscoveryResult discoveryResult,
                                                  int totalSteps) {
        // Step 1: Check if there are any modules to load
        if (!discoveryResult.isSuccess() || discoveryResult.getValidModuleDirectories().isEmpty()) {
            Logging.info("No modules to load - skipping loading workflow");
            return new ArrayList<>();
        }
        
        // Step 2: Process each discovered module (update progress for each)
        ModuleRuntimeLoader.processModules(progressManager, discoveryResult.getValidModuleDirectories(), totalSteps);
        
        // Step 3: Load the discovered modules into memory
        List<GameModule> discoveredModules = ModuleRuntimeLoader.loadModules(
            progressManager, discoveryResult.getValidModuleDirectories());
        
        // Step 4: Report the number of successfully loaded modules
        ModuleCountReporter.reportModuleCount(progressManager, discoveredModules);
        
        return discoveredModules;
    }
    
    /**
     * Initializes module loading and checks if modules need to be built.
     * 
     * @param progressManager The progress manager for updates
     */
    public static void initializeModuleLoading(ModuleLoadingProgressManager progressManager) {
        ModuleBuildChecker.checkAndInitialize(progressManager);
    }
    
    /**
     * Finalizes the module loading process.
     * 
     * @param progressManager The progress manager for updates
     */
    public static void finalizeModuleLoading(ModuleLoadingProgressManager progressManager) {
        Logging.info("Finalizing module loading...");
        progressManager.updateProgress("Finalizing module loading");
        launcher.utils.StartupDelayUtil.addDevelopmentDelay("After finalizing module loading");
    }
}

