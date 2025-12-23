package launcher.lifecycle.start.launch_modes;

import gdk.api.GameModule;
import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.gui.GDKGameLobbyController;
import launcher.gui.GDKViewModel;
import launcher.utils.AutoLaunchUtil;
import launcher.utils.module.ModuleDiscovery;

/**
 * Handles the auto-launch path for the GDK application.
 * 
 * Auto-launch attempts to restore and launch a previously selected game
 * using saved configuration data. If successful, the application skips
 * the standard GDK interface and goes directly to the game.
 * 
 * @author Clement Luo
 * @date December 22, 2025
 * @since Beta 1.0
 */
public final class AutoLaunch {

    private AutoLaunch() {
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
            AutoLaunchUtil.AutoLaunchData data = AutoLaunchUtil.loadAutoLaunchData();
            if (data == null) {
                return false;
            }
            
            Logging.info("Auto-launch: Attempting to launch " + data.getSelectedGameName() + " with saved JSON");

            // Step 2: Find and load the selected game module
            GameModule selectedModule = ModuleDiscovery.findModuleByName(data.getSelectedGameName());
            if (selectedModule == null) {
                Logging.info("Auto-launch: Selected game module not found: " + data.getSelectedGameName());
                return false;
            }

            // Step 3: Create and configure controller/viewmodel
            AutoLaunchUtil.AutoLaunchComponents components = 
                AutoLaunchUtil.createAutoLaunchComponents(primaryApplicationStage);
            GDKGameLobbyController controller = components.getController();
            GDKViewModel viewModel = components.getViewModel();
            
            // Step 4: Configure the primary stage for the game
            AutoLaunchUtil.configureStageForGame(primaryApplicationStage, selectedModule);
            
            // Step 5: Set up callback for returning to normal GDK
            viewModel.setReturnToNormalGDKCallback(() -> {
                AutoLaunchUtil.returnToNormalGDK(controller, normalLaunchCallback);
            });
            
            // Step 6: Launch the game with saved configuration
            if (AutoLaunchUtil.launchGame(selectedModule, data.getSavedJson(), viewModel)) {
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

