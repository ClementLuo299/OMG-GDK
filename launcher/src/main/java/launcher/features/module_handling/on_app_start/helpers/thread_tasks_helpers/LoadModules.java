package launcher.features.module_handling.on_app_start.helpers.thread_tasks_helpers;

import gdk.internal.Logging;
import launcher.features.file_paths.PathUtil;
import launcher.features.module_handling.module_root_scanning.ScanForModuleFolders;
import launcher.features.module_handling.module_source_validation.ModuleSourceValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the startup workflow phases.
 * Coordinates module ui_loading, load_modules checking, UI updates, and completion.
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
     * Executes the complete module load_modules workflow including initialization,
     * module_finding, load_modules, and finalization.
     */
    public static void loadModules() {
        try {
            Logging.info("Starting module load_modules process");
            
            // Step 1: Check if modules need to be built and initialize the load_modules process
            ModuleLoadingSteps.initializeModuleLoading();
            
            // Step 2: Discover available modules in the modules directory
            List<File> validModuleDirectories = discoverValidModules();
            
            // Step 3: Handle module_finding results
            if (validModuleDirectories.isEmpty()) {
                // No modules found - continue without load_modules anything
                Logging.info("No modules discovered - continuing without modules");
            } else {
                // Modules found - proceed with load_modules
                // Step 4: Load the discovered modules into memory
                launcher.features.module_handling.load_modules.LoadModules.ModuleLoadResult loadResult = ModuleLoadingSteps.executeLoading(validModuleDirectories);
                
                // Store failures for later reporting to UI
                CompilationChecker.storeStartupFailures(loadResult.getCompilationFailures());
            }
            
            // Step 5: Finalize the module load_modules process
            ModuleLoadingSteps.finalizeModuleLoading();
            
            Logging.info("Module load_modules completed");
            
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
     * Handles exceptions during module load_modules.
     * 
     * @param e The exception that occurred
     */
    private static void handleModuleLoadingException(Exception e) {
        // Log the error and continue execution even if module load_modules fails
        Logging.error("Critical error during module load_modules: " + e.getMessage(), e);
        e.printStackTrace();
    }
}

