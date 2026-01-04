package launcher.features.module_handling.on_app_start.helpers.thread_tasks_helpers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.discovery.ModuleDiscovery;

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
     * @param discoveryResult The result from module discovery
     * @return List of successfully loaded GameModule instances
     */
    public static List<GameModule> executeLoading(ModuleDiscovery.DiscoveryResult discoveryResult) {
        // Step 1: Check if there are any modules to load
        if (!discoveryResult.isSuccess() || discoveryResult.getValidModuleDirectories().isEmpty()) {
            Logging.info("No modules to load - skipping loading workflow");
            return new ArrayList<>();
        }
        
        // Step 2: Process each discovered module
        ModuleRuntimeLoader.processModules(discoveryResult.getValidModuleDirectories());
        
        // Step 3: Load the discovered modules into memory
        List<GameModule> discoveredModules = ModuleRuntimeLoader.loadModules(
            discoveryResult.getValidModuleDirectories());
        
        // Step 4: Report the number of successfully loaded modules
        ModuleCountReporter.reportModuleCount(discoveredModules);
        
        return discoveredModules;
    }
    
    /**
     * Initializes module loading.
     */
    public static void initializeModuleLoading() {
        Logging.info("Preparing module discovery...");
    }
    
    /**
     * Finalizes the module loading process.
     */
    public static void finalizeModuleLoading() {
        Logging.info("Finalizing module loading...");
    }
}

