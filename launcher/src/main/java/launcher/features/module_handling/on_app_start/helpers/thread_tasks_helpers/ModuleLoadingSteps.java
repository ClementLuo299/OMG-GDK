package launcher.features.module_handling.on_app_start.helpers.thread_tasks_helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.load_modules.LoadModules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the module ui_loading workflow.
 * Coordinates initialization, module processing, ui_loading, and finalization.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public final class ModuleLoadingSteps {
    
    private ModuleLoadingSteps() {}
    
    /**
     * Orchestrates the complete module load_modules process.
     * Coordinates initialization, processing, load_modules, and reporting.
     * 
     * @param validModuleDirectories List of valid module directories to load
     * @return ModuleLoadResult containing loaded modules and load_modules failures
     */
    public static LoadModules.ModuleLoadResult executeLoading(List<File> validModuleDirectories) {
        // Step 1: Check if there are any modules to load
        if (validModuleDirectories == null || validModuleDirectories.isEmpty()) {
            Logging.info("No modules to load - skipping load_modules workflow");
            return new LoadModules.ModuleLoadResult(new ArrayList<>(), new ArrayList<>());
        }
        
        // Step 2: Process each discovered module
        ModuleRuntimeLoader.processModules(validModuleDirectories);
        
        // Step 3: Load the discovered modules into memory
        LoadModules.ModuleLoadResult loadResult = ModuleRuntimeLoader.loadModulesWithFailures(
            validModuleDirectories);
        
        // Step 4: Report the number of successfully loaded modules
        ModuleCountReporter.reportModuleCount(loadResult.getLoadedModules());
        
        return loadResult;
    }
    
    /**
     * Initializes module load_modules.
     */
    public static void initializeModuleLoading() {
        Logging.info("Preparing module module_finding...");
    }
    
    /**
     * Finalizes the module load_modules process.
     */
    public static void finalizeModuleLoading() {
        Logging.info("Finalizing module load_modules...");
    }
}

