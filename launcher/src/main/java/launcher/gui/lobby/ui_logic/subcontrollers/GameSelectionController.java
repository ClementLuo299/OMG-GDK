package launcher.gui.lobby.ui_logic.subcontrollers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.ui_logic.managers.MessageManager;
import launcher.gui.lobby.ui_logic.managers.UIStateManager;
import launcher.gui.lobby.ui_logic.managers.LoadingAnimationManager;
import launcher.gui.lobby.ui_logic.managers.ModuleCompilationChecker;
import launcher.gui.lobby.persistence.JsonPersistenceManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Subcontroller for the Game Selection UI area.
 * 
 * Handles game selection, refresh operations, and launch button management.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class GameSelectionController {
    
    // UI Components
    private final ComboBox<GameModule> gameSelector;
    private final Button launchGameButton;
    private final Button refreshButton;
    private final Button settingsButton;
    
    // Managers
    private final GDKViewModel viewModel;
    private final MessageManager messageManager;
    private final UIStateManager uiStateManager;
    private final LoadingAnimationManager loadingAnimationManager;
    private final ModuleCompilationChecker moduleCompilationChecker;
    private final JsonPersistenceManager jsonPersistenceManager;
    
    // State
    private final ObservableList<GameModule> availableGameModules;
    private GameModule selectedGameModule;
    private int previousModuleCount = 0;
    private final Set<String> removedModuleNames = new HashSet<>();
    
    // Callbacks
    private Runnable onLaunchGame;
    private Runnable onOpenSettings;
    private Runnable onGameSelected;
    
    /**
     * Create a new GameSelectionController.
     * 
     * @param gameSelector The game selection ComboBox
     * @param launchGameButton The launch game button
     * @param refreshButton The refresh button
     * @param settingsButton The settings button
     * @param availableGameModules The observable list of available game modules
     * @param viewModel The ViewModel for business logic
     * @param messageManager The message manager
     * @param uiStateManager The UI state manager
     * @param loadingAnimationManager The loading animation manager
     * @param moduleCompilationChecker The module compilation checker
     * @param jsonPersistenceManager The JSON persistence manager
     */
    public GameSelectionController(
            ComboBox<GameModule> gameSelector,
            Button launchGameButton,
            Button refreshButton,
            Button settingsButton,
            ObservableList<GameModule> availableGameModules,
            GDKViewModel viewModel,
            MessageManager messageManager,
            UIStateManager uiStateManager,
            LoadingAnimationManager loadingAnimationManager,
            ModuleCompilationChecker moduleCompilationChecker,
            JsonPersistenceManager jsonPersistenceManager) {
        
        this.gameSelector = gameSelector;
        this.launchGameButton = launchGameButton;
        this.refreshButton = refreshButton;
        this.settingsButton = settingsButton;
        this.availableGameModules = availableGameModules;
        this.viewModel = viewModel;
        this.messageManager = messageManager;
        this.uiStateManager = uiStateManager;
        this.loadingAnimationManager = loadingAnimationManager;
        this.moduleCompilationChecker = moduleCompilationChecker;
        this.jsonPersistenceManager = jsonPersistenceManager;
    }
    
    /**
     * Set callback for when launch game is requested.
     * 
     * @param onLaunchGame The callback to execute
     */
    public void setOnLaunchGame(Runnable onLaunchGame) {
        this.onLaunchGame = onLaunchGame;
    }
    
    /**
     * Set callback for when settings page should be opened.
     * 
     * @param onOpenSettings The callback to execute
     */
    public void setOnOpenSettings(Runnable onOpenSettings) {
        this.onOpenSettings = onOpenSettings;
    }
    
    /**
     * Set callback for when a game is selected.
     * 
     * @param onGameSelected The callback to execute
     */
    public void setOnGameSelected(Runnable onGameSelected) {
        this.onGameSelected = onGameSelected;
    }
    
    /**
     * Get the currently selected game module.
     * 
     * @return The selected game module, or null if none selected
     */
    public GameModule getSelectedGameModule() {
        return selectedGameModule;
    }
    
    /**
     * Set the selected game module.
     * 
     * @param gameModule The game module to select
     */
    public void setSelectedGameModule(GameModule gameModule) {
        this.selectedGameModule = gameModule;
    }
    
    /**
     * Get the observable list of available game modules.
     * 
     * @return The observable list of available game modules
     */
    public ObservableList<GameModule> getAvailableGameModules() {
        return availableGameModules;
    }
    
    /**
     * Initialize the game selection UI and event handlers.
     */
    public void initialize() {
        // Configure the ComboBox to display game module names from metadata
        gameSelector.setCellFactory(param -> new javafx.scene.control.ListCell<GameModule>() {
            @Override
            protected void updateItem(GameModule gameModule, boolean empty) {
                super.updateItem(gameModule, empty);
                if (empty || gameModule == null) {
                    setText("Select a game...");
                } else {
                    setText(gameModule.getMetadata().getGameName());
                }
            }
        });
        
        gameSelector.setButtonCell(gameSelector.getCellFactory().call(null));
        gameSelector.setItems(availableGameModules);
        
        // Game Selection Handler
        gameSelector.setOnAction(event -> {
            selectedGameModule = gameSelector.getValue();
            String selectedGameName = selectedGameModule != null ? selectedGameModule.getMetadata().getGameName() : "None";
            
            // Update launch button state based on selection
            uiStateManager.updateLaunchButtonState(selectedGameModule != null);
            
            if (selectedGameModule != null) {
                messageManager.addMessage("🎮 Selected game: " + selectedGameName);
                jsonPersistenceManager.persistSelectedGame(selectedGameName);
            } else {
                messageManager.addMessage("⚠️ No game selected");
            }
            
            if (onGameSelected != null) {
                onGameSelected.run();
            }
        });
        
        // Launch button: Start the selected game with validation
        launchGameButton.setOnAction(event -> {
            if (onLaunchGame != null) {
                onLaunchGame.run();
            }
        });
        
        // Initialize launch button state (disabled until a game is selected)
        uiStateManager.updateLaunchButtonState(false);
        
        // Refresh button: Reload the list of available games
        refreshButton.setOnAction(event -> handleRefresh());
        
        // Settings button: Open settings page
        settingsButton.setOnAction(event -> {
            if (onOpenSettings != null) {
                onOpenSettings.run();
            }
        });
    }
    
    /**
     * Handle the refresh button click.
     */
    private void handleRefresh() {
        // Start loading animation
        loadingAnimationManager.startAnimation();
        messageManager.setRefreshing(true);
        
        // Clear the message container immediately
        messageManager.clearMessages();

        // Snapshot current modules on the JavaFX thread for safe background use
        List<GameModule> previousModulesSnapshot = new ArrayList<>(availableGameModules);
        int previousCountSnapshot = previousModuleCount;

        // Reset UI selections immediately to avoid launching stale modules
        availableGameModules.clear();
        selectedGameModule = null;
        gameSelector.getSelectionModel().clearSelection();
        
        // Run the refresh in a background thread to keep UI responsive
        new Thread(() -> {
            try {
                // Store previous module names for removal detection
                Set<String> previousModuleNames = new HashSet<>();
                if (previousCountSnapshot > 0) {
                    for (GameModule module : previousModulesSnapshot) {
                        previousModuleNames.add(module.getMetadata().getGameName());
                    }
                }

                // Delegate module discovery to ViewModel (business logic)
                final List<GameModule> discoveredGameModules;
                if (viewModel != null) {
                    discoveredGameModules = viewModel.discoverAndLoadModules();
                } else {
                    // Fallback if ViewModel not available (shouldn't happen)
                    Logging.error("❌ ViewModel not available for module discovery");
                    messageManager.addMessage("❌ Error: ViewModel not available");
                    discoveredGameModules = new ArrayList<>();
                }
                
                // Add each discovered module to our observable list
                for (GameModule gameModule : discoveredGameModules) {
                    String gameName = gameModule.getMetadata().getGameName();
                    String className = gameModule.getClass().getSimpleName();
                    Logging.info("📦 Loaded game module: " + gameName + " (" + className + ")");
                    
                    // Add user-friendly message for each detected game (only on first load)
                    if (previousCountSnapshot == 0) {
                        messageManager.addMessage("🎮 Detected game: " + gameName);
                    }
                }
                
                // Check for compilation failures using ViewModel (business logic)
                if (viewModel != null && moduleCompilationChecker != null) {
                    List<String> compilationFailures = viewModel.checkForCompilationFailures();
                    if (!compilationFailures.isEmpty()) {
                        for (String moduleName : compilationFailures) {
                            messageManager.addMessage("⚠️ Module '" + moduleName + "' failed to compile - check source code for errors");
                        }
                    }
                }
                
                // Update the ComboBox with the new list of games
                Logging.info("🔄 Updating ComboBox with " + discoveredGameModules.size() + " modules");
                Logging.info("🧭 Scheduling ComboBox UI update...");
                
                Platform.runLater(() -> {
                    try {
                        availableGameModules.setAll(discoveredGameModules);
                        selectedGameModule = null;
                        gameSelector.setItems(availableGameModules);
                        gameSelector.getSelectionModel().clearSelection();
                        uiStateManager.updateGameCountStatus(availableGameModules.size());
                        uiStateManager.updateLaunchButtonState(false);
                        gameSelector.requestLayout();

                        Logging.info("📋 ComboBox items after refresh: " + gameSelector.getItems().size());
                        for (GameModule module : gameSelector.getItems()) {
                            Logging.info("   • " + module.getMetadata().getGameName());
                        }
                    } catch (Exception e) {
                        Logging.error("❌ Error updating ComboBox items on refresh: " + e.getMessage(), e);
                    }
                });
                
                // Check for module changes and provide user feedback
                int currentModuleCount = discoveredGameModules.size();
                
                if (currentModuleCount == 0) {
                    messageManager.addMessage("⚠️ No game modules found");
                } else if (previousCountSnapshot > 0) {
                    // Check for module changes (additions/removals)
                    Set<String> currentModuleNames = new HashSet<>();
                    for (GameModule module : discoveredGameModules) {
                        currentModuleNames.add(module.getMetadata().getGameName());
                    }
                    
                    // Find removed module names
                    Set<String> removedNames = new HashSet<>(previousModuleNames);
                    removedNames.removeAll(currentModuleNames);
                    
                    // Find added module names
                    Set<String> addedNames = new HashSet<>(currentModuleNames);
                    addedNames.removeAll(previousModuleNames);
                    
                    // Add removed names to tracking set
                    removedModuleNames.addAll(removedNames);
                    
                    // Show specific removal messages
                    for (String removedName : removedNames) {
                        messageManager.addMessage("⚠️ Game module '" + removedName + "' was removed or disabled");
                    }
                    
                    // Show specific addition messages
                    for (String addedName : addedNames) {
                        messageManager.addMessage("✅ New game module '" + addedName + "' was added");
                    }
                    
                    messageManager.addMessage("✅ Successfully detected " + currentModuleCount + " game(s)");
                } else {
                    messageManager.addMessage("✅ Successfully detected " + currentModuleCount + " game(s)");
                }
                
                // Update the previous count for next comparison
                previousModuleCount = currentModuleCount;
                
            } catch (Exception moduleDiscoveryError) {
                // Handle any errors during module discovery
                Logging.error("❌ Error refreshing game list: " + moduleDiscoveryError.getMessage(), moduleDiscoveryError);
                Platform.runLater(() -> {
                    messageManager.addMessage("❌ Error refreshing game list: " + moduleDiscoveryError.getMessage());
                });
            } finally {
                // Stop loading animation when done (regardless of success or failure)
                Platform.runLater(() -> {
                    loadingAnimationManager.stopAnimation();
                    messageManager.setRefreshing(false);
                });
            }
        }).start();
    }
    
    /**
     * Fast refresh that skips compilation checks for faster startup.
     */
    public void refreshAvailableGameModulesFast() {
        try {
            Logging.info("🔄 refreshAvailableGameModulesFast() called");
            Set<String> previousModuleNames = new HashSet<>();
            for (GameModule module : availableGameModules) {
                previousModuleNames.add(module.getMetadata().getGameName());
            }
            Logging.info("📊 Previous module count: " + previousModuleNames.size());

            // Delegate module discovery to ViewModel (business logic)
            final List<GameModule> discoveredGameModules;
            if (viewModel != null) {
                discoveredGameModules = viewModel.discoverAndLoadModules();
            } else {
                Logging.error("❌ ViewModel not available for module discovery");
                Platform.runLater(() -> {
                    messageManager.addMessage("❌ Error: ViewModel not available");
                    uiStateManager.updateGameCountStatus(availableGameModules.size());
                });
                return;
            }
            
            Logging.info("✅ Module loading completed. Loaded " + discoveredGameModules.size() + " modules");
            
            if (discoveredGameModules == null || discoveredGameModules.isEmpty()) {
                Logging.warning("⚠️ No modules discovered");
                Platform.runLater(() -> {
                    messageManager.addMessage("⚠️ No valid modules found");
                    uiStateManager.updateGameCountStatus(availableGameModules.size());
                    uiStateManager.updateLaunchButtonState(false);
                });
                return;
            }
            
            // Track newly discovered modules for detailed reporting
            Set<String> newlyDiscoveredModuleNames = new HashSet<>();
            List<String> uiMessages = new ArrayList<>();
            
            for (GameModule gameModule : discoveredGameModules) {
                if (gameModule == null) {
                    Logging.warning("⚠️ Null game module in discovered list - skipping");
                    continue;
                }
                try {
                    String gameName = gameModule.getMetadata().getGameName();
                    String className = gameModule.getClass().getSimpleName();
                    newlyDiscoveredModuleNames.add(gameName);
                    Logging.info("📦 Loaded game module: " + gameName + " (" + className + ")");
                    uiMessages.add("🎮 Detected game: " + gameName);
                } catch (Exception e) {
                    Logging.error("❌ Error getting metadata from game module: " + e.getMessage(), e);
                }
            }
            
            if (!discoveredGameModules.isEmpty()) {
                uiMessages.add("✅ Successfully detected " + discoveredGameModules.size() + " game(s)");
                Logging.info("✅ Ready to update UI with " + discoveredGameModules.size() + " modules");
            } else {
                uiMessages.add("⚠️ No games detected - check modules directory");
                Logging.warning("⚠️ No modules were loaded. Check logs above for loading errors.");
            }
            
            // Create a final list of valid modules (filter out nulls)
            final List<GameModule> validModules = new ArrayList<>();
            for (GameModule module : discoveredGameModules) {
                if (module != null) {
                    validModules.add(module);
                }
            }
            
            Logging.info("📊 Valid modules to add to UI: " + validModules.size());
            
            // Always update UI on JavaFX thread
            Runnable uiUpdate = () -> {
                try {
                    Logging.info("🎨 Updating UI on JavaFX thread...");
                    Logging.info("   Current availableGameModules size: " + availableGameModules.size());
                    Logging.info("   Modules to add: " + validModules.size());
                    
                    availableGameModules.clear();
                    availableGameModules.addAll(validModules);
                    
                    Logging.info("   After adding, availableGameModules size: " + availableGameModules.size());
                    
                    selectedGameModule = null;
                    
                    if (gameSelector != null) {
                        gameSelector.setItems(availableGameModules);
                        gameSelector.getSelectionModel().clearSelection();
                        gameSelector.requestLayout();
                        Logging.info("📋 ComboBox items after fast refresh: " + gameSelector.getItems().size());
                        
                        if (gameSelector.getItems().isEmpty() && !validModules.isEmpty()) {
                            Logging.error("❌ ComboBox is empty but validModules is not! This is a bug.");
                            gameSelector.setItems(FXCollections.observableArrayList(validModules));
                        }
                    } else {
                        Logging.error("❌ gameSelector is null!");
                    }
                    
                    uiStateManager.updateGameCountStatus(availableGameModules.size());
                    uiStateManager.updateLaunchButtonState(false);
                    
                    if (gameSelector != null) {
                        for (GameModule module : gameSelector.getItems()) {
                            if (module != null) {
                                Logging.info("   • ComboBox item: " + module.getMetadata().getGameName());
                            }
                        }
                    }
                    
                    Logging.info("📊 Final UI state: " + availableGameModules.size() + " modules in ObservableList");
                    for (GameModule module : availableGameModules) {
                        if (module != null) {
                            Logging.info("   - ObservableList item: " + module.getMetadata().getGameName());
                        }
                    }
                    
                    uiMessages.forEach(messageManager::addMessage);
                    
                    if (uiStateManager != null) {
                        uiStateManager.reportModuleChanges(previousModuleNames, newlyDiscoveredModuleNames);
                    }
                    previousModuleCount = availableGameModules.size();
                    
                    Logging.info("✅ UI update completed successfully");
                } catch (Exception e) {
                    Logging.error("❌ Error updating ComboBox items during fast refresh: " + e.getMessage(), e);
                    e.printStackTrace();
                }
            };
            
            if (Platform.isFxApplicationThread()) {
                Logging.info("🔄 Already on JavaFX thread - running UI update directly");
                uiUpdate.run();
            } else {
                Logging.info("🔄 Not on JavaFX thread - scheduling UI update");
                Platform.runLater(uiUpdate);
            }
            
        } catch (Exception e) {
            messageManager.addMessage("❌ Error refreshing game modules: " + e.getMessage());
            Logging.error("❌ Error refreshing game modules: " + e.getMessage(), e);
        }
    }
}

