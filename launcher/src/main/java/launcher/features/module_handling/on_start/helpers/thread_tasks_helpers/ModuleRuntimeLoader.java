package launcher.features.module_handling.loading.helpers;

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
     * Loads discovered modules into memory.
     * 
     * @param validModuleDirectories List of valid module directories to load
     * @return List of successfully loaded GameModule instances
     */
    public static List<GameModule> loadModules(List<File> validModuleDirectories) {
        List<GameModule> discoveredModules = new ArrayList<>();
        
        try {
            // Delegate to ModuleCompiler for actual loading
            Logging.info("Starting module loading...");
            discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
            Logging.info("Module loading completed. Successfully loaded " + discoveredModules.size() + " modules");
            
            // Warn if no modules were loaded (might indicate compilation issues)
            if (discoveredModules.isEmpty()) {
                Logging.warning("No modules were loaded! Check module compilation status.");
            }
            
        } catch (Exception e) {
            // Handle loading errors gracefully - continue with empty list
            Logging.error("Module loading failed: " + e.getMessage(), e);
            e.printStackTrace();
        }
        
        return discoveredModules;
    }
}

