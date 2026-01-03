package launcher.features.module_handling.discovery;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.compilation.ModuleCompiler;
import launcher.features.file_paths.PathUtil;
import launcher.features.module_handling.directory_management.ModuleDirectoryUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles module discovery and validation.
 * 
 * <p>This class has a single responsibility: finding and validating game modules
 * in the modules directory. It checks module structure and required components
 * but does not handle compilation or ui_loading (delegated to ModuleCompiler).
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Discovering modules in the modules directory</li>
 *   <li>Validating module structure and required files</li>
 *   <li>Counting valid modules</li>
 *   <li>Getting lists of valid module directories</li>
 *   <li>Checking compilation status</li>
 *   <li>Diagnosing module detection issues</li>
 *   <li>Finding modules by name</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date August 12, 2025
 * @edited January 2, 2026
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
    public static GameModule findModuleByName(String gameName) {
        if (gameName == null || gameName.trim().isEmpty()) {
            Logging.info("Module lookup: Game name is null or empty");
            return null;
        }
        
        try {
            // Discover valid module directories
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            File modulesDirectory = new File(modulesDirectoryPath);
            List<File> validModuleDirectories = ModuleDirectoryUtil.getValidModuleDirectories(modulesDirectory);
            
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
    public static List<GameModule> discoverAndLoadModules() {
        try {
            String modulesDirectoryPath = launcher.features.file_paths.PathUtil.getModulesDirectoryPath();
            Logging.info("📂 Scanning for modules in: " + modulesDirectoryPath);
            
            File modulesDir = new File(modulesDirectoryPath);
            if (!modulesDir.exists()) {
                Logging.error("❌ Modules directory does not exist: " + modulesDirectoryPath);
                return new ArrayList<>();
            }
            
            List<File> validModuleDirectories = ModuleDirectoryUtil.getValidModuleDirectories(modulesDir);
            List<GameModule> discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
            
            // Filter out null modules
            List<GameModule> validModules = new ArrayList<>();
            for (GameModule module : discoveredModules) {
                if (module != null) {
                    validModules.add(module);
                }
            }
            
            Logging.info("✅ Found " + validModules.size() + " game module(s)");
            return validModules;
            
        } catch (Exception moduleDiscoveryError) {
            Logging.error("❌ Error discovering modules: " + moduleDiscoveryError.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Processes discovered modules and extracts information.
     * 
     * <p>This method:
     * <ul>
     *   <li>Extracts module names and metadata</li>
     *   <li>Logs discovered modules</li>
     *   <li>Handles errors gracefully</li>
     * </ul>
     * 
     * @param discoveredGameModules List of discovered game modules
     * @return Result containing module names and processing information
     */
    public static ModuleDiscoveryResult processDiscoveredModules(List<GameModule> discoveredGameModules) {
        Set<String> moduleNames = new HashSet<>();
        List<ModuleInfo> moduleInfos = new ArrayList<>();
        
        for (GameModule gameModule : discoveredGameModules) {
            if (gameModule == null) {
                Logging.warning("Null game module in discovered list - skipping");
                continue;
            }
            
            try {
                String gameName = gameModule.getMetadata().getGameName();
                String className = gameModule.getClass().getSimpleName();
                moduleNames.add(gameName);
                moduleInfos.add(new ModuleInfo(gameName, className));
                Logging.info("Loaded game module: " + gameName + " (" + className + ")");
            } catch (Exception e) {
                Logging.error("Error getting metadata from game module: " + e.getMessage(), e);
            }
        }
        
        return new ModuleDiscoveryResult(moduleNames, moduleInfos, discoveredGameModules.size());
    }
    
    /**
     * Filters out null modules from the discovered list.
     * 
     * @param discoveredGameModules List of discovered modules (may contain nulls)
     * @return List containing only valid (non-null) modules
     */
    public static List<GameModule> filterValidModules(List<GameModule> discoveredGameModules) {
        List<GameModule> validModules = new ArrayList<>();
        for (GameModule module : discoveredGameModules) {
            if (module != null) {
                validModules.add(module);
            }
        }
        Logging.info("Valid modules to add to UI: " + validModules.size());
        return validModules;
    }
    
    /**
     * Collects module names from a collection of game modules.
     * 
     * @param modules Collection of game modules (list or observable list)
     * @return Set of module names
     */
    public static Set<String> collectModuleNames(Iterable<GameModule> modules) {
        Set<String> moduleNames = new HashSet<>();
        for (GameModule module : modules) {
            if (module != null) {
                moduleNames.add(module.getMetadata().getGameName());
            }
        }
        return moduleNames;
    }
    
    /**
     * Extracts module names from a list of modules.
     * 
     * <p>This method only extracts names if previousCount > 0, indicating this is not
     * the first load. This is used to determine which modules are new vs existing.
     * 
     * @param modules List of game modules
     * @param previousCount Previous module count (used to determine if this is first load)
     * @return Set of module names (empty if previousCount is 0)
     */
    public static Set<String> extractModuleNames(List<GameModule> modules, int previousCount) {
        Set<String> moduleNames = new HashSet<>();
        if (previousCount > 0) {
            for (GameModule module : modules) {
                moduleNames.add(module.getMetadata().getGameName());
            }
        }
        return moduleNames;
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Result object containing module discovery information.
     */
    public static class ModuleDiscoveryResult {
        /** Set of discovered module names. */
        public final Set<String> moduleNames;
        
        /** List of module information. */
        public final List<ModuleInfo> moduleInfos;
        
        /** Total count of discovered modules. */
        public final int totalCount;
        
        /**
         * Creates a new ModuleDiscoveryResult.
         * 
         * @param moduleNames Set of discovered module names
         * @param moduleInfos List of module information
         * @param totalCount Total count of discovered modules
         */
        public ModuleDiscoveryResult(Set<String> moduleNames, List<ModuleInfo> moduleInfos, int totalCount) {
            this.moduleNames = moduleNames;
            this.moduleInfos = moduleInfos;
            this.totalCount = totalCount;
        }
    }
    
    /**
     * Information about a discovered module.
     */
    public static class ModuleInfo {
        /** Name of the game module. */
        public final String gameName;
        
        /** Class name of the game module. */
        public final String className;
        
        /**
         * Creates a new ModuleInfo.
         * 
         * @param gameName Name of the game module
         * @param className Class name of the game module
         */
        public ModuleInfo(String gameName, String className) {
            this.gameName = gameName;
            this.className = className;
        }
    }
}

