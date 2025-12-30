package launcher.gui.lobby.ui_logic.managers.core.factories;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.ui_logic.managers.core.LobbyInitializationManager;
import launcher.gui.lobby.ui_logic.managers.game.GameLaunchErrorHandler;
import launcher.gui.lobby.ui_logic.managers.game.GameLaunchManager;
import launcher.gui.lobby.ui_logic.managers.game.GameModuleRefreshManager;
import launcher.gui.lobby.ui_logic.managers.game.ModuleCompilationChecker;
import launcher.gui.lobby.ui_logic.managers.json.JsonEditorOperations;
import launcher.gui.lobby.ui_logic.managers.ui.LoadingAnimationManager;
import launcher.gui.lobby.ui_logic.subcontrollers.GameSelectionController;
import launcher.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;
import launcher.gui.lobby.ui_logic.subcontrollers.TopBarController;

/**
 * Factory for updating ViewModel references in components.
 * Encapsulates ViewModel update logic to reduce complexity in LobbyInitializationManager.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class ViewModelUpdateFactory {
    
    /**
     * Update ViewModel references in all components that need it.
     * Since some components have final ViewModel fields, they need to be recreated.
     * 
     * @param applicationViewModel The ViewModel to set
     * @param messageReporter Callback for reporting messages
     * @param gameSelector The game selection ComboBox
     * @param launchGameButton The launch game button
     * @param refreshButton The refresh button
     * @param loadingProgressBar The loading progress bar
     * @param loadingStatusLabel The loading status label
     * @param currentResult The current initialization result
     * @return Updated components that need ViewModel references
     */
    public static ViewModelUpdateResult updateComponents(
            GDKViewModel applicationViewModel,
            LobbyInitializationManager.MessageReporter messageReporter,
            ComboBox<?> gameSelector,
            Button launchGameButton,
            Button refreshButton,
            ProgressBar loadingProgressBar,
            Label loadingStatusLabel,
            LobbyInitializationManager.InitializationResult currentResult) {
        
        // Recreate components with final ViewModel fields
        ModuleCompilationChecker moduleCompilationChecker = new ModuleCompilationChecker(applicationViewModel, messageReporter::addMessage);
        JsonEditorOperations jsonEditorOperations = new JsonEditorOperations(applicationViewModel, 
            currentResult.jsonInputEditor(), currentResult.jsonOutputEditor(), messageReporter::addMessage);
        
        // Recreate GameSelectionController (no ViewModel dependency)
        GameSelectionController gameSelectionController = new GameSelectionController(
            (ComboBox) gameSelector,
            launchGameButton,
            currentResult.gameSelectionController().getAvailableGameModules(),
            currentResult.messageManager(),
            currentResult.launchButtonManager(),
            currentResult.jsonPersistenceManager()
        );
        
        // Recreate LoadingAnimationManager (no ViewModel dependency)
        LoadingAnimationManager loadingAnimationManager = new LoadingAnimationManager(
            refreshButton,
            loadingProgressBar,
            loadingStatusLabel
        );
        
        // Recreate GameModuleRefreshManager with new ViewModel
        GameModuleRefreshManager gameModuleRefreshManager = new GameModuleRefreshManager(
            applicationViewModel,
            currentResult.gameSelectionController().getAvailableGameModules(),
            (ComboBox) gameSelector,
            currentResult.messageManager(),
            currentResult.statusLabelManager(),
            currentResult.launchButtonManager(),
            currentResult.moduleChangeReporter(),
            loadingAnimationManager,
            moduleCompilationChecker
        );
        
        // Recreate GameLaunchErrorHandler (no ViewModel dependency)
        GameLaunchErrorHandler gameLaunchErrorHandler = new GameLaunchErrorHandler(currentResult.messageManager());
        
        // Recreate GameLaunchManager with new ViewModel
        GameLaunchManager gameLaunchManager = new GameLaunchManager(applicationViewModel, 
            currentResult.jsonActionButtonsController(), gameLaunchErrorHandler);
        
        // Initialize the recreated subcontroller
        gameSelectionController.initialize();
        
        return new ViewModelUpdateResult(
            moduleCompilationChecker,
            jsonEditorOperations,
            gameSelectionController,
            loadingAnimationManager,
            gameModuleRefreshManager,
            gameLaunchManager
        );
    }
    
    /**
     * Result containing updated components with new ViewModel references.
     */
    public record ViewModelUpdateResult(
        ModuleCompilationChecker moduleCompilationChecker,
        JsonEditorOperations jsonEditorOperations,
        GameSelectionController gameSelectionController,
        LoadingAnimationManager loadingAnimationManager,
        GameModuleRefreshManager gameModuleRefreshManager,
        GameLaunchManager gameLaunchManager
    ) {}
}

