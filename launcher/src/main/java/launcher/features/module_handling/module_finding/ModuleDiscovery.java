package launcher.features.module_handling.module_finding;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.load_modules.LoadModules;
import launcher.features.file_paths.PathUtil;
import launcher.features.module_handling.module_root_scanning.ScanForModuleFolders;
import java.io.File;
import java.util.List;

/**
 * Public API for module module_finding operations.
 * 
 * <p>This class handles finding game modules by name from the modules directory.
 * 
 * <p>All other classes in this package are internal implementation details.
 * External code should only use this class for module module_finding operations.
 * 
 * @author Clement Luo
 * @date August 12, 2025
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleDiscovery {
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private ModuleDiscovery() {
        throw new AssertionError("ModuleDiscovery should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - MODULE LOOKUP ====================
    
    /**
     * Finds and loads a game module by its game name.
     * 
     * <p>This method loads modules one by one until finding a match, stopping
     * early for efficiency. It uses ModuleCompiler to load each module.
     * 
     * @param gameName The name of the game to find (as returned by getGameName())
     * @return The loaded GameModule instance, or null if not found
     */
    public static GameModule getModuleByName(String gameName) {

        // Check if the game name is null or empty
        if (gameName == null || gameName.trim().isEmpty()) {
            Logging.info("Module lookup: Game name is null or empty");
            return null;
        }
        
        try {
            // Find module directory path
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            List<File> moduleDirectories = ScanForModuleFolders.findModuleFolders(modulesDirectoryPath);
            
            if (moduleDirectories.isEmpty()) {
                Logging.info("Module lookup: No module directories found");
                return null;
            }
            
            // Load modules one by one until we find the selected game
            for (File moduleDir : moduleDirectories) {
                GameModule module = LoadModules.loadModule(moduleDir);
                if (module != null && gameName.equals(module.getMetadata().getGameName())) {
                    Logging.info("Module lookup: Found game module: " + gameName);
                    return module;
                }
            }
            
            Logging.info("Module lookup: Game module not found: " + gameName);
            return null;
            
        } catch (Exception e) {
            Logging.error("Module lookup failed: " + e.getMessage(), e);
            return null;
        }
    }
    
}



