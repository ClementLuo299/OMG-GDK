package launcher.lifecycle.start.module_loading.loading;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.lifecycle.start.module_loading.progress.ModuleLoadingProgressManager;
import launcher.utils.module.ModuleCompiler;
import launcher.utils.StartupDelayUtil;

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
     * Processes each discovered module and updates progress.
     * 
     * @param progressManager The progress manager for updates
     * @param validModuleDirectories List of valid module directories
     * @param totalSteps Total number of progress steps
     */
    public static void processModules(ModuleLoadingProgressManager progressManager,
                                     List<File> validModuleDirectories,
                                     int totalSteps) {
        // Iterate through each module directory and update progress
        for (File moduleDir : validModuleDirectories) {
            // Reserve last 2 steps for finalization
            if (progressManager.getCurrentStep() >= totalSteps - 2) {
                break;
            }
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
            // Update progress and delegate to ModuleCompiler for actual loading
            Logging.info("Starting module loading...");
            progressManager.updateProgress("Loading compiled modules");
            discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
            Logging.info("Module loading completed. Successfully loaded " + discoveredModules.size() + " modules");
            
            // Warn if no modules were loaded (might indicate compilation issues)
            if (discoveredModules.isEmpty()) {
                Logging.warning("No modules were loaded! Check module compilation status.");
            }
            StartupDelayUtil.addDevelopmentDelay("After loading compiled modules - loaded " + 
                discoveredModules.size() + " modules");
            
        } catch (Exception e) {
            // Handle loading errors gracefully - continue with empty list
            Logging.error("Module loading failed: " + e.getMessage(), e);
            e.printStackTrace();
            progressManager.updateProgress("Module loading failed - continuing with empty list");
            StartupDelayUtil.addDevelopmentDelay("After module loading failure");
        }
        
        return discoveredModules;
    }
}

