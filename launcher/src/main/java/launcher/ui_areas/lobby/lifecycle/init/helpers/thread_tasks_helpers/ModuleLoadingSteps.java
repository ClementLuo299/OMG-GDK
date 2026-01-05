package launcher.ui_areas.lobby.lifecycle.init.helpers.thread_tasks_helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.load_modules.LoadModules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the module loading workflow.
 * Coordinates initialization, module processing, loading, and finalization.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public final class ModuleLoadingSteps {
    
    private ModuleLoadingSteps() {}
    
    /**
     * Orchestrates the complete module loading process.
     * Coordinates initialization, processing, loading, and reporting.
     * 
     * @param validModuleDirectories List of valid module directories to load
     * @return ModuleLoadResult containing loaded modules and loading failures
     */
    public static LoadModules.ModuleLoadResult executeLoading(List<File> validModuleDirectories) {
        // Step 1: Check if there are any modules to load
        if (validModuleDirectories == null || validModuleDirectories.isEmpty()) {
            Logging.info("No modules to load - skipping loading workflow");
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

