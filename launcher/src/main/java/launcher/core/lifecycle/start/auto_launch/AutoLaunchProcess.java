package launcher.core.lifecycle.start.auto_launch;

import gdk.api.GameModule;
import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.lobby.GDKViewModel;
import launcher.features.module_handling.module_finding.ModuleDiscovery;
import launcher.ui_areas.lobby.lifecycle.init.InitializeLobbyUIForAutoLaunch;

/**
 * Handles the auto-launch flow for the GDK application.
 * 
 * Auto-launch attempts to restore and launch a previously selected game
 * using saved configuration data. If successful, the application skips
 * the standard GDK interface and goes directly to the game.
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public final class AutoLaunchProcess {

    private AutoLaunchProcess() {
        // Utility class - prevent instantiation
    }

    /**
     * Attempts to auto-launch a game from saved state.
     * 
     * This method performs the following steps:
     * 1. Loads and validates saved auto-launch data
     * 2. Finds and loads the selected game module
     * 3. Creates and configures the controller/viewmodel
     * 4. Configures the primary stage for the game
     * 5. Sets up callback for returning to normal GDK
     * 6. Launches the game with saved configuration
     * 
     * @param primaryApplicationStage The primary JavaFX stage
     * @param normalLaunchCallback Callback to execute normal launch if auto-launch fails
     * @return true if auto-launch was successful, false otherwise
     */
    public static boolean launch(Stage primaryApplicationStage, Runnable normalLaunchCallback) {
        try {
            // Step 1: Load and validate saved auto-launch data
            InitializeLobbyUIForAutoLaunch.AutoLaunchData data = InitializeLobbyUIForAutoLaunch.loadAutoLaunchData();
            if (data == null) {
                return false;
            }
            
            Logging.info("Auto-launch: Attempting to launch " + data.getSelectedGameName() + " with saved JSON");

            // Step 2: Find and load the selected game module
            GameModule selectedModule = ModuleDiscovery.getModuleByName(data.getSelectedGameName());
            if (selectedModule == null) {
                Logging.info("Auto-launch: Selected game module not found: " + data.getSelectedGameName());
                return false;
            }

            // Step 3: Create and configure controller/viewmodel
            InitializeLobbyUIForAutoLaunch.AutoLaunchComponents components =
                InitializeLobbyUIForAutoLaunch.createAutoLaunchComponents(primaryApplicationStage);
            GDKGameLobbyController controller = components.getController();
            GDKViewModel viewModel = components.getViewModel();
            
            // Step 4: Configure the primary stage for the game
            InitializeLobbyUIForAutoLaunch.configureStageForGame(primaryApplicationStage, selectedModule);
            
            // Step 5: Set up callback for returning to normal GDK
            viewModel.setReturnToNormalGDKCallback(() -> {
                InitializeLobbyUIForAutoLaunch.returnToNormalGDK(controller, normalLaunchCallback);
            });
            
            // Step 6: Launch the game with saved configuration
            if (InitializeLobbyUIForAutoLaunch.launchGame(selectedModule, data.getSavedJson(), viewModel)) {
                Logging.info("Auto-launch: Successfully launched " + data.getSelectedGameName() + 
                    " using ViewModel (consistent with normal mode)");
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            Logging.error("Auto-launch failed with error: " + e.getMessage(), e);
            return false;
        }
    }
}

