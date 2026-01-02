package launcher.features.module_handling.loading;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.progress.ModuleLoadingProgressManager;
import launcher.features.development_features.StartupDelayUtil;

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
     * @param progressManager The progress manager for updates
     * @param validModuleDirectories List of valid module directories
     */
    public static void processModules(ModuleLoadingProgressManager progressManager,
                                     List<File> validModuleDirectories) {
        // Iterate through each module directory
        for (File moduleDir : validModuleDirectories) {
            String moduleName = moduleDir.getName();
            Logging.info("Processing module: " + moduleName);
            progressManager.updateProgress("Processing module: " + moduleName);
            StartupDelayUtil.addDevelopmentDelay("After processing module: " + moduleName);
        }
    }
    
    /**
     * Loads discovered modules into memory.
     * 
     * @param progressManager The progress manager for updates
     * @param validModuleDirectories List of valid module directories to load
     * @return List of successfully loaded GameModule instances
     */
    public static List<GameModule> loadModules(ModuleLoadingProgressManager progressManager,
                                              List<File> validModuleDirectories) {
        List<GameModule> discoveredModules = new ArrayList<>();
        
        try {
            // Update progress and delegate to ModuleCompiler for actual ui_loading
            Logging.info("Starting module ui_loading...");
            progressManager.updateProgress("Loading compiled modules");
            discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
            Logging.info("Module ui_loading completed. Successfully loaded " + discoveredModules.size() + " modules");
            
            // Warn if no modules were loaded (might indicate compilation issues)
            if (discoveredModules.isEmpty()) {
                Logging.warning("No modules were loaded! Check module compilation status.");
            }
            StartupDelayUtil.addDevelopmentDelay("After ui_loading compiled modules - loaded " +
                discoveredModules.size() + " modules");
            
        } catch (Exception e) {
            // Handle ui_loading errors gracefully - continue with empty list
            Logging.error("Module ui_loading failed: " + e.getMessage(), e);
            e.printStackTrace();
            progressManager.updateProgress("Module ui_loading failed - continuing with empty list");
            StartupDelayUtil.addDevelopmentDelay("After module ui_loading failure");
        }
        
        return discoveredModules;
    }
}

