package launcher.ui_areas.lobby.lifecycle.init.helpers.thread_tasks_helpers;

import gdk.internal.Logging;
import launcher.features.file_handling.PathUtil;
import launcher.features.module_handling.module_root_scanning.ScanForModuleFolders;
import launcher.features.module_handling.module_source_validation.ModuleSourceValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the startup workflow phases.
 * Coordinates module discovery, loading, UI updates, and completion.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public final class LoadModules {
    
    private LoadModules() {}
    
    /**
     * Loads game modules.
     * Executes the complete module loading workflow including initialization,
     * discovery, loading, and finalization.
     */
    public static void loadModules() {
        try {
            Logging.info("Starting module loading process");
            
            // Step 1: Initialize the loading process
            ModuleLoadingSteps.initializeModuleLoading();
            
            // Step 2: Discover available modules in the modules directory
            List<File> validModuleDirectories = discoverValidModules();
            
            // Step 3: Handle discovery results
            if (validModuleDirectories.isEmpty()) {
                // No modules found - continue without loading anything
                Logging.info("No modules discovered - continuing without modules");
            } else {
                // Modules found - proceed with loading
                // Step 4: Load the discovered modules into memory
                launcher.features.module_handling.load_modules.LoadModules.ModuleLoadResult loadResult = ModuleLoadingSteps.executeLoading(validModuleDirectories);
                
                // Store failures for later reporting to UI
                CompilationChecker.storeStartupFailures(loadResult.getCompilationFailures());
            }
            
            // Step 5: Finalize the module loading process
            ModuleLoadingSteps.finalizeModuleLoading();
            
            Logging.info("Module loading completed");
            
        } catch (Exception e) {
            handleModuleLoadingException(e);
        }
    }
    
    /**
     * Discovers valid module directories.
     * 
     * @return List of valid module directories
     */
    private static List<File> discoverValidModules() {
        try {
            // Get modules directory path
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            
            // Scan for module folders
            List<File> moduleDirectories = ScanForModuleFolders.findModuleFolders(modulesDirectoryPath);
            
            if (moduleDirectories.isEmpty()) {
                Logging.info("No module directories found in: " + modulesDirectoryPath);
                return new ArrayList<>();
            }
            
            // Filter to only valid module structures
            List<File> validModuleDirectories = new ArrayList<>();
            for (File folder : moduleDirectories) {
                if (ModuleSourceValidator.isValidModule(folder)) {
                    validModuleDirectories.add(folder);
                }
            }
            
            Logging.info("Found " + validModuleDirectories.size() + " valid module(s)");
            return validModuleDirectories;
            
        } catch (Exception e) {
            Logging.error("Error discovering modules: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Handles exceptions during module loading.
     * 
     * @param e The exception that occurred
     */
    private static void handleModuleLoadingException(Exception e) {
        // Log the error and continue execution even if module loading fails
        Logging.error("Critical error during module loading: " + e.getMessage(), e);
        e.printStackTrace();
    }
}

