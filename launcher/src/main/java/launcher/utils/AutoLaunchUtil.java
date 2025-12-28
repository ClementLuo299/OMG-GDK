package launcher.utils;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.gui.lobby.GDKGameLobbyController;
import launcher.gui.lobby.GDKViewModel;
import launcher.utils.path.FilePaths;

import javafx.stage.Stage;
import javafx.application.Platform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for auto-launch functionality.
 * Handles reading and writing auto-launch configuration settings.
 * 
 * @authors Clement Luo
 * @date December 20, 2025
 * @edited December 20, 2025
 * @since Beta 1.0
 */
public final class AutoLaunchUtil {
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private AutoLaunchUtil() {
        throw new AssertionError("AutoLaunchUtil should not be instantiated");
    }
    
    /**
     * Data class for auto-launch saved data.
     * Contains the saved JSON configuration and selected game name.
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
    
    /**
     * Check if auto-launch functionality is enabled and all required data is available.
     * Auto-launch is only considered enabled if:
     * 1. The auto-launch flag is set to true
     * 2. The JSON persistence file exists and is not empty
     * 3. The selected game file exists and is not empty
     * 
     * @return true if auto-launch is enabled and all required data is available, false otherwise
     */
    public static boolean isAutoLaunchEnabled() {
        try {
            // Check if the auto-launch flag is enabled
            Path autoLaunchFile = Paths.get(FilePaths.AUTO_LAUNCH_ENABLED_FILE);
            if (!Files.exists(autoLaunchFile)) {
                return false; // Default to disabled
            }

            String flagContent = Files.readString(autoLaunchFile).trim();
            if (!Boolean.parseBoolean(flagContent)) {
                return false; // Auto-launch flag is disabled
            }

            // Check if required files exist
            Path jsonFile = Paths.get(FilePaths.JSON_PERSISTENCE_FILE);
            Path selectedGameFile = Paths.get(FilePaths.SELECTED_GAME_FILE);
            
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
    
    /**
     * Load the saved auto-launch data (JSON and selected game name).
     * 
     * @return AutoLaunchData containing the saved JSON and game name, or null if loading fails
     */
    public static AutoLaunchData loadAutoLaunchData() {
        try {
            Path jsonFile = Paths.get(FilePaths.JSON_PERSISTENCE_FILE);
            Path selectedGameFile = Paths.get(FilePaths.SELECTED_GAME_FILE);
            
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
            
            // Validate JSON syntax as part of loading
            if (!JsonUtil.isValidJson(savedJson)) {
                Logging.info("Auto-launch: Invalid JSON syntax in saved data");
                return null;
            }
            
            return new AutoLaunchData(savedJson, selectedGameName);
        } catch (Exception e) {
            Logging.error("Error loading auto-launch data: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Components needed for auto-launch (controller and viewmodel).
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
    
    /**
     * Create and configure controller and viewmodel for auto-launch.
     * Sets the primary stage on the viewmodel for consistency with normal launch.
     * 
     * @param primaryApplicationStage The primary stage to set on the viewmodel
     * @return AutoLaunchComponents containing the configured controller and viewmodel
     */
    public static AutoLaunchComponents createAutoLaunchComponents(Stage primaryApplicationStage) {
        GDKGameLobbyController controller = new GDKGameLobbyController();
        controller.setControllerMode(GDKGameLobbyController.ControllerMode.AUTO_LAUNCH);
        GDKViewModel viewModel = new GDKViewModel();
        viewModel.setPrimaryStage(primaryApplicationStage);
        controller.setViewModel(viewModel);
        return new AutoLaunchComponents(controller, viewModel);
    }
    
    /**
     * Configure the primary stage for auto-launch game display.
     * 
     * @param primaryApplicationStage The primary stage to configure
     * @param gameModule The game module (for title)
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
    
    /**
     * Launch the game with configuration.
     * This method handles the configuration and launch sequence.
     * 
     * @param gameModule The game module to launch
     * @param savedJson The saved JSON configuration string
     * @param viewModel The ViewModel to use for launching
     * @return true if launch was successful, false otherwise
     */
    public static boolean launchGame(GameModule gameModule, String savedJson, GDKViewModel viewModel) {
        // Parse JSON configuration
        java.util.Map<String, Object> jsonConfigurationData = 
            launcher.utils.game.GameLaunchUtil.parseJsonString(savedJson);
        
        // Prepare game with configuration
        boolean configSuccess = launcher.utils.game.GameLaunchUtil.launchGameWithConfiguration(
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
    
    /**
     * Handle returning to normal GDK from auto-launch mode.
     * Cleans up the game and starts the normal GDK interface.
     * 
     * @param controller The controller instance managing the game
     * @param startNormalGDKCallback Callback to start the normal GDK interface
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

