package launcher.lifecycle.start.gui;

import javafx.scene.Scene;
import javafx.stage.Stage;
import launcher.gui.lobby.ui_logic.GDKGameLobbyController;
import launcher.gui.lobby.business.GDKViewModel;
import launcher.lifecycle.start.gui.initialization.SceneLoader;
import launcher.lifecycle.start.gui.initialization.StageInitializer;
import launcher.lifecycle.start.gui.optimization.PerformanceOptimizer;
import launcher.lifecycle.start.startup_window.StartupWindowManager;

/**
 * Initializes the main user interface components.
 * Orchestrates the UI setup process by coordinating scene loading, ViewModel creation,
 * stage configuration, and controller-ViewModel wiring.
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public final class UIInitializer {

    private UIInitializer() {}

    /**
     * Initializes the main user interface components for the GDK application.
     * Orchestrates the complete UI setup process including loading the FXML scene,
     * creating the ViewModel, configuring the primary stage, and wiring up the controller.
     * 
     * @param primaryApplicationStage The primary JavaFX stage for the application
     * @param windowManager The startup window manager for progress updates
     * @return The initialized GDKGameLobbyController
     */
    public static GDKGameLobbyController initialize(Stage primaryApplicationStage, StartupWindowManager windowManager) {
        windowManager.updateProgress(1, "Loading user interface");
        
        // Load the main scene from FXML
        SceneLoader.SceneLoadResult loadResult = SceneLoader.loadMainScene();
        Scene mainLobbyScene = loadResult.scene;
        GDKGameLobbyController lobbyController = loadResult.controller;
        
        // Create and configure the ViewModel
        GDKViewModel applicationViewModel = createViewModel(primaryApplicationStage);
        applicationViewModel.setMainLobbyScene(mainLobbyScene);
        
        // Initialize the primary stage
        StageInitializer.initialize(primaryApplicationStage, mainLobbyScene);
        
        // Apply performance optimizations
        PerformanceOptimizer.optimize(primaryApplicationStage, mainLobbyScene);
        
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