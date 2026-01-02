package launcher.ui_areas.lobby.more_managers.core.factories;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import launcher.features.game_launching.GameLaunchService;
import launcher.features.json_processing.JsonProcessingService;
import launcher.ui_areas.lobby.GDKViewModel;
import launcher.ui_areas.lobby.more_managers.core.LobbyInitializationManager;
import launcher.ui_areas.lobby.more_managers.game_launching.GameLaunchErrorHandler;
import launcher.ui_areas.lobby.managers.game_launching.GameLaunchingManager;
import launcher.ui_areas.lobby.more_managers.game_launching.GameModuleRefreshManager;
import launcher.ui_areas.lobby.more_managers.game_launching.ModuleCompilationChecker;
import launcher.ui_areas.lobby.managers.json.JsonEditorOperations;
import launcher.ui_areas.lobby.managers.ui.LoadingAnimationManager;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;

/**
 * Factory for recreating components that depend on ViewModel.
 * Encapsulates ViewModel-dependent component recreation logic to reduce complexity in LobbyInitializationManager.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class ViewModelDependentsFactory {
    
    /**
     * Recreate components that depend on ViewModel with new ViewModel references.
     * Since some components have final ViewModel fields, they need to be recreated.
     * 
     * @param applicationViewModel The ViewModel to set
     * @param messageReporter Callback for reporting messages
     * @param gameSelector The game selection ComboBox
     * @param launchGameButton The launch game button
     * @param refreshButton The refresh button
     * @param loadingProgressBar The ui_loading progress bar
     * @param loadingStatusLabel The ui_loading status label
     * @param currentResult The current initialization result
     * @return Recreated components with new ViewModel references
     */
    public static ViewModelDependentsResult recreateComponents(
            GDKViewModel applicationViewModel,
            LobbyInitializationManager.MessageReporter messageReporter,
            ComboBox<?> gameSelector,
            Button launchGameButton,
            Button refreshButton,
            ProgressBar loadingProgressBar,
            Label loadingStatusLabel,
            LobbyInitializationManager.InitializationResult currentResult) {
        
        // ==================== RECREATE VIEWMODEL-DEPENDENT COMPONENTS ====================
        
        ModuleCompilationChecker moduleCompilationChecker = new ModuleCompilationChecker(applicationViewModel, messageReporter::addMessage);
        
        // Create business service for JSON processing
        JsonProcessingService jsonProcessingService = new JsonProcessingService(applicationViewModel);
        JsonEditorOperations jsonEditorOperations = new JsonEditorOperations(jsonProcessingService, 
            currentResult.jsonInputEditor(), currentResult.jsonOutputEditor(), messageReporter::addMessage);
        
        // ==================== RECREATE COMPONENTS (NO VIEWMODEL DEPENDENCY) ====================
        
        GameSelectionController gameSelectionController = new GameSelectionController(
            (ComboBox) gameSelector,
            launchGameButton,
            currentResult.gameSelectionController().getAvailableGameModules(),
            currentResult.messageManager(),
            currentResult.launchButtonManager(),
            currentResult.jsonPersistenceManager()
        );
        
        LoadingAnimationManager loadingAnimationManager = new LoadingAnimationManager(
            refreshButton,
            loadingProgressBar,
            loadingStatusLabel
        );
        
        GameLaunchErrorHandler gameLaunchErrorHandler = new GameLaunchErrorHandler(currentResult.messageManager());
        
        // ==================== RECREATE VIEWMODEL-DEPENDENT MANAGERS ====================
        
        // Note: GameModuleRefreshManager creates ModuleDiscoveryService internally
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
        
        // Create business service for game launching
        GameLaunchService gameLaunchService = new GameLaunchService(applicationViewModel);
        GameLaunchingManager gameLaunchManager = new GameLaunchingManager(gameLaunchService, 
            currentResult.jsonActionButtonsController(), gameLaunchErrorHandler);
        
        // ==================== INITIALIZE RECREATED COMPONENTS ====================
        
        gameSelectionController.initialize();
        
        return new ViewModelDependentsResult(
            moduleCompilationChecker,
            jsonEditorOperations,
            gameSelectionController,
            loadingAnimationManager,
            gameModuleRefreshManager,
            gameLaunchManager
        );
    }
    
    /**
     * Result containing recreated components with new ViewModel references.
     */
    public record ViewModelDependentsResult(
        ModuleCompilationChecker moduleCompilationChecker,
        JsonEditorOperations jsonEditorOperations,
        GameSelectionController gameSelectionController,
        LoadingAnimationManager loadingAnimationManager,
        GameModuleRefreshManager gameModuleRefreshManager,
        GameLaunchingManager gameLaunchManager
    ) {}
}

