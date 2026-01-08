package launcher.features.module_handling;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.file_handling.file_paths.GetModulesDirectoryPath;
import launcher.features.module_handling.load_modules.LoadModules;
import launcher.features.module_handling.module_root_scanning.ScanForModuleFolders;
import launcher.features.module_handling.module_source_validation.ModuleSourceValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for discovering and loading game modules.
 * Provides core module discovery and loading functionality without UI dependencies.
 * 
 * @author Clement Luo
 * @date January 8, 2026
 * @since Beta 1.0
 */
public final class ModuleDiscoveryAndLoading {
    
    private ModuleDiscoveryAndLoading() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Discovers and loads all game modules.
     * 
     * @return ModuleLoadResult containing loaded modules and loading failures
     */
    public static LoadModules.ModuleLoadResult discoverAndLoadAllModules() {
        try {
            Logging.info("Starting module discovery and loading");
            
            // Get modules directory path
            String modulesDirectoryPath = GetModulesDirectoryPath.getModulesDirectoryPath();
            
            // Scan for module folders
            List<File> moduleDirectories = ScanForModuleFolders.findModuleFolders(modulesDirectoryPath);
            
            if (moduleDirectories.isEmpty()) {
                Logging.info("No module directories found in: " + modulesDirectoryPath);
                return new LoadModules.ModuleLoadResult(new ArrayList<>(), new ArrayList<>());
            }
            
            // Filter to only valid module structures
            List<File> validModuleDirectories = new ArrayList<>();
            for (File folder : moduleDirectories) {
                if (ModuleSourceValidator.isValidModule(folder)) {
                    validModuleDirectories.add(folder);
                }
            }
            
            if (validModuleDirectories.isEmpty()) {
                Logging.info("No valid modules discovered");
                return new LoadModules.ModuleLoadResult(new ArrayList<>(), new ArrayList<>());
            }
            
            Logging.info("Found " + validModuleDirectories.size() + " valid module(s). Loading...");
            
            // Load the discovered modules into memory
            LoadModules.ModuleLoadResult result = LoadModules.loadModules(validModuleDirectories);
            
            // Log results
            List<GameModule> loadedModules = result.getLoadedModules();
            List<String> failures = result.getCompilationFailures();
            
            if (!failures.isEmpty()) {
                Logging.warning("Failed to load " + failures.size() + " module(s): " + String.join(", ", failures));
            }
            
            Logging.info("Module loading completed. Successfully loaded " + loadedModules.size() + " module(s)");
            
            if (loadedModules.isEmpty()) {
                Logging.warning("No modules were loaded! Check module loading status.");
            }
            
            return result;
            
        } catch (Exception e) {
            Logging.error("Critical error during module discovery and loading: " + e.getMessage(), e);
            return new LoadModules.ModuleLoadResult(new ArrayList<>(), new ArrayList<>());
        }
    }
}

