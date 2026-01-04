package launcher.ui_areas.lobby.game_launching;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.discovery.ModuleDiscovery;
import launcher.ui_areas.lobby.messaging.MessageManager;
import launcher.ui_areas.lobby.ui_management.StatusLabelManager;
import launcher.ui_areas.lobby.ui_management.LaunchButtonManager;
import launcher.ui_areas.lobby.ui_management.LoadingAnimationManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages refreshing the list of available game modules in the lobby UI.
 * 
 * <p>This manager handles two types of refresh operations:
 * <ul>
 *   <li><b>Full refresh</b> ({@link #handleRefresh()}): Performs complete module discovery
 *       with compilation checks, typically triggered by user clicking the refresh button.</li>
 *   <li><b>Fast refresh</b> ({@link #refreshAvailableGameModulesFast()}): Skips compilation
 *       checks for faster startup performance.</li>
 * </ul>
 * 
 * <p>Both refresh types update the UI components (ComboBox, status labels, messages)
 * and track changes in available modules to provide user feedback.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class GameModuleRefreshManager {
    
    // ==================== DEPENDENCIES ====================
    
    /** Handler for module discovery operations. */
    private final ModuleDiscoveryHandler moduleDiscoveryHandler;
    
    /** Updater for UI components during refresh. */
    private final ModuleRefreshUIUpdater uiUpdater;
    
    /** Manager for displaying messages to the user. */
    private final MessageManager messageManager;
    
    /** Reporter for module change notifications (additions/removals). */
    private final ModuleChangesReporter moduleChangeReporter;
    
    /** Manager for ui_loading animation display. */
    private final LoadingAnimationManager loadingAnimationManager;
    
    /** Checker for module compilation failures. */
    private final ModuleCompilationChecker moduleCompilationChecker;
    
    // ==================== STATE ====================
    
    /** Previous module count for change detection. */
    private int previousModuleCount = 0;
    
    /** Set of module names that were removed (for tracking purposes). */
    private final Set<String> removedModuleNames = new HashSet<>();
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new GameModuleRefreshManager with all required dependencies.
     * 
     * @param availableGameModules The observable list of available game modules
     * @param gameSelector The game selector ComboBox UI component
     * @param messageManager The message manager for user feedback
     * @param statusLabelManager The status label manager for status updates
     * @param launchButtonManager The launch button manager for button state
     * @param moduleChangeReporter The module change reporter for change notifications
     * @param loadingAnimationManager The ui_loading animation manager
     * @param moduleCompilationChecker The module compilation checker
     */
    public GameModuleRefreshManager(
            ObservableList<GameModule> availableGameModules,
            ComboBox<GameModule> gameSelector,
            MessageManager messageManager,
            StatusLabelManager statusLabelManager,
            LaunchButtonManager launchButtonManager,
            ModuleChangesReporter moduleChangeReporter,
            LoadingAnimationManager loadingAnimationManager,
            ModuleCompilationChecker moduleCompilationChecker) {
        
        this.messageManager = messageManager;
        this.moduleChangeReporter = moduleChangeReporter;
        this.loadingAnimationManager = loadingAnimationManager;
        this.moduleCompilationChecker = moduleCompilationChecker;
        
        // Create handlers
        this.moduleDiscoveryHandler = new ModuleDiscoveryHandler(messageManager, statusLabelManager);
        this.uiUpdater = new ModuleRefreshUIUpdater(availableGameModules, gameSelector, statusLabelManager, launchButtonManager);
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Handles the refresh button click event.
     * 
     * <p>Performs a full refresh operation that includes:
     * <ul>
     *   <li>Module discovery and ui_loading</li>
     *   <li>Compilation failure checks</li>
     *   <li>UI updates (ComboBox, status labels, messages)</li>
     *   <li>Change detection and reporting</li>
     * </ul>
     * 
     * <p>This operation runs in a background helpers to keep the UI responsive.
     */
    public void handleRefresh() {
        // Initialize UI state for refresh operation
        loadingAnimationManager.startAnimation();
        messageManager.setRefreshing(true);
        messageManager.clearMessages();

        // Snapshot current state on JavaFX helpers for safe background use
        List<GameModule> previousModulesSnapshot = new ArrayList<>(uiUpdater.availableGameModules);
        int previousCountSnapshot = previousModuleCount;

        // Clear UI selections immediately to prevent launching stale modules
        uiUpdater.clearUISelections();
        
        // Perform refresh in background helpers to keep UI responsive
        new Thread(() -> {
            try {
                performFullRefresh(previousModulesSnapshot, previousCountSnapshot);
            } catch (Exception e) {
                handleRefreshError(e);
            } finally {
                stopRefreshAnimation();
            }
        }).start();
    }
    
    /**
     * Performs a fast refresh that skips compilation checks for faster startup.
     * 
     * <p>This method is optimized for startup performance and:
     * <ul>
     *   <li>Skips compilation failure checks</li>
     *   <li>Updates UI components with discovered modules</li>
     *   <li>Reports module changes if this is not the first load</li>
     * </ul>
     * 
     * <p>Can be called from any helpers; UI updates are automatically scheduled
     * on the JavaFX application helpers if needed.
     */
    public void refreshAvailableGameModulesFast() {
        try {
            Logging.info("Fast refresh started");
            
            // Collect previous module names for change detection
            Set<String> previousModuleNames = moduleDiscoveryHandler.collectModuleNames(uiUpdater.availableGameModules);
            Logging.info("Previous module count: " + previousModuleNames.size());

            // Discover and load modules using ViewModel
            List<GameModule> discoveredGameModules = moduleDiscoveryHandler.discoverModules(uiUpdater.availableGameModules.size());
            if (discoveredGameModules == null || discoveredGameModules.isEmpty()) {
                uiUpdater.handleNoModulesDiscovered(messageManager);
                return;
            }
            
            Logging.info("Module ui_loading completed. Loaded " + discoveredGameModules.size() + " modules");
            
            // Process discovered modules and prepare UI messages
            ModuleDiscoveryHandler.ModuleDiscoveryResult discoveryResult = 
                moduleDiscoveryHandler.processDiscoveredModules(discoveredGameModules);
            
            // Filter out any null modules
            List<GameModule> validModules = moduleDiscoveryHandler.filterValidModules(discoveredGameModules);
            
            // Update UI on JavaFX helpers
            previousModuleCount = uiUpdater.updateUIForFastRefresh(
                validModules, discoveryResult, messageManager, moduleChangeReporter, previousModuleNames);
            
        } catch (Exception e) {
            handleFastRefreshError(e);
        }
    }
    
    // ==================== PRIVATE HELPER METHODS - FULL REFRESH ====================
    
    /**
     * Performs the core full refresh logic in a background helpers.
     * 
     * @param previousModulesSnapshot Snapshot of modules before refresh
     * @param previousCountSnapshot Count of modules before refresh
     */
    private void performFullRefresh(List<GameModule> previousModulesSnapshot, int previousCountSnapshot) {
        // Extract previous module names for change detection
        Set<String> previousModuleNames = moduleDiscoveryHandler.extractModuleNames(previousModulesSnapshot, previousCountSnapshot);

        // Discover and load modules using ViewModel (with failures)
        ModuleDiscovery.ModuleLoadResult discoveryResult = moduleDiscoveryHandler.discoverModulesWithFailures(uiUpdater.availableGameModules.size());
        
        if (discoveryResult == null) {
            return;
        }
        
        List<GameModule> discoveredGameModules = discoveryResult.getLoadedModules();
        
        // Log and report discovered modules
        moduleDiscoveryHandler.reportDiscoveredModules(discoveredGameModules, previousCountSnapshot);
        
        // Check for compilation failures and report them
        checkAndReportCompilationFailures(discoveryResult.getCompilationFailures());
        
        // Update ComboBox UI on JavaFX helpers
        uiUpdater.updateComboBoxUI(discoveredGameModules);
        
        // Detect and report module changes
        int currentModuleCount = discoveredGameModules.size();
        reportModuleChanges(previousModuleNames, discoveredGameModules, previousCountSnapshot, currentModuleCount);
        
        // Update state for next comparison
        previousModuleCount = currentModuleCount;
    }
    
    /**
     * Checks for compilation failures and reports them to the user.
     * 
     * @param compilationFailures List of module names that failed to compile
     */
    private void checkAndReportCompilationFailures(List<String> compilationFailures) {
        if (moduleCompilationChecker != null && !compilationFailures.isEmpty()) {
            for (String moduleName : compilationFailures) {
                messageManager.addMessage("Module '" + moduleName + "' failed to compile - check source code for errors");
            }
        }
    }
    
    /**
     * Detects and reports changes in available modules.
     * 
     * @param previousModuleNames Set of module names before refresh
     * @param discoveredGameModules List of discovered modules after refresh
     * @param previousCount Previous module count
     * @param currentModuleCount Current module count
     */
    private void reportModuleChanges(Set<String> previousModuleNames, 
                                     List<GameModule> discoveredGameModules,
                                     int previousCount, 
                                     int currentModuleCount) {
        if (currentModuleCount == 0) {
            messageManager.addMessage("No game modules found");
            return;
        }
        
        if (previousCount > 0) {
            // Detect additions and removals
            Set<String> currentModuleNames = moduleDiscoveryHandler.collectModuleNames(discoveredGameModules);
            Set<String> removedNames = new HashSet<>(previousModuleNames);
            removedNames.removeAll(currentModuleNames);
            Set<String> addedNames = new HashSet<>(currentModuleNames);
            addedNames.removeAll(previousModuleNames);
            
            // Track removed modules
            removedModuleNames.addAll(removedNames);
            
            // Report removals
            for (String removedName : removedNames) {
                messageManager.addMessage("Game module '" + removedName + "' was removed or disabled");
            }
            
            // Report additions
            for (String addedName : addedNames) {
                messageManager.addMessage("New game module '" + addedName + "' was added");
            }
        }
        
        messageManager.addMessage("Successfully detected " + currentModuleCount + " game(s)");
    }
    
    /**
     * Handles errors during full refresh operation.
     * 
     * @param error The exception that occurred
     */
    private void handleRefreshError(Exception error) {
        Logging.error("Error refreshing game list: " + error.getMessage(), error);
        Platform.runLater(() -> {
            messageManager.addMessage("Error refreshing game list: " + error.getMessage());
        });
    }
    
    /**
     * Stops the refresh animation and resets refresh state.
     */
    private void stopRefreshAnimation() {
        Platform.runLater(() -> {
            loadingAnimationManager.stopAnimation();
            messageManager.setRefreshing(false);
        });
    }
    
    // ==================== PRIVATE HELPER METHODS - FAST REFRESH ====================
    
    /**
     * Handles errors during fast refresh operation.
     * 
     * @param error The exception that occurred
     */
    private void handleFastRefreshError(Exception error) {
        messageManager.addMessage("Error refreshing game modules: " + error.getMessage());
        Logging.error("Error refreshing game modules: " + error.getMessage(), error);
    }
}

