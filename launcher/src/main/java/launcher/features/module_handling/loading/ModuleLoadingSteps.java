package launcher.features.module_handling.loading;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.development_features.StartupDelayUtil;
import launcher.features.module_handling.discovery.helpers.ModuleDiscoverySteps;
import launcher.features.module_handling.loading.helpers.ModuleBuildChecker;
import launcher.features.module_handling.loading.helpers.ModuleCountReporter;
import launcher.features.module_handling.loading.helpers.ModuleRuntimeLoader;
import launcher.features.module_handling.progress.ModuleLoadingProgressManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the module ui_loading workflow.
 * Coordinates initialization, module processing, ui_loading, and finalization.
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
     * @return List of successfully loaded GameModule instances
     */
    public static List<GameModule> executeLoading(ModuleLoadingProgressManager progressManager,
                                                  ModuleDiscoverySteps.DiscoveryResult discoveryResult) {
        // Step 1: Check if there are any modules to load
        if (!discoveryResult.isSuccess() || discoveryResult.getValidModuleDirectories().isEmpty()) {
            Logging.info("No modules to load - skipping loading workflow");
            return new ArrayList<>();
        }
        
        // Step 2: Process each discovered module
        ModuleRuntimeLoader.processModules(progressManager, discoveryResult.getValidModuleDirectories());
        
        // Step 3: Load the discovered modules into memory
        List<GameModule> discoveredModules = ModuleRuntimeLoader.loadModules(
            progressManager, discoveryResult.getValidModuleDirectories());
        
        // Step 4: Report the number of successfully loaded modules
        ModuleCountReporter.reportModuleCount(progressManager, discoveredModules);
        
        return discoveredModules;
    }
    
    /**
     * Initializes module ui_loading and checks if modules need to be built.
     * 
     * @param progressManager The progress manager for updates
     */
    public static void initializeModuleLoading(ModuleLoadingProgressManager progressManager) {
        ModuleBuildChecker.checkAndInitialize(progressManager);
    }
    
    /**
     * Finalizes the module ui_loading process.
     * 
     * @param progressManager The progress manager for updates
     */
    public static void finalizeModuleLoading(ModuleLoadingProgressManager progressManager) {
        Logging.info("Finalizing module ui_loading...");
        progressManager.updateProgress("Finalizing module ui_loading");
        StartupDelayUtil.addDevelopmentDelay("After finalizing module ui_loading");
    }
}

