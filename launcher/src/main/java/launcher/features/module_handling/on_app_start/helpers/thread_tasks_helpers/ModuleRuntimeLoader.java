package launcher.features.module_handling.on_app_start.helpers.thread_tasks_helpers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.compilation.ModuleCompiler;

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
     * Result container for module loading operations.
     */
    public static class ModuleLoadResult {
        private final List<GameModule> loadedModules;
        private final List<String> compilationFailures;
        
        public ModuleLoadResult(List<GameModule> loadedModules, List<String> compilationFailures) {
            this.loadedModules = loadedModules != null ? loadedModules : new ArrayList<>();
            this.compilationFailures = compilationFailures != null ? compilationFailures : new ArrayList<>();
        }
        
        public List<GameModule> getLoadedModules() {
            return loadedModules;
        }
        
        public List<String> getCompilationFailures() {
            return compilationFailures;
        }
    }
    
    /**
     * Loads discovered modules into memory.
     * 
     * @param validModuleDirectories List of valid module directories to load
     * @return List of successfully loaded GameModule instances
     * @deprecated Use {@link #loadModulesWithFailures(List)} to also get compilation failures
     */
    @Deprecated
    public static List<GameModule> loadModules(List<File> validModuleDirectories) {
        return loadModulesWithFailures(validModuleDirectories).getLoadedModules();
    }
    
    /**
     * Loads discovered modules into memory.
     * 
     * @param validModuleDirectories List of valid module directories to load
     * @return ModuleLoadResult containing loaded modules and compilation failures
     */
    public static ModuleLoadResult loadModulesWithFailures(List<File> validModuleDirectories) {
        try {
            // Delegate to ModuleCompiler for actual loading
            Logging.info("Starting module loading...");
            ModuleCompiler.ModuleLoadResult result = ModuleCompiler.loadModules(validModuleDirectories);
            List<GameModule> discoveredModules = result.getLoadedModules();
            
            // Log compilation failures if any
            List<String> failures = result.getCompilationFailures();
            if (!failures.isEmpty()) {
                Logging.warning("Failed to load " + failures.size() + " module(s): " + String.join(", ", failures));
            }
            
            Logging.info("Module loading completed. Successfully loaded " + discoveredModules.size() + " modules");
            
            // Warn if no modules were loaded (might indicate compilation issues)
            if (discoveredModules.isEmpty()) {
                Logging.warning("No modules were loaded! Check module compilation status.");
            }
            
            return new ModuleLoadResult(discoveredModules, failures);
            
        } catch (Exception e) {
            // Handle loading errors gracefully - continue with empty list
            Logging.error("Module loading failed: " + e.getMessage(), e);
            e.printStackTrace();
            return new ModuleLoadResult(new ArrayList<>(), new ArrayList<>());
        }
    }
}

