package launcher.ui_areas.lobby.lifecycle.init.helpers.thread_tasks_helpers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.load_modules.LoadModules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads compiled modules into the runtime.
 * Responsible for loading discovered modules into memory and handling loading errors.
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
     * @return ModuleLoadResult containing loaded modules and loading failures
     */
    public static LoadModules.ModuleLoadResult loadModulesWithFailures(List<File> validModuleDirectories) {
        try {
            // Delegate to loading package for actual loading
            Logging.info("Starting module loading...");
            LoadModules.ModuleLoadResult result = LoadModules.loadModules(validModuleDirectories);
            List<GameModule> discoveredModules = result.getLoadedModules();
            
            // Log loading failures if any
            List<String> failures = result.getCompilationFailures();
            if (!failures.isEmpty()) {
                Logging.warning("Failed to load " + failures.size() + " module(s): " + String.join(", ", failures));
            }
            
            Logging.info("Module loading completed. Successfully loaded " + discoveredModules.size() + " modules");
            
            // Warn if no modules were loaded (might indicate loading issues)
            if (discoveredModules.isEmpty()) {
                Logging.warning("No modules were loaded! Check module loading status.");
            }
            
            return result;
            
        } catch (Exception e) {
            // Handle loading errors gracefully - continue with empty list
            Logging.error("Module loading failed: " + e.getMessage(), e);
            e.printStackTrace();
            return new LoadModules.ModuleLoadResult(new ArrayList<>(), new ArrayList<>());
        }
    }
}

