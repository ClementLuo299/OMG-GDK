package launcher.ui_areas.lobby.game_launching;

import gdk.api.GameModule;
import gdk.api.GameMetadata;
import gdk.internal.Logging;
import launcher.features.file_handling.PathUtil;
import launcher.features.module_handling.load_modules.LoadModules;
import launcher.features.module_handling.extract_metadata.ModuleMetadataExtractor;
import launcher.features.module_handling.module_root_scanning.ScanForModuleFolders;
import launcher.features.module_handling.module_source_validation.ModuleSourceValidator;
import launcher.ui_areas.lobby.messaging.MessageManager;
import launcher.ui_areas.lobby.ui_management.StatusLabelManager;
import javafx.application.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Handles UI coordination for module module_finding operations.
 * 
 * <p>This handler is responsible for UI-related operations:
 * <ul>
 *   <li>Reporting discovered modules to users via messages</li>
 *   <li>Updating status labels</li>
 *   <li>Creating UI-friendly messages from module_finding results</li>
 * </ul>
 * 
 * <p>Business logic (module_finding, processing, filtering) is delegated to
 * {@link ModuleDiscovery}.
 * 
 * @author Clement Luo
 * @date December 30, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class ModuleDiscoveryHandler {
    
    // ==================== DEPENDENCIES ====================
    
    // ModuleDiscovery is now a utility class with static methods, no service instance needed
    
    /** Message manager for user feedback. */
    private final MessageManager messageManager;
    
    /** Status label manager for status updates. */
    private final StatusLabelManager statusLabelManager;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new ModuleDiscoveryHandler.
     * 
     * @param messageManager The message manager for user feedback
     * @param statusLabelManager The status label manager for status updates
     */
    public ModuleDiscoveryHandler(MessageManager messageManager,
                                  StatusLabelManager statusLabelManager) {
        this.messageManager = messageManager;
        this.statusLabelManager = statusLabelManager;
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Discovers and loads game modules.
     * 
     * @param availableGameModulesSize Current size of available modules (for status updates)
     * @return List of discovered game modules
     */
    public List<GameModule> discoverModules(int availableGameModulesSize) {
        LoadModules.ModuleLoadResult result = discoverModulesWithFailures(availableGameModulesSize);
        return result.getLoadedModules();
    }
    
    /**
     * Discovers and loads game modules with load_modules failures.
     * 
     * @param availableGameModulesSize Current size of available modules (for status updates)
     * @return ModuleLoadResult containing loaded modules and load_modules failures
     */
    public LoadModules.ModuleLoadResult discoverModulesWithFailures(int availableGameModulesSize) {
        try {
            // Get modules directory path
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            Logging.info("Scanning for modules in: " + modulesDirectoryPath);
            
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
            
            // Load modules
            LoadModules.ModuleLoadResult result = LoadModules.loadModules(validModuleDirectories);
            
            // Log load_modules failures if any
            List<String> failures = result.getCompilationFailures();
            if (!failures.isEmpty()) {
                Logging.warning("Failed to load " + failures.size() + " module(s): " + String.join(", ", failures));
            }
            
            Logging.info("Found " + result.getLoadedModules().size() + " game module(s)");
            return result;
            
        } catch (Exception e) {
            Logging.error("Module module_finding failed: " + e.getMessage(), e);
            Platform.runLater(() -> {
                messageManager.addMessage("Error discovering modules: " + e.getMessage());
                statusLabelManager.updateGameCountStatus(availableGameModulesSize);
            });
            return new LoadModules.ModuleLoadResult(new ArrayList<>(), new ArrayList<>());
        }
    }
    
    /**
     * Processes discovered modules and prepares UI-friendly module_finding result.
     * 
     * <p>This method:
     * <ul>
     *   <li>Extracts metadata from modules</li>
     *   <li>Creates UI messages from metadata</li>
     *   <li>Handles errors gracefully</li>
     * </ul>
     * 
     * @param discoveredGameModules List of discovered game modules
     * @return Result containing module names and UI messages
     */
    public ModuleDiscoveryResult processDiscoveredModules(List<GameModule> discoveredGameModules) {
        // Extract metadata from modules
        List<GameMetadata> metadataList = ModuleMetadataExtractor.getMetadataFromModules(discoveredGameModules);
        
        // Extract module names and create UI messages
        Set<String> moduleNames = new HashSet<>();
        List<String> uiMessages = new ArrayList<>();
        
        for (GameMetadata metadata : metadataList) {
            String gameName = metadata.getGameName();
            moduleNames.add(gameName);
            uiMessages.add("Detected game: " + gameName);
        }
        
        if (!metadataList.isEmpty()) {
            uiMessages.add("Successfully detected " + metadataList.size() + " game(s)");
            Logging.info("Ready to update UI with " + metadataList.size() + " modules");
        } else {
            uiMessages.add("No games detected - check modules directory");
            Logging.warning("No modules were loaded. Check logs above for ui_loading errors.");
        }
        
        return new ModuleDiscoveryResult(moduleNames, uiMessages);
    }
    
    /**
     * Reports discovered modules with logging and user messages.
     * 
     * @param discoveredGameModules List of discovered game modules
     * @param previousCount Previous module count (for first-load detection)
     */
    public void reportDiscoveredModules(List<GameModule> discoveredGameModules, int previousCount) {
        for (GameModule gameModule : discoveredGameModules) {
            String gameName = gameModule.getMetadata().getGameName();
            String className = gameModule.getClass().getSimpleName();
            Logging.info("Loaded game module: " + gameName + " (" + className + ")");
            
            // Only show detection messages on first load
            if (previousCount == 0) {
                messageManager.addMessage("Detected game: " + gameName);
            }
        }
    }
    
    /**
     * Collects module names from a collection of game modules.
     * 
     * @param modules Collection of game modules (list or observable list)
     * @return Set of module names
     */
    public Set<String> collectModuleNames(Iterable<GameModule> modules) {
        return StreamSupport.stream(modules.spliterator(), false)
            .filter(module -> module != null)
            .map(module -> module.getMetadata().getGameName())
            .collect(Collectors.toSet());
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Result object containing module module_finding information.
     */
    public static class ModuleDiscoveryResult {
        /** Set of discovered module names. */
        final Set<String> moduleNames;
        
        /** List of UI messages to display. */
        final List<String> uiMessages;
        
        /**
         * Creates a new ModuleDiscoveryResult.
         * 
         * @param moduleNames Set of discovered module names
         * @param uiMessages List of UI messages to display
         */
        public ModuleDiscoveryResult(Set<String> moduleNames, List<String> uiMessages) {
            this.moduleNames = moduleNames;
            this.uiMessages = uiMessages;
        }
        
        /**
         * Gets the set of discovered module names.
         * 
         * @return Set of module names
         */
        public Set<String> getModuleNames() {
            return moduleNames;
        }
        
        /**
         * Gets the list of UI messages to display.
         * 
         * @return List of UI messages
         */
        public List<String> getUiMessages() {
            return uiMessages;
        }
    }
}

