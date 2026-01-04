package launcher.ui_areas.lobby.game_launching;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.discovery.ModuleDiscovery;
import launcher.features.module_handling.metadata.ModuleMetadataExtractor;
import launcher.ui_areas.lobby.messaging.MessageManager;
import launcher.ui_areas.lobby.ui_management.StatusLabelManager;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles UI coordination for module discovery operations.
 * 
 * <p>This handler is responsible for UI-related operations:
 * <ul>
 *   <li>Reporting discovered modules to users via messages</li>
 *   <li>Updating status labels</li>
 *   <li>Creating UI-friendly messages from discovery results</li>
 * </ul>
 * 
 * <p>Business logic (discovery, processing, filtering) is delegated to
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
     * Discovers and loads game modules using the business service.
     * 
     * @param availableGameModulesSize Current size of available modules (for status updates)
     * @return List of discovered game modules, or null if service is unavailable
     */
    public List<GameModule> discoverModules(int availableGameModulesSize) {
        ModuleDiscovery.ModuleLoadResult result = ModuleDiscovery.getAllModulesWithFailures();
        if (result == null) {
            Platform.runLater(() -> {
                messageManager.addMessage("Error: ViewModel not available");
                statusLabelManager.updateGameCountStatus(availableGameModulesSize);
            });
            return null;
        }
        return result.getLoadedModules();
    }
    
    /**
     * Discovers and loads game modules with compilation failures.
     * 
     * @param availableGameModulesSize Current size of available modules (for status updates)
     * @return ModuleLoadResult containing loaded modules and compilation failures, or null if service is unavailable
     */
    public ModuleDiscovery.ModuleLoadResult discoverModulesWithFailures(int availableGameModulesSize) {
        ModuleDiscovery.ModuleLoadResult result = ModuleDiscovery.getAllModulesWithFailures();
        if (result == null) {
            Platform.runLater(() -> {
                messageManager.addMessage("Error: ViewModel not available");
                statusLabelManager.updateGameCountStatus(availableGameModulesSize);
            });
            return null;
        }
        return result;
    }
    
    /**
     * Processes discovered modules and prepares UI-friendly discovery result.
     * 
     * <p>This method:
     * <ul>
     *   <li>Delegates processing to business service</li>
     *   <li>Creates UI messages from business results</li>
     *   <li>Handles errors gracefully</li>
     * </ul>
     * 
     * @param discoveredGameModules List of discovered game modules
     * @return Result containing module names and UI messages
     */
    public ModuleDiscoveryResult processDiscoveredModules(List<GameModule> discoveredGameModules) {
        // Process using ModuleMetadataExtractor
        ModuleMetadataExtractor.ModuleDiscoveryResult businessResult = 
            ModuleMetadataExtractor.processDiscoveredModules(discoveredGameModules);
        
        // Create UI messages from business results
        List<String> uiMessages = new ArrayList<>();
        for (ModuleMetadataExtractor.ModuleInfo info : businessResult.moduleInfos) {
            uiMessages.add("Detected game: " + info.gameName);
        }
        
        if (businessResult.totalCount > 0) {
            uiMessages.add("Successfully detected " + businessResult.totalCount + " game(s)");
            Logging.info("Ready to update UI with " + businessResult.totalCount + " modules");
        } else {
            uiMessages.add("No games detected - check modules directory");
            Logging.warning("No modules were loaded. Check logs above for ui_loading errors.");
        }
        
        return new ModuleDiscoveryResult(businessResult.moduleNames, uiMessages);
    }
    
    /**
     * Filters out null modules from the discovered list.
     * Delegates to business service.
     * 
     * @param discoveredGameModules List of discovered modules (may contain nulls)
     * @return List containing only valid (non-null) modules
     */
    public List<GameModule> filterValidModules(List<GameModule> discoveredGameModules) {
        return ModuleMetadataExtractor.filterValidModules(discoveredGameModules);
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
     * Delegates to business service.
     * 
     * @param modules Collection of game modules (list or observable list)
     * @return Set of module names
     */
    public Set<String> collectModuleNames(Iterable<GameModule> modules) {
        return ModuleMetadataExtractor.collectModuleNames(modules);
    }
    
    /**
     * Extracts module names from a list of modules.
     * Delegates to business service.
     * 
     * @param modules List of game modules
     * @param previousCount Previous module count (used to determine if this is first load)
     * @return Set of module names
     */
    public Set<String> extractModuleNames(List<GameModule> modules, int previousCount) {
        return ModuleMetadataExtractor.extractModuleNames(modules, previousCount);
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Result object containing module discovery information.
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

