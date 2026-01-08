package launcher.ui_areas.lobby.lifecycle.startup;

import gdk.api.GameModule;
import gdk.internal.Logging;
import javafx.stage.Stage;
import launcher.features.module_handling.module_finding.ModuleDiscovery;
import launcher.features.game_launching.LaunchGame;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.lobby.GDKViewModel;
import launcher.ui_areas.lobby.ControllerMode;
import launcher.ui_areas.lobby.lifecycle.startup.ui_initialization.InitializeLobbyUIForStandardLaunch;
import launcher.ui_areas.lobby.lifecycle.startup.ui_initialization.InitializeLobbyUIForAutoLaunch;
import launcher.ui_areas.lobby.lifecycle.startup.module_loading.ModuleLoadingThread;
import launcher.ui_areas.startup_window.StartupWindow;

/**
 * Single entry point for lobby startup operations.
 * Handles both standard launch and auto-launch modes.
 * 
 * @author Clement Luo
 * @date January 8, 2026
 * @edited January 8, 2026
 * @since Beta 1.0
 */
public final class LobbyStartup {
    
    private LobbyStartup() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if auto-launch is enabled and all required data is available.
     * 
     * @return true if auto-launch is enabled, false otherwise
     */
    public static boolean isAutoLaunchEnabled() {
        return InitializeLobbyUIForAutoLaunch.isAutoLaunchEnabled();
    }
    
    /**
     * Starts the standard lobby launch process.
     * Initializes the UI and starts module loading in the background.
     * 
     * @param primaryApplicationStage The primary JavaFX stage
     * @param windowManager The startup window manager
     * @return The initialized lobby controller
     */
    public static GDKGameLobbyController startStandardLaunch(Stage primaryApplicationStage, StartupWindow windowManager) {
        Logging.info("Starting standard lobby launch");
        
        // Initialize the user interface
        GDKGameLobbyController lobbyController = 
            InitializeLobbyUIForStandardLaunch.initialize(primaryApplicationStage);
        
        // Start loading modules in the background
        ModuleLoadingThread.start(primaryApplicationStage, lobbyController, windowManager);
        
        return lobbyController;
    }
    
    /**
     * Attempts to start auto-launch mode.
     * 
     * @param primaryApplicationStage The primary JavaFX stage
     * @param normalLaunchCallback Callback to execute if auto-launch fails
     * @return true if auto-launch was successful, false otherwise
     */
    public static boolean startAutoLaunch(Stage primaryApplicationStage, Runnable normalLaunchCallback) {
        try {
            // Load and validate saved auto-launch data
            InitializeLobbyUIForAutoLaunch.AutoLaunchData data = InitializeLobbyUIForAutoLaunch.loadAutoLaunchData();
            if (data == null) {
                return false;
            }
            
            Logging.info("Auto-launch: Attempting to launch " + data.getSelectedGameName() + " with saved JSON");
            
            // Find and load the selected game module
            GameModule selectedModule = ModuleDiscovery.getModuleByName(data.getSelectedGameName());
            if (selectedModule == null) {
                Logging.info("Auto-launch: Selected game module not found: " + data.getSelectedGameName());
                return false;
            }
            
            // Create and configure controller/viewmodel
            InitializeLobbyUIForAutoLaunch.AutoLaunchComponents components =
                InitializeLobbyUIForAutoLaunch.createAutoLaunchComponents(primaryApplicationStage);
            GDKGameLobbyController controller = components.getController();
            GDKViewModel viewModel = components.getViewModel();
            
            // Configure the primary stage for the game
            InitializeLobbyUIForAutoLaunch.configureStageForGame(primaryApplicationStage, selectedModule);
            
            // Set up callback for returning to normal GDK
            viewModel.setReturnToNormalGDKCallback(() -> {
                InitializeLobbyUIForAutoLaunch.returnToNormalGDK(controller, normalLaunchCallback);
            });
            
            // Launch the game with saved configuration
            if (InitializeLobbyUIForAutoLaunch.launchGame(selectedModule, data.getSavedJson(), viewModel)) {
                Logging.info("Auto-launch: Successfully launched " + data.getSelectedGameName());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            Logging.error("Auto-launch failed with error: " + e.getMessage(), e);
            return false;
        }
    }
}

