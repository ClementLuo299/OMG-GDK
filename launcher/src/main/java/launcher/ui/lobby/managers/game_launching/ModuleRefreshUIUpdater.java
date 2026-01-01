package launcher.ui.lobby.managers.game_launching;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.ui.lobby.managers.messaging.MessageManager;
import launcher.ui.lobby.managers.ui.LaunchButtonManager;
import launcher.ui.lobby.managers.ui.StatusLabelManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

import java.util.List;
import java.util.Set;

/**
 * Handles UI updates for module refresh operations.
 * 
 * <p>This updater is responsible for:
 * <ul>
 *   <li>Updating the ComboBox with discovered modules</li>
 *   <li>Updating status labels and button states</li>
 *   <li>Logging UI state for debugging</li>
 *   <li>Ensuring thread safety (JavaFX application thread)</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date December 30, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class ModuleRefreshUIUpdater {
    
    // ==================== DEPENDENCIES ====================
    
    /** Observable list of available game modules (bound to UI). */
    final ObservableList<GameModule> availableGameModules;
    
    /** ComboBox UI component for game selection. */
    private final ComboBox<GameModule> gameSelector;
    
    /** Manager for updating status label text. */
    private final StatusLabelManager statusLabelManager;
    
    /** Manager for updating launch button state. */
    private final LaunchButtonManager launchButtonManager;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new ModuleRefreshUIUpdater.
     * 
     * @param availableGameModules The observable list of available game modules
     * @param gameSelector The game selector ComboBox UI component
     * @param statusLabelManager The status label manager for status updates
     * @param launchButtonManager The launch button manager for button state
     */
    public ModuleRefreshUIUpdater(ObservableList<GameModule> availableGameModules,
                                 ComboBox<GameModule> gameSelector,
                                 StatusLabelManager statusLabelManager,
                                 LaunchButtonManager launchButtonManager) {
        this.availableGameModules = availableGameModules;
        this.gameSelector = gameSelector;
        this.statusLabelManager = statusLabelManager;
        this.launchButtonManager = launchButtonManager;
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Updates the ComboBox UI with discovered modules on the JavaFX thread.
     * 
     * @param discoveredGameModules List of discovered game modules
     */
    public void updateComboBoxUI(List<GameModule> discoveredGameModules) {
        Logging.info("Updating ComboBox with " + discoveredGameModules.size() + " modules");
        
        Platform.runLater(() -> {
            try {
                availableGameModules.setAll(discoveredGameModules);
                gameSelector.setItems(availableGameModules);
                gameSelector.getSelectionModel().clearSelection();
                statusLabelManager.updateGameCountStatus(availableGameModules.size());
                launchButtonManager.updateLaunchButtonState(false);
                gameSelector.requestLayout();

                // Log ComboBox state for debugging
                Logging.info("ComboBox items after refresh: " + gameSelector.getItems().size());
                for (GameModule module : gameSelector.getItems()) {
                    Logging.info("   • " + module.getMetadata().getGameName());
                }
            } catch (Exception e) {
                Logging.error("Error updating ComboBox items on refresh: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Updates UI components for fast refresh operation.
     * 
     * <p>This method:
     * <ul>
     *   <li>Updates the observable list with valid modules</li>
     *   <li>Updates the ComboBox</li>
     *   <li>Updates status labels and button states</li>
     *   <li>Logs the final UI state</li>
     * </ul>
     * 
     * @param validModules List of valid modules to display
     * @param discoveryResult Result containing module names and UI messages
     * @param messageManager Message manager for displaying messages
     * @param moduleChangeReporter Reporter for module change notifications
     * @param previousModuleNames Set of previous module names for change detection
     * @return The new module count after update
     */
    public int updateUIForFastRefresh(List<GameModule> validModules,
                                      launcher.features.lobby_features.managers.game_launching.ModuleDiscoveryHandler.ModuleDiscoveryResult discoveryResult,
                                      MessageManager messageManager,
                                      launcher.features.lobby_features.managers.game_launching.ModuleChangesReporter moduleChangeReporter,
                                      Set<String> previousModuleNames) {
        Runnable uiUpdate = () -> {
            try {
                Logging.info("Updating UI on JavaFX thread...");
                Logging.info("   Current availableGameModules size: " + availableGameModules.size());
                Logging.info("   Modules to add: " + validModules.size());
                
                // Update observable list
                availableGameModules.clear();
                availableGameModules.addAll(validModules);
                
                Logging.info("   After adding, availableGameModules size: " + availableGameModules.size());
                
                // Update ComboBox
                updateComboBoxForFastRefresh(validModules);
                
                // Update status and button state
                statusLabelManager.updateGameCountStatus(availableGameModules.size());
                launchButtonManager.updateLaunchButtonState(false);
                
                // Log final state for debugging
                logUIState();
                
                // Display messages and report changes
                discoveryResult.getUiMessages().forEach(messageManager::addMessage);
                
                if (moduleChangeReporter != null) {
                    moduleChangeReporter.reportModuleChanges(previousModuleNames, discoveryResult.getModuleNames());
                }
                
                Logging.info("UI update completed successfully");
            } catch (Exception e) {
                Logging.error("Error updating ComboBox items during fast refresh: " + e.getMessage(), e);
                e.printStackTrace();
            }
        };
        
        // Execute on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            Logging.info("Already on JavaFX thread - running UI update directly");
            uiUpdate.run();
        } else {
            Logging.info("Not on JavaFX thread - scheduling UI update");
            Platform.runLater(uiUpdate);
        }
        
        return availableGameModules.size();
    }
    
    /**
     * Clears UI selections immediately to prevent launching stale modules.
     */
    public void clearUISelections() {
        availableGameModules.clear();
        gameSelector.getSelectionModel().clearSelection();
    }
    
    /**
     * Updates UI state when no modules are discovered.
     * 
     * @param messageManager Message manager for user feedback
     */
    public void handleNoModulesDiscovered(MessageManager messageManager) {
        Logging.warning("No modules discovered");
        Platform.runLater(() -> {
            messageManager.addMessage("No valid modules found");
            statusLabelManager.updateGameCountStatus(availableGameModules.size());
            launchButtonManager.updateLaunchButtonState(false);
        });
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Updates the ComboBox UI component for fast refresh.
     * 
     * @param validModules List of valid modules to set in ComboBox
     */
    private void updateComboBoxForFastRefresh(List<GameModule> validModules) {
        if (gameSelector != null) {
            gameSelector.setItems(availableGameModules);
            gameSelector.getSelectionModel().clearSelection();
            gameSelector.requestLayout();
            Logging.info("ComboBox items after fast refresh: " + gameSelector.getItems().size());
            
            // Safety check: if ComboBox is empty but we have modules, force update
            if (gameSelector.getItems().isEmpty() && !validModules.isEmpty()) {
                Logging.error("ComboBox is empty but validModules is not! This is a bug.");
                gameSelector.setItems(FXCollections.observableArrayList(validModules));
            }
        } else {
            Logging.error("gameSelector is null!");
        }
    }
    
    /**
     * Logs the current UI state for debugging purposes.
     */
    private void logUIState() {
        if (gameSelector != null) {
            for (GameModule module : gameSelector.getItems()) {
                if (module != null) {
                    Logging.info("   • ComboBox item: " + module.getMetadata().getGameName());
                }
            }
        }
        
        Logging.info("Final UI state: " + availableGameModules.size() + " modules in ObservableList");
        for (GameModule module : availableGameModules) {
            if (module != null) {
                Logging.info("   - ObservableList item: " + module.getMetadata().getGameName());
            }
        }
    }
}

