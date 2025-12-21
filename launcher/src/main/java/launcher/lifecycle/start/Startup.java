package launcher.lifecycle.start;

import gdk.internal.Logging;
import gdk.api.GameModule;
import gdk.internal.MessagingBridge;
import launcher.utils.AutoLaunchUtil;
import launcher.utils.module.ModuleCompiler;
import launcher.utils.module.ModuleDiscovery;
import launcher.utils.path.FilePaths;

import javafx.stage.Stage;
import javafx.application.Platform;

import launcher.gui.GDKGameLobbyController;
import launcher.lifecycle.start.startup_window.StartupWindowManager;
import launcher.lifecycle.start.gui.UIInitializer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            if (AutoLaunchUtil.isAutoLaunchEnabled() && attemptAutoLaunch(primaryApplicationStage)) {
                Logging.info("Auto-launch successful");
                
                // Keep the primary stage alive but hidden to prevent application shutdown
                primaryApplicationStage.setTitle("GDK (Auto-Launch Mode)");
                primaryApplicationStage.hide(); // Hide the stage completely instead of showing it

                return; // Exit startup process, so we dont execute the normal startup process
            }

            // Normal GDK startup process
            Logging.info("Auto-launch failed or disabled - proceeding with normal startup");
            startNormalGDK(primaryApplicationStage);

        } catch (Exception startupError) {
            Logging.error("GDK application startup failed: " + startupError.getMessage(), startupError);
            throw new RuntimeException("Failed to start GDK application", startupError);
        }
    }

    /**
     * Attempt to auto-launch a game from saved state
     */
    private static boolean attemptAutoLaunch(Stage primaryApplicationStage) {
        try {
            // Check if required files exist
            if (!Files.exists(Paths.get(FilePaths.JSON_PERSISTENCE_FILE))) {
                Logging.info("Auto-launch: No saved JSON found");
                return false;
            }
            if (!Files.exists(Paths.get(FilePaths.SELECTED_GAME_FILE))) {
                Logging.info("Auto-launch: No saved game selection found");
                return false;
            }

            // Load saved JSON and game selection
            String savedJson = Files.readString(Paths.get(FilePaths.JSON_PERSISTENCE_FILE)).trim();
            String selectedGameName = Files.readString(Paths.get(FilePaths.SELECTED_GAME_FILE)).trim();
            
            if (savedJson.isEmpty() || selectedGameName.isEmpty()) {
                Logging.info("Auto-launch: Saved data is empty");
                return false;
            }

            Logging.info("Auto-launch: Attempting to launch " + selectedGameName + " with saved JSON");

            // Discover and load modules
            String modulesDirectoryPath = launcher.utils.path.PathUtil.getModulesDirectoryPath();
            File modulesDirectory = new File(modulesDirectoryPath);
            List<File> validModuleDirectories = ModuleDiscovery.getValidModuleDirectories(modulesDirectory);
            
            if (validModuleDirectories.isEmpty()) {
                Logging.info("Auto-launch: No valid modules found");
                return false;
            }

            List<GameModule> discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
            
            // Find the selected game module
            GameModule selectedModule = null;
            for (GameModule module : discoveredModules) {
                if (selectedGameName.equals(module.getMetadata().getGameName())) {
                    selectedModule = module;
                    break;
                }
            }

            if (selectedModule == null) {
                Logging.info("Auto-launch: Selected game module not found: " + selectedGameName);
                return false;
            }

            // Validate JSON syntax
            ObjectMapper jsonMapper = new ObjectMapper();
            try {
                jsonMapper.readValue(savedJson, Map.class);
            } catch (Exception e) {
                Logging.info("Auto-launch: Invalid JSON syntax in saved data");
                return false;
            }

            // Create controller instance for auto-launch (ensures consistency with normal mode)
            // Set mode to AUTO_LAUNCH so it knows to skip GUI setup
            launcher.gui.GDKGameLobbyController autoLaunchController = new launcher.gui.GDKGameLobbyController();
            autoLaunchController.setControllerMode(launcher.gui.GDKGameLobbyController.ControllerMode.AUTO_LAUNCH);
            launcher.gui.GDKViewModel autoLaunchViewModel = new launcher.gui.GDKViewModel();
            autoLaunchController.setViewModel(autoLaunchViewModel);
            
            // Create a separate stage for the game so closing it doesn't shut down the app
            // The primaryApplicationStage remains hidden to keep the app alive
            Stage gameStage = new Stage();
            gameStage.setTitle(selectedModule.getMetadata().getGameName());
            gameStage.setMinWidth(800);
            gameStage.setMinHeight(600);
            gameStage.setWidth(1200);
            gameStage.setHeight(900);
            gameStage.setOpacity(1.0);
            
            // Set the game stage as the primary stage for the ViewModel
            // This allows the ViewModel to manage the game lifecycle exactly like normal mode
            autoLaunchViewModel.setPrimaryStage(gameStage);
            
            // Use the utility class to prepare the game with configuration (identical to pressing Launch Game)
            Map<String, Object> jsonConfigurationData = launcher.utils.game.GameLaunchUtil.parseJsonString(savedJson);
            boolean configSuccess = launcher.utils.game.GameLaunchUtil.launchGameWithConfiguration(selectedModule, jsonConfigurationData, true);
            
            if (configSuccess) {
                // Use the ViewModel's handleLaunchGame method - this is the EXACT same code path as normal mode
                // It will set up all MessagingBridge subscriptions, server simulator, transcript recording, etc.
                autoLaunchViewModel.handleLaunchGame(selectedModule, savedJson);
                
                // Set up return to lobby functionality (pass controller as parameter)
                setupAutoLaunchReturnToLobby(gameStage, primaryApplicationStage, autoLaunchController);
                
                // Note: Lobby return callback is set up in setupAutoLaunchReturnToLobby()
                
                Logging.info("Auto-launch: Successfully launched " + selectedGameName + " using ViewModel (consistent with normal mode)");
                return true;
            } else {
                Logging.info("Auto-launch: Game configuration failed");
                return false;
            }
            
        } catch (Exception e) {
            Logging.error("Auto-launch failed with error: " + e.getMessage(), e);
            return false;
        }
    }
    
    
    /**
     * Set up the return to lobby functionality for auto-launched games.
     * Uses the same ViewModel cleanup logic as normal mode for consistency.
     * 
     * @param gameStage The stage hosting the game
     * @param primaryApplicationStage The hidden primary stage (keeps app alive)
     * @param controller The controller instance managing the game
     */
    private static void setupAutoLaunchReturnToLobby(Stage gameStage, Stage primaryApplicationStage, launcher.gui.GDKGameLobbyController controller) {
        // Set up a close handler that will clean up the game and generate transcript when closed
        // Then automatically start the normal GDK interface
        gameStage.setOnCloseRequest(event -> {
            Logging.info("Auto-launched game window closing - cleaning up via ViewModel");
            
            // Use ViewModel's cleanup method (same cleanup logic as normal mode)
            // Access ViewModel through controller, just like normal mode does
            if (controller != null && controller.getApplicationViewModel() != null) {
                controller.getApplicationViewModel().cleanupGameAndResources();
            }
            
            // Automatically start the normal GDK interface for a seamless experience
            Logging.info("Auto-launched game closed - starting normal GDK interface");
            Platform.runLater(() -> startNormalGDK(primaryApplicationStage));
        });
        
        // Set up the messaging bridge callback for games that use returnToLobby()
        MessagingBridge.setLobbyReturnCallback(() -> {
            Logging.info("Auto-launched game requested return to lobby - starting GDK");
            
            // Use ViewModel's cleanup method (same cleanup logic as normal mode)
            // Access ViewModel through controller, just like normal mode does
            if (controller != null && controller.getApplicationViewModel() != null) {
                controller.getApplicationViewModel().cleanupGameAndResources();
            }
            
            Platform.runLater(() -> startNormalGDK(primaryApplicationStage));
        });
    }
    
    /**
     * Start the normal GDK interface
     */
    private static void startNormalGDK(Stage primaryApplicationStage) {
        try {
            // 1. Progress window
            StartupWindowManager windowManager = StartupWindowManager.initializeWithCalculatedSteps();

            // 2. UI initialization
            GDKGameLobbyController lobbyController = UIInitializer.initialize(primaryApplicationStage, windowManager);
            
            // Controller mode is NORMAL by default (set when FXML loads)
            // No need to store it statically - it's managed by JavaFX Scene/Stage hierarchy

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
