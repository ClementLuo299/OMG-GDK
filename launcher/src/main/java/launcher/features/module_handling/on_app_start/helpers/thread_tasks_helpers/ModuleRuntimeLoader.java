package launcher.features.module_handling.on_app_start.helpers.thread_tasks_helpers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.load_modules.LoadModules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads compiled modules into the runtime.
 * Responsible for ui_loading discovered modules into memory and handling ui_loading errors.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class ModuleRuntimeLoader {
    
    private ModuleRuntimeLoader() {}
    
    /**
     * Processes each discovered module.
     * 
     * @param validModuleDirectories List of valid module directories
     */
    public static void processModules(List<File> validModuleDirectories) {
        // Iterate through each module directory
        for (File moduleDir : validModuleDirectories) {
            String moduleName = moduleDir.getName();
            Logging.info("Processing module: " + moduleName);
        }
    }
    
    /**
     * Loads discovered modules into memory.
     * 
     * @param validModuleDirectories List of valid module directories to load
     * @return List of successfully loaded GameModule instances
     * @deprecated Use {@link #loadModulesWithFailures(List)} to also get load_modules failures
     */
    @Deprecated
    public static List<GameModule> loadModules(List<File> validModuleDirectories) {
        return loadModulesWithFailures(validModuleDirectories).getLoadedModules();
    }
    
    /**
     * Loads discovered modules into memory.
     * 
     * @param validModuleDirectories List of valid module directories to load
     * @return ModuleLoadResult containing loaded modules and load_modules failures
     */
    public static LoadModules.ModuleLoadResult loadModulesWithFailures(List<File> validModuleDirectories) {
        try {
            // Delegate to load_modules package for actual load_modules
            Logging.info("Starting module load_modules...");
            LoadModules.ModuleLoadResult result = LoadModules.loadModules(validModuleDirectories);
            List<GameModule> discoveredModules = result.getLoadedModules();
            
            // Log load_modules failures if any
            List<String> failures = result.getCompilationFailures();
            if (!failures.isEmpty()) {
                Logging.warning("Failed to load " + failures.size() + " module(s): " + String.join(", ", failures));
            }
            
            Logging.info("Module load_modules completed. Successfully loaded " + discoveredModules.size() + " modules");
            
            // Warn if no modules were loaded (might indicate load_modules issues)
            if (discoveredModules.isEmpty()) {
                Logging.warning("No modules were loaded! Check module load_modules status.");
            }
            
            return result;
            
        } catch (Exception e) {
            // Handle load_modules errors gracefully - continue with empty list
            Logging.error("Module load_modules failed: " + e.getMessage(), e);
            e.printStackTrace();
            return new LoadModules.ModuleLoadResult(new ArrayList<>(), new ArrayList<>());
        }
    }
}

