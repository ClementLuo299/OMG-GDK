package launcher.ui_areas.lobby.lifecycle.init;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.json_processing.JsonUtil;
import launcher.features.game_launching.GameLaunchUtil;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.ui_areas.lobby.GDKViewModel;
import launcher.ui_areas.lobby.ControllerMode;
import launcher.features.file_handling.file_paths.GetOtherPaths;

import javafx.stage.Stage;
import javafx.application.Platform;
import launcher.ui_areas.lobby.lifecycle.init.viewmodel.ViewModelInitializer;
import launcher.ui_areas.lobby.lifecycle.init.viewmodel.WireViewModelToController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for auto-launch functionality.
 * 
 * <p>This class has a single responsibility: handling auto-launch functionality
 * for the GDK application, including reading configuration, creating components,
 * and launching games automatically.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Checking if auto-launch is enabled</li>
 *   <li>Loading saved auto-launch data (JSON and game selection)</li>
 *   <li>Creating controller and ViewModel components for auto-launch</li>
 *   <li>Configuring the stage for game display</li>
 *   <li>Launching games with saved configuration</li>
 *   <li>Handling return to normal GDK mode</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date December 20, 2025
 * @edited December 20, 2025
 * @since Beta 1.0
 */
public final class InitializeLobbyUIForAutoLaunch {
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private InitializeLobbyUIForAutoLaunch() {
        throw new AssertionError("AutoLaunchUtil should not be instantiated");
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Data class for auto-launch saved data.
     * 
     * <p>Contains the saved JSON configuration and selected game name
     * that are used to automatically launch a game on startup.
     */
    public static class AutoLaunchData {
        private final String savedJson;
        private final String selectedGameName;
        
        private AutoLaunchData(String savedJson, String selectedGameName) {
            this.savedJson = savedJson;
            this.selectedGameName = selectedGameName;
        }
        
        public String getSavedJson() {
            return savedJson;
        }
        
        public String getSelectedGameName() {
            return selectedGameName;
        }
    }
    
    // ==================== PUBLIC METHODS - AUTO-LAUNCH CHECK ====================
    
    /**
     * Checks if auto-launch functionality is enabled and all required data is available.
     * 
     * <p>Auto-launch is only considered enabled if:
     * <ol>
     *   <li>The auto-launch flag is set to true</li>
     *   <li>The JSON persistence file exists and is not empty</li>
     *   <li>The selected game file exists and is not empty</li>
     * </ol>
     * 
     * @return true if auto-launch is enabled and all required data is available, false otherwise
     */
    public static boolean isAutoLaunchEnabled() {
        try {
            // Check if the auto-launch flag is enabled
            Path autoLaunchFile = Paths.get(GetOtherPaths.AUTO_LAUNCH_ENABLED_FILE);
            if (!Files.exists(autoLaunchFile)) {
                return false; // Default to disabled
            }

            String flagContent = Files.readString(autoLaunchFile).trim();
            if (!Boolean.parseBoolean(flagContent)) {
                return false; // Auto-launch flag is disabled
            }

            // Check if required files exist
            Path jsonFile = Paths.get(GetOtherPaths.JSON_PERSISTENCE_FILE);
            Path selectedGameFile = Paths.get(GetOtherPaths.SELECTED_GAME_FILE);
            
            if (!Files.exists(jsonFile)) {
                Logging.info("Auto-launch: No saved JSON found");
                return false;
            }
            
            if (!Files.exists(selectedGameFile)) {
                Logging.info("Auto-launch: No saved game selection found");
                return false;
            }

            // Check if required files have non-empty content
            String savedJson = Files.readString(jsonFile).trim();
            String selectedGameName = Files.readString(selectedGameFile).trim();
            
            if (savedJson.isEmpty() || selectedGameName.isEmpty()) {
                Logging.info("Auto-launch: Saved data is empty");
                return false;
            }

            // All conditions met
            return true;
        } catch (Exception e) {
            Logging.error("Error checking auto-launch status: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== PUBLIC METHODS - DATA LOADING ====================
    
    /**
     * Loads the saved auto-launch data (JSON and selected game name).
     * 
     * <p>This method reads the saved JSON configuration and selected game name
     * from persistence files. It also validates that the JSON syntax is correct.
     * 
     * @return AutoLaunchData containing the saved JSON and game name, or null if ui_loading fails
     */
    public static AutoLaunchData loadAutoLaunchData() {
        try {
            Path jsonFile = Paths.get(GetOtherPaths.JSON_PERSISTENCE_FILE);
            Path selectedGameFile = Paths.get(GetOtherPaths.SELECTED_GAME_FILE);
            
            if (!Files.exists(jsonFile)) {
                Logging.info("Auto-launch: No saved JSON found");
                return null;
            }
            
            if (!Files.exists(selectedGameFile)) {
                Logging.info("Auto-launch: No saved game selection found");
                return null;
            }
            
            String savedJson = Files.readString(jsonFile).trim();
            String selectedGameName = Files.readString(selectedGameFile).trim();
            
            if (savedJson.isEmpty() || selectedGameName.isEmpty()) {
                Logging.info("Auto-launch: Saved data is empty");
                return null;
            }
            
            // Validate JSON syntax as part of ui_loading
            if (!JsonUtil.isValidJson(savedJson)) {
                Logging.info("Auto-launch: Invalid JSON syntax in saved data");
                return null;
            }
            
            return new AutoLaunchData(savedJson, selectedGameName);
        } catch (Exception e) {
            Logging.error("Error ui_loading auto-launch data: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Components needed for auto-launch.
     * 
     * <p>Contains the controller and ViewModel instances that are configured
     * for auto-launch mode, allowing games to run without the normal lobby interface.
     */
    public static class AutoLaunchComponents {
        private final GDKGameLobbyController controller;
        private final GDKViewModel viewModel;
        
        private AutoLaunchComponents(GDKGameLobbyController controller, GDKViewModel viewModel) {
            this.controller = controller;
            this.viewModel = viewModel;
        }
        
        public GDKGameLobbyController getController() {
            return controller;
        }
        
        public GDKViewModel getViewModel() {
            return viewModel;
        }
    }
    
    // ==================== PUBLIC METHODS - COMPONENT CREATION ====================
    
    /**
     * Creates and configures controller and ViewModel for auto-launch.
     * 
     * <p>This method creates a new GDKGameLobbyController and GDKViewModel,
     * sets the controller mode to AUTO_LAUNCH, and configures the ViewModel
     * with the primary stage for consistency with normal launch.
     * 
     * @param primaryApplicationStage The primary stage to set on the ViewModel
     * @return AutoLaunchComponents containing the configured controller and ViewModel
     */
    public static AutoLaunchComponents createAutoLaunchComponents(Stage primaryApplicationStage) {
        GDKGameLobbyController controller = new GDKGameLobbyController();
        controller.setControllerMode(ControllerMode.AUTO_LAUNCH);
        GDKViewModel viewModel = ViewModelInitializer.createForAutoLaunch(primaryApplicationStage);
        WireViewModelToController.wireUp(viewModel, controller);
        return new AutoLaunchComponents(controller, viewModel);
    }
    
    // ==================== PUBLIC METHODS - STAGE CONFIGURATION ====================
    
    /**
     * Configures the primary stage for auto-launch game display.
     * 
     * <p>This method sets the stage title, size, and opacity for displaying
     * the game in auto-launch mode.
     * 
     * @param primaryApplicationStage The primary stage to configure
     * @param gameModule The game module (used for setting the window title)
     */
    public static void configureStageForGame(Stage primaryApplicationStage, GameModule gameModule) {
        primaryApplicationStage.setTitle(gameModule.getMetadata().getGameName());
        primaryApplicationStage.setMinWidth(800);
        primaryApplicationStage.setMinHeight(600);
        primaryApplicationStage.setWidth(1200);
        primaryApplicationStage.setHeight(900);
        primaryApplicationStage.setOpacity(1.0);
        primaryApplicationStage.show();
    }
    
    // ==================== PUBLIC METHODS - GAME LAUNCHING ====================
    
    /**
     * Launches the game with configuration.
     * 
     * <p>This method handles the complete configuration and launch sequence:
     * <ol>
     *   <li>Parses the JSON configuration string</li>
     *   <li>Applies configuration to the game module</li>
     *   <li>Launches the game via the ViewModel</li>
     * </ol>
     * 
     * @param gameModule The game module to launch
     * @param savedJson The saved JSON configuration string
     * @param viewModel The ViewModel to use for launching
     * @return true if launch was successful, false otherwise
     */
    public static boolean launchGame(GameModule gameModule, String savedJson, GDKViewModel viewModel) {
        // Parse JSON configuration
        java.util.Map<String, Object> jsonConfigurationData = 
            GameLaunchUtil.parseJsonString(savedJson);
        
        // Prepare game with configuration
        boolean configSuccess = GameLaunchUtil.launchGameWithConfiguration(
            gameModule, jsonConfigurationData, true);
        
        if (!configSuccess) {
            Logging.info("Auto-launch: Game configuration failed");
            return false;
        }
        
        // Launch the game via ViewModel
        // This sets up all MessagingBridge subscriptions, server simulator, transcript recording, etc.
        viewModel.handleLaunchGame(gameModule, savedJson);
        
        return true;
    }
    
    // ==================== PUBLIC METHODS - MODE TRANSITION ====================
    
    /**
     * Handles returning to normal GDK from auto-launch mode.
     * 
     * <p>This method cleans up the current game and starts the normal GDK interface.
     * It uses the same cleanup logic as normal mode to ensure consistency.
     * 
     * @param controller The controller instance managing the game
     * @param startNormalGDKCallback Callback to init the normal GDK interface
     */
    public static void returnToNormalGDK(GDKGameLobbyController controller, Runnable startNormalGDKCallback) {
        // Use ViewModel's cleanup method (same cleanup logic as normal mode)
        if (controller != null && controller.getApplicationViewModel() != null) {
            controller.getApplicationViewModel().cleanupGameAndResources();
        }
        
        // Start the normal GDK interface (reuses the same primary stage)
        Platform.runLater(startNormalGDKCallback);
    }
}

