package launcher.features.module_handling.metadata;

import gdk.api.GameModule;
import gdk.internal.Logging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for extracting and processing metadata from game modules.
 * 
 * <p>This class handles:
 * <ul>
 *   <li>Extracting module names and metadata from GameModule instances</li>
 *   <li>Filtering and validating module lists</li>
 *   <li>Collecting module names from collections</li>
 * </ul>
 * 
 * <p>This class does NOT handle:
 * <ul>
 *   <li>Module discovery or loading (see ModuleDiscovery)</li>
 *   <li>Module compilation (see ModuleCompiler)</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @since Beta 1.0
 */
public final class ModuleMetadataExtractor {
    
    private ModuleMetadataExtractor() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - METADATA EXTRACTION ====================
    
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

