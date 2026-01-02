package launcher.ui_areas.lobby.lifecycle;

import javafx.scene.Scene;
import javafx.stage.Stage;
import launcher.core.ui.ui_loading.fonts.FontLoaderWrapper;
import launcher.features.lobby_features.business.GDKViewModel;
import launcher.core.ui.ui_loading.stage.MainStageInitializer;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.lobby.LobbyOptimizer;
import launcher.ui_areas.lobby.LobbySceneLoader;
import launcher.ui_areas.startup_window.StartupWindowManager;

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
public final class LobbyUIInitializer {

    private LobbyUIInitializer() {}

    /**
     * Initializes the main user interface components for the GDK application.
     * Orchestrates the complete UI setup process including resource initialization,
     * ui_loading the FXML scene, creating the ViewModel, configuring the primary stage,
     * and wiring up the controller.
     * 
     * @param primaryApplicationStage The primary JavaFX stage for the application
     * @param windowManager The startup window manager for progress updates
     * @return The initialized GDKGameLobbyController
     */
    public static GDKGameLobbyController initialize(Stage primaryApplicationStage, StartupWindowManager windowManager) {
        // Initialize application resources before UI creation
        FontLoaderWrapper.initialize();
        
        // Load the main scene from FXML
        LobbySceneLoader.SceneLoadResult loadResult = LobbySceneLoader.loadMainScene();
        Scene mainLobbyScene = loadResult.scene;
        GDKGameLobbyController lobbyController = loadResult.controller;
        
        // Create and configure the ViewModel
        GDKViewModel applicationViewModel = createViewModel(primaryApplicationStage);
        applicationViewModel.setMainLobbyScene(mainLobbyScene);
        
        // Initialize the primary stage
        MainStageInitializer.initialize(primaryApplicationStage, mainLobbyScene);
        
        // Apply performance optimizations
        LobbyOptimizer.optimize(primaryApplicationStage, mainLobbyScene);
        
        // Wire up the controller with the ViewModel
        wireUpControllerWithViewModel(applicationViewModel, lobbyController);
        
        return lobbyController;
    }

    /**
     * Creates and initializes the application ViewModel.
     * Sets up the ViewModel with the primary stage reference for managing application state.
     * 
     * @param primaryApplicationStage The primary JavaFX stage to associate with the ViewModel
     * @return The initialized GDKViewModel instance
     */
    private static GDKViewModel createViewModel(Stage primaryApplicationStage) {
        GDKViewModel applicationViewModel = new GDKViewModel();
        applicationViewModel.setPrimaryStage(primaryApplicationStage);
        return applicationViewModel;
    }

    /**
     * Wires up the controller with the ViewModel to establish the connection between
     * the UI controller and the application's data model. This enables the controller
     * to access and update the ViewModel's state.
     * 
     * @param applicationViewModel The ViewModel instance containing application state
     * @param lobbyController The lobby controller to connect with the ViewModel
     * @throws RuntimeException if the controller is null or wiring fails
     */
    private static void wireUpControllerWithViewModel(GDKViewModel applicationViewModel, GDKGameLobbyController lobbyController) {
        try {
            if (lobbyController != null) {
                lobbyController.setViewModel(applicationViewModel);
            } else {
                throw new RuntimeException("Lobby controller is null - cannot wire up ViewModel");
            }
        } catch (Exception wiringError) {
            throw new RuntimeException("Failed to wire up controller with ViewModel", wiringError);
        }
    }
} 

