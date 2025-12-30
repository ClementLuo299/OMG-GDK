package launcher.gui.lobby.ui_logic.managers.game_launching;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.ui_logic.managers.messaging.MessageManager;
import launcher.gui.lobby.ui_logic.managers.ui.StatusLabelManager;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles discovery and processing of game modules.
 * 
 * <p>This handler is responsible for:
 * <ul>
 *   <li>Discovering modules via ViewModel</li>
 *   <li>Processing discovered modules (extracting names, logging, creating messages)</li>
 *   <li>Filtering out invalid (null) modules</li>
 *   <li>Reporting discovery results</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date December 30, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class ModuleDiscoveryHandler {
    
    // ==================== DEPENDENCIES ====================
    
    /** ViewModel for business logic operations (module discovery). */
    private final GDKViewModel viewModel;
    
    /** Message manager for user feedback. */
    private final MessageManager messageManager;
    
    /** Status label manager for status updates. */
    private final StatusLabelManager statusLabelManager;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new ModuleDiscoveryHandler.
     * 
     * @param viewModel The ViewModel for business logic operations
     * @param messageManager The message manager for user feedback
     * @param statusLabelManager The status label manager for status updates
     */
    public ModuleDiscoveryHandler(GDKViewModel viewModel,
                                  MessageManager messageManager,
                                  StatusLabelManager statusLabelManager) {
        this.viewModel = viewModel;
        this.messageManager = messageManager;
        this.statusLabelManager = statusLabelManager;
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Discovers and loads game modules using the ViewModel.
     * 
     * @param availableGameModulesSize Current size of available modules (for status updates)
     * @return List of discovered game modules, or null if ViewModel is unavailable
     */
    public List<GameModule> discoverModules(int availableGameModulesSize) {
        if (viewModel != null) {
            return viewModel.discoverAndLoadModules();
        } else {
            Logging.error("ViewModel not available for module discovery");
            Platform.runLater(() -> {
                messageManager.addMessage("Error: ViewModel not available");
                statusLabelManager.updateGameCountStatus(availableGameModulesSize);
            });
            return null;
        }
    }
    
    /**
     * Processes discovered modules and prepares discovery result.
     * 
     * <p>This method:
     * <ul>
     *   <li>Extracts module names and metadata</li>
     *   <li>Logs discovered modules</li>
     *   <li>Creates UI messages for each discovered module</li>
     *   <li>Handles errors gracefully</li>
     * </ul>
     * 
     * @param discoveredGameModules List of discovered game modules
     * @return Result containing module names and UI messages
     */
    public ModuleDiscoveryResult processDiscoveredModules(List<GameModule> discoveredGameModules) {
        Set<String> newlyDiscoveredModuleNames = new HashSet<>();
        List<String> uiMessages = new ArrayList<>();
        
        for (GameModule gameModule : discoveredGameModules) {
            if (gameModule == null) {
                Logging.warning("Null game module in discovered list - skipping");
                continue;
            }
            
            try {
                String gameName = gameModule.getMetadata().getGameName();
                String className = gameModule.getClass().getSimpleName();
                newlyDiscoveredModuleNames.add(gameName);
                Logging.info("Loaded game module: " + gameName + " (" + className + ")");
                uiMessages.add("Detected game: " + gameName);
            } catch (Exception e) {
                Logging.error("Error getting metadata from game module: " + e.getMessage(), e);
            }
        }
        
        if (!discoveredGameModules.isEmpty()) {
            uiMessages.add("Successfully detected " + discoveredGameModules.size() + " game(s)");
            Logging.info("Ready to update UI with " + discoveredGameModules.size() + " modules");
        } else {
            uiMessages.add("No games detected - check modules directory");
            Logging.warning("No modules were loaded. Check logs above for loading errors.");
        }
        
        return new ModuleDiscoveryResult(newlyDiscoveredModuleNames, uiMessages);
    }
    
    /**
     * Filters out null modules from the discovered list.
     * 
     * @param discoveredGameModules List of discovered modules (may contain nulls)
     * @return List containing only valid (non-null) modules
     */
    public List<GameModule> filterValidModules(List<GameModule> discoveredGameModules) {
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
     * @param modules List of game modules
     * @param previousCount Previous module count (used to determine if this is first load)
     * @return Set of module names
     */
    public Set<String> extractModuleNames(List<GameModule> modules, int previousCount) {
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
    }
}

