package launcher.ui_areas.lobby.lifecycle.ui_initialization;

import javafx.scene.Scene;
import javafx.stage.Stage;
import launcher.ui_areas.lobby.GDKViewModel;
import launcher.ui_areas.lobby.lifecycle.ui_initialization.main_stage.MainStageInitializer;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.lobby.lifecycle.ui_initialization.ui_optimizers.SceneOptimizer;
import launcher.ui_areas.lobby.lifecycle.ui_initialization.ui_optimizers.StageOptimizer;
import launcher.ui_areas.lobby.lifecycle.ui_initialization.scene_and_controller.LoadLobbySceneAndGetController;
import launcher.ui_areas.lobby.lifecycle.ui_initialization.viewmodel.ViewModelInitializer;
import launcher.ui_areas.lobby.lifecycle.ui_initialization.viewmodel.WireViewModelToController;

/**
 * Initializes the main user interface components.
 * Orchestrates the UI setup process by coordinating scene ui_loading, ViewModel creation,
 * stage configuration, and controller-ViewModel wiring.
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public final class InitializeLobbyUIForStandardLaunch {

    private InitializeLobbyUIForStandardLaunch() {}

    /**
     * Initializes the main user interface components for the GDK application.
     * Orchestrates the complete UI setup process including resource initialization,
     * ui_loading the FXML scene, creating the ViewModel, configuring the primary stage,
     * and wiring up the controller.
     * 
     * @param primaryApplicationStage The primary JavaFX stage for the application
     * @return The initialized GDKGameLobbyController
     */
    public static GDKGameLobbyController initialize(Stage primaryApplicationStage) {

        // Load the main scene and controller from FXML
        LoadLobbySceneAndGetController.SceneLoadResult loadResult = LoadLobbySceneAndGetController.loadMainScene();
        Scene mainLobbyScene = loadResult.scene();
        GDKGameLobbyController lobbyController = loadResult.controller();
        
        // Create and configure the ViewModel
        GDKViewModel applicationViewModel = ViewModelInitializer.createForStandardLaunch(primaryApplicationStage, mainLobbyScene);
        
        // Initialize the primary stage
        MainStageInitializer.initialize(primaryApplicationStage, mainLobbyScene);
        
        // Apply performance optimizations
        StageOptimizer.optimize(primaryApplicationStage, mainLobbyScene);
        SceneOptimizer.optimize(mainLobbyScene);
        
        // Wire up the controller with the ViewModel
        WireViewModelToController.wireUp(applicationViewModel, lobbyController);
        
        return lobbyController;
    }
} 

