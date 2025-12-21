package launcher.lifecycle.start;

import gdk.internal.Logging;
import gdk.api.GameModule;
import launcher.utils.AutoLaunchUtil;
import launcher.utils.module.ModuleDiscovery;

import javafx.stage.Stage;

import launcher.gui.GDKGameLobbyController;
import launcher.gui.GDKViewModel;
import launcher.lifecycle.start.startup_window.StartupWindowManager;
import launcher.lifecycle.start.gui.UIInitializer;


/**
 * Orchestrates the startup process of the GDK application.
 * 
 * @authors Clement Luo
 * @date August 8, 2025
 * @edited December 20, 2025  
 * @since Beta 1.0
 */
public class Startup {

    /**
     * Main entry point for the GDK application startup process.
     * Orchestrates the startup sequence, checking for auto-launch functionality first,
     * and falling back to the normal GDK interface if auto-launch is disabled or fails.
     * 
     * @param primaryApplicationStage The primary JavaFX stage for the application
     * @throws RuntimeException if the startup process fails
     */
    public static void start(Stage primaryApplicationStage) {
        Logging.info("Beginning GDK application startup process");
        try {
            // Auto-launch
            if (AutoLaunchUtil.isAutoLaunchEnabled() && autoLaunch(primaryApplicationStage)) {
                Logging.info("Auto-launch successful");
                // Primary stage is now configured and showing the game
                return; // Exit startup process, so we dont execute the normal startup process
            }

            // Normal GDK startup process
            Logging.info("Auto-launch failed or disabled - proceeding with normal startup");
            normalLaunch(primaryApplicationStage);

        } catch (Exception startupError) {
            Logging.error("GDK application startup failed: " + startupError.getMessage(), startupError);
            throw new RuntimeException("Failed to start GDK application", startupError);
        }
    }

    /**
     * Auto-launch a game from saved state.
     */
    private static boolean autoLaunch(Stage primaryApplicationStage) {
        try {
            // Load and validate saved data
            AutoLaunchUtil.AutoLaunchData data = AutoLaunchUtil.loadAutoLaunchData();
            if (data == null) {
                return false;
            }
            
            Logging.info("Auto-launch: Attempting to launch " + data.getSelectedGameName() + " with saved JSON");

            // Find and load the selected game module
            GameModule selectedModule = ModuleDiscovery.findModuleByName(data.getSelectedGameName());
            if (selectedModule == null) {
                Logging.info("Auto-launch: Selected game module not found: " + data.getSelectedGameName());
                return false;
            }

            // Create and configure controller/viewmodel for auto-launch
            AutoLaunchUtil.AutoLaunchComponents components = AutoLaunchUtil.createAutoLaunchComponents();
            GDKGameLobbyController controller = components.getController();
            GDKViewModel viewModel = components.getViewModel();
            
            // Configure the primary stage for the game
            AutoLaunchUtil.configureStageForGame(primaryApplicationStage, selectedModule);
            viewModel.setPrimaryStage(primaryApplicationStage);
            
            // Set up callback for returning to normal GDK 
            viewModel.setReturnToNormalGDKCallback(() -> {
                AutoLaunchUtil.returnToNormalGDK(controller, () -> normalLaunch(primaryApplicationStage));
            });
            
            // Launch the game with configuration
            if (AutoLaunchUtil.launchGame(selectedModule, data.getSavedJson(), viewModel)) {
                Logging.info("Auto-launch: Successfully launched " + data.getSelectedGameName() + " using ViewModel (consistent with normal mode)");
                return true;
            } else {
                return false;
            }
            
        } catch (Exception e) {
            Logging.error("Auto-launch failed with error: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Start the normal GDK interface
     */
    private static void normalLaunch(Stage primaryApplicationStage) {
        try {
            // 1. Progress window
            StartupWindowManager windowManager = StartupWindowManager.initializeWithCalculatedSteps();

            // 2. UI initialization
            GDKGameLobbyController lobbyController = UIInitializer.initialize(primaryApplicationStage, windowManager);

            // 3. Check readiness and show main stage
            StartupOperations.ensureUIReady(primaryApplicationStage, lobbyController, windowManager);
            StartupOperations.showMainStageWithFade(primaryApplicationStage, windowManager);

            Logging.info("GDK application startup completed successfully");
        } catch (Exception startupError) {
            Logging.error("GDK application startup failed: " + startupError.getMessage(), startupError);
            throw new RuntimeException("Failed to start GDK application", startupError);
        }
    }
}
