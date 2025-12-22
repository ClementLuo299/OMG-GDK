package launcher.lifecycle.start.module_loading.steps;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.lifecycle.start.module_loading.progress.ModuleLoadingProgressManager;
import launcher.utils.module.ModuleCompiler;
import launcher.utils.StartupDelayUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes the module loading steps.
 * Handles initialization, module processing, loading, and finalization.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited December 21, 2025
 * @since Beta 1.0
 */
public final class ModuleLoadingSteps {
    
    private ModuleLoadingSteps() {}
    
    /**
     * Executes the complete module loading process.
     * 
     * @param progressManager The progress manager for updates
     * @param discoveryResult The result from module discovery
     * @param totalSteps Total number of progress steps
     * @return List of successfully loaded GameModule instances
     */
    public static List<GameModule> executeLoading(ModuleLoadingProgressManager progressManager,
                                                  ModuleDiscoverySteps.DiscoveryResult discoveryResult,
                                                  int totalSteps) {
        if (!discoveryResult.isSuccess() || discoveryResult.getValidModuleDirectories().isEmpty()) {
            Logging.info("No modules to load - skipping loading workflow");
            return new ArrayList<>();
        }
        
        // Process each discovered module
        processDiscoveredModules(progressManager, discoveryResult.getValidModuleDirectories(), totalSteps);
        
        // Load the discovered modules
        List<GameModule> discoveredModules = loadDiscoveredModules(
            progressManager, discoveryResult.getValidModuleDirectories());
        
        // Report module count
        reportModuleCount(progressManager, discoveredModules);
        
        return discoveredModules;
    }
    
    /**
     * Initializes module loading and checks if modules need to be built.
     * 
     * @param progressManager The progress manager
     */
    public static void initializeModuleLoading(ModuleLoadingProgressManager progressManager) {
        progressManager.updateProgressWithDelay("Initializing game modules...", 
            "After initializing game modules");
        
        if (ModuleCompiler.needToBuildModules()) {
            Logging.info("Modules need to be built");
            progressManager.updateProgressWithDelay("Building modules...", 
                "After checking if modules need to be built");
        } else {
            Logging.info("Using existing builds (recent compilation detected)");
            progressManager.updateProgressWithDelay("Using existing builds (recent compilation detected)", 
                "After checking if modules need to be built");
        }
        
        Logging.info("Preparing module discovery...");
        progressManager.updateProgressWithDelay("Preparing module discovery...", 
            "After preparing module discovery");
    }
    
    /**
     * Processes each discovered module and updates progress.
     * 
     * @param progressManager The progress manager
     * @param validModuleDirectories List of valid module directories
     * @param totalSteps Total number of progress steps
     */
    private static void processDiscoveredModules(ModuleLoadingProgressManager progressManager,
                                                List<File> validModuleDirectories,
                                                int totalSteps) {
        for (File moduleDir : validModuleDirectories) {
            if (progressManager.getCurrentStep() >= totalSteps - 2) {
                break; // Reserve last 2 steps for finalization
            }
            String moduleName = moduleDir.getName();
            Logging.info("Processing module: " + moduleName);
            progressManager.updateProgressWithDelay("Processing module: " + moduleName, 
                "After processing module: " + moduleName);
        }
    }
    
    /**
     * Loads discovered modules into memory.
     * 
     * @param progressManager The progress manager
     * @param validModuleDirectories List of valid module directories to load
     * @return List of successfully loaded GameModule instances
     */
    private static List<GameModule> loadDiscoveredModules(ModuleLoadingProgressManager progressManager,
                                                         List<File> validModuleDirectories) {
        List<GameModule> discoveredModules = new ArrayList<>();
        
        try {
            Logging.info("Starting module loading...");
            progressManager.updateProgress("Loading compiled modules...");
            discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
            Logging.info("Module loading completed. Successfully loaded " + discoveredModules.size() + " modules");
            
            if (discoveredModules.isEmpty()) {
                Logging.warning("No modules were loaded! Check module compilation status.");
            }
            StartupDelayUtil.addDevelopmentDelay("After loading compiled modules - loaded " + 
                discoveredModules.size() + " modules");
            
        } catch (Exception e) {
            Logging.error("Module loading failed: " + e.getMessage(), e);
            e.printStackTrace();
            progressManager.updateProgress("Module loading failed - continuing with empty list");
            StartupDelayUtil.addDevelopmentDelay("After module loading failure");
        }
        
        return discoveredModules;
    }
    
    /**
     * Reports the number of modules found.
     * 
     * @param progressManager The progress manager
     * @param discoveredModules List of discovered modules
     */
    private static void reportModuleCount(ModuleLoadingProgressManager progressManager,
                                         List<GameModule> discoveredModules) {
        int moduleCount = discoveredModules.size();
        progressManager.updateProgressWithDelay("Found " + moduleCount + " modules", 
            "After reporting module count");
    }
    
    /**
     * Finalizes the module loading process.
     * 
     * @param progressManager The progress manager
     */
    public static void finalizeModuleLoading(ModuleLoadingProgressManager progressManager) {
        Logging.info("Finalizing module loading...");
        progressManager.updateProgressWithDelay("Finalizing module loading...", 
            "After finalizing module loading");
    }
}

