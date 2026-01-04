package launcher.features.module_handling.discovery;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.compilation.ModuleCompiler;
import launcher.features.file_paths.PathUtil;
import launcher.features.module_handling.module_root_scanning.ScanForModuleFolders;
import launcher.features.module_handling.validation.ModuleValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Public API for module discovery operations.
 * 
 * <p>This class handles discovering and loading game modules from the modules directory.
 * It provides methods for:
 * <ul>
 *   <li>Discovering and loading all available modules</li>
 *   <li>Finding modules by name</li>
 *   <li>Discovering module directories (without loading)</li>
 * </ul>
 * 
 * <p>All other classes in this package are internal implementation details.
 * External code should only use this class for module discovery operations.
 * 
 * @author Clement Luo
 * @date August 12, 2025
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleDiscovery {
    
    /**
     * Result container for module discovery operations.
     */
    public static class DiscoveryResult {
        private final List<File> validModuleDirectories;
        private final boolean success;
        private final String errorMessage;
        
        private DiscoveryResult(List<File> validModuleDirectories, boolean success, String errorMessage) {
            this.validModuleDirectories = validModuleDirectories != null ? validModuleDirectories : new ArrayList<>();
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public static DiscoveryResult success(List<File> validModuleDirectories) {
            return new DiscoveryResult(validModuleDirectories, true, null);
        }
        
        public static DiscoveryResult failure(String errorMessage) {
            return new DiscoveryResult(null, false, errorMessage);
        }
        
        public List<File> getValidModuleDirectories() {
            return validModuleDirectories;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
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
            // Get modules directory path
            String modulesDirectoryPath = launcher.features.file_paths.PathUtil.getModulesDirectoryPath();
            Logging.info("Scanning for modules in: " + modulesDirectoryPath);
            
            // Get all module directories (checks access internally)
            List<File> moduleDirectories = ScanForModuleFolders.findModuleFolders(modulesDirectoryPath);
            
            if (moduleDirectories.isEmpty()) {
                Logging.error("No module directories found or directory not accessible: " + modulesDirectoryPath);
                return new ArrayList<>();
            }
            
            // Filter to only valid module structures
            List<File> validModuleDirectories = new ArrayList<>();
            for (File folder : moduleDirectories) {
                if (launcher.features.module_handling.validation.ModuleValidator.isValidModuleStructure(folder)) {
                    validModuleDirectories.add(folder);
                }
            }
            
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
    
    /**
     * Discovers module directories without loading them.
     * 
     * <p>This method scans the modules directory and validates module structures,
     * returning a result containing valid module directories. This is useful when
     * you need to know which modules exist before loading them.
     * 
     * @return Discovery result containing valid module directories or error information
     */
    public static DiscoveryResult discoverModuleDirectories() {
        // Step 1: Get all module directories (checks access internally)
        String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
        List<File> moduleDirectories = ScanForModuleFolders.findModuleFolders(modulesDirectoryPath);
        
        File modulesDirectory = new File(modulesDirectoryPath);
        
        if (moduleDirectories.isEmpty()) {
            // No modules found or directory not accessible - run diagnostics
            if (!modulesDirectory.exists()) {
                return DiscoveryResult.failure("Modules directory does not exist: " + modulesDirectoryPath);
            }
            // Directory exists but no modules found - run diagnostics
            runEmptyDiscoveryDiagnostics(modulesDirectory, modulesDirectoryPath);
            return DiscoveryResult.failure("No module directories found in: " + modulesDirectoryPath);
        }
        
        // Step 2: Validate the found module folders
        Logging.info("Found " + moduleDirectories.size() + " possible module folders - validating...");
        List<File> validModuleDirectories = findModules(modulesDirectory, modulesDirectoryPath, moduleDirectories);
        
        // Step 3: If no modules found, run diagnostics to help identify the issue
        if (validModuleDirectories.isEmpty()) {
            runEmptyDiscoveryDiagnostics(modulesDirectory, modulesDirectoryPath);
        }
        
        // Step 4: Return success result with discovered modules (empty list is still success)
        return DiscoveryResult.success(validModuleDirectories);
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Performs module discovery by validating the provided module directories.
     * 
     * @param modulesDirectory The modules directory (for diagnostics)
     * @param modulesDirectoryPath The path to the modules directory (for diagnostics)
     * @param moduleDirectories List of module directories to validate
     * @return List of valid module directories
     */
    private static List<File> findModules(File modulesDirectory, String modulesDirectoryPath, List<File> moduleDirectories) {
        List<File> validModuleDirectories = new ArrayList<>();
        
        try {
            // Validate each folder to determine if it's a valid module
            Logging.info("Validating " + moduleDirectories.size() + " module folders...");
            for (File moduleFolder : moduleDirectories) {
                try {
                    if (ModuleValidator.isValidModuleStructure(moduleFolder)) {
                        validModuleDirectories.add(moduleFolder);
                        Logging.info("Valid module found: " + moduleFolder.getName());
                    } else {
                        Logging.info("Invalid module structure: " + moduleFolder.getName());
                    }
                } catch (Exception e) {
                    Logging.error("Error validating module " + moduleFolder.getName() + ": " + e.getMessage());
                    // Continue with other modules instead of failing completely
                }
            }
            
            Logging.info("Module discovery completed. Found " + validModuleDirectories.size() + " valid modules");
            
        } catch (Exception e) {
            // If discovery fails (e.g., file system error), log and run diagnostics
            Logging.error("Module discovery failed: " + e.getMessage(), e);
            e.printStackTrace();
            ModuleValidator.diagnoseModuleDetectionIssues(modulesDirectoryPath);
        }
        
        // Return list of valid module directories (empty if none found or on error)
        return validModuleDirectories;
    }
    
    /**
     * Runs diagnostics when no modules are found.
     * 
     * @param modulesDirectory The modules directory
     * @param modulesDirectoryPath The path to the modules directory
     */
    private static void runEmptyDiscoveryDiagnostics(File modulesDirectory, String modulesDirectoryPath) {
        // Run comprehensive diagnostics to help identify why no modules were found
        Logging.warning("No valid modules found - running diagnostics...");
        ModuleValidator.diagnoseModuleDetectionIssues(modulesDirectoryPath);
        
        // Check if modules exist but just need to be compiled
        Logging.info("Checking module compilation status...");
        ModuleCompiler.reportModuleCompilationStatus(modulesDirectory);
    }
    
}

