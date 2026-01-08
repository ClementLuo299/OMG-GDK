package launcher.ui_areas.lobby.lifecycle.startup.controller_initialization;

import gdk.internal.Logging;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.lobby.GDKViewModel;
import launcher.ui_areas.lobby.lifecycle.startup.component_setup.callback_wiring.CallbackWiring;
import launcher.ui_areas.lobby.lifecycle.startup.controller_initialization.ControllerInitialization;
import launcher.ui_areas.lobby.factories.ViewModelDependentsFactory;

/**
 * Manages ViewModel initialization and updates for the lobby controller.
 * Handles updating ViewModel references in components that depend on it.
 * 
 * @author Clement Luo
 * @date January 8, 2026
 * @since Beta 1.0
 */
public class ViewModelInitialization {
    
    private final GDKGameLobbyController controller;
    private final ControllerInitialization.MessageReporter messageReporter;
    
    /**
     * Create a new ViewModelInitialization.
     * 
     * @param controller The main lobby controller
     * @param messageReporter Callback for reporting messages
     */
    public ViewModelInitialization(GDKGameLobbyController controller, ControllerInitialization.MessageReporter messageReporter) {
        this.controller = controller;
        this.messageReporter = messageReporter;
    }
    
    // ==================== VIEWMODEL UPDATES ====================
    
    /**
     * Update ViewModel references in all components that need it.
     * Since some components have final ViewModel fields, they need to be recreated.
     * Called when the ViewModel becomes available after initial initialization.
     * 
     * @param applicationViewModel The ViewModel to set
     * @param currentResult The current initialization result
     * @return Updated initialization result with new ViewModel references
     */
    public ControllerInitialization.InitializationResult updateViewModel(
            GDKViewModel applicationViewModel, 
            ControllerInitialization.InitializationResult currentResult) {
        
        Logging.info("Updating ViewModel references in lobby components");
        
        // ==================== RECREATE VIEWMODEL-DEPENDENT COMPONENTS ====================
        
        ViewModelDependentsFactory.ViewModelDependentsResult updateResult = ViewModelDependentsFactory.recreateComponents(
            applicationViewModel, messageReporter, controller.getGameSelector(), controller.getLaunchGameButton(), controller.getRefreshButton(),
            controller.getLoadingProgressBar(), controller.getLoadingStatusLabel(), currentResult
        );
        
        // ==================== RE-WIRE CALLBACKS ====================
        
        CallbackWiring.wireCallbacks(updateResult.gameSelectionController(), currentResult.jsonActionButtonsController(),
            currentResult.topBarController(), updateResult.gameLaunchManager(), currentResult.lobbyShutdownManager(),
            currentResult.settingsNavigationManager(), updateResult.gameModuleRefreshManager());
        
        // ==================== BUILD UPDATED RESULT ====================
        
        return new ControllerInitialization.InitializationResult(currentResult.jsonInputEditor(), currentResult.jsonOutputEditor(),
            currentResult.messageManager(), updateResult.loadingAnimationManager(), updateResult.moduleCompilationChecker(),
            updateResult.jsonEditorOperations(), currentResult.statusLabelManager(), currentResult.launchButtonManager(),
            currentResult.moduleChangeReporter(), updateResult.gameLaunchManager(), currentResult.messageBridgeManager(),
            currentResult.lobbyShutdownManager(), currentResult.settingsNavigationManager(),
            updateResult.gameSelectionController(), currentResult.jsonActionButtonsController(),
            currentResult.topBarController(), updateResult.gameModuleRefreshManager());
    }
}

