package launcher.features.module_handling.discovery;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.compilation.ModuleCompiler;
import launcher.features.file_paths.PathUtil;
import launcher.features.module_handling.directory_management.ModuleDirectoryManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles module discovery.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Discovering and loading modules from the modules directory</li>
 *   <li>Finding modules by name</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date August 12, 2025
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public class ModuleDiscovery {
    
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
            List<File> validModuleDirectories = ModuleDirectoryManager.getValidModuleDirectories(modulesDirectoryPath);
            
            if (validModuleDirectories.isEmpty()) {
                Logging.info("Module lookup: No valid modules found");
                return null;
            }
            
            // Load modules one by one until we find the selected game
            for (File moduleDir : validModuleDirectories) {
                GameModule module = ModuleCompiler.loadModule(moduleDir);
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
    
    // ==================== PUBLIC METHODS - MODULE PROCESSING ====================
    
    /**
     * Discovers and loads all available game modules from the modules directory.
     * 
     * <p>This method scans the modules directory, validates module directories,
     * and compiles/loads them into GameModule instances.
     * 
     * @return List of discovered game modules, or empty list if error occurs
     */
    public static List<GameModule> getAllModules() {
        try {
            String modulesDirectoryPath = launcher.features.file_paths.PathUtil.getModulesDirectoryPath();
            Logging.info("Scanning for modules in: " + modulesDirectoryPath);
            
            File modulesDir = new File(modulesDirectoryPath);
            if (!modulesDir.exists()) {
                Logging.error("Modules directory does not exist: " + modulesDirectoryPath);
                return new ArrayList<>();
            }
            
            List<File> validModuleDirectories = ModuleDirectoryManager.getValidModuleDirectories(modulesDirectoryPath);
            List<GameModule> discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
            
            // Filter out null modules
            List<GameModule> validModules = new ArrayList<>();
            for (GameModule module : discoveredModules) {
                if (module != null) {
                    validModules.add(module);
                }
            }
            
            Logging.info("Found " + validModules.size() + " game module(s)");
            return validModules;
            
        } catch (Exception moduleDiscoveryError) {
            Logging.error("Error discovering modules: " + moduleDiscoveryError.getMessage());
            return new ArrayList<>();
        }
    }
    
}

