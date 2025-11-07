package launcher.lifecycle.start;

import gdk.infrastructure.Logging;
import gdk.api.GameModule;
import gdk.infrastructure.MessagingBridge;
import launcher.utils.ModuleCompiler;
import launcher.utils.ModuleDiscovery;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.application.Platform;

import launcher.gui.GDKGameLobbyController;
import launcher.lifecycle.start.startup_window.StartupWindowManager;
import launcher.lifecycle.start.gui.UIInitializer;
import launcher.gui.ServerSimulatorController;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXMLLoader;
import java.util.HashMap;

/**
 * Orchestrates the startup process of the GDK application.
 * 
 * @authors Clement Luo
 * @date August 8, 2025
 * @edited August 12, 2025  
 * @since 1.0
 */
public class Startup {

    // File paths for auto-launch functionality
    private static final String JSON_PERSISTENCE_FILE = "saved/gdk-json-persistence.txt";
    private static final String SELECTED_GAME_FILE = "saved/gdk-selected-game.txt";
    private static final String AUTO_LAUNCH_ENABLED_FILE = "saved/gdk-auto-launch-enabled.txt";

    public static void start(Stage primaryApplicationStage) {
        Logging.info("Starting GDK application startup process");
        try {
            // Check if auto-launch is enabled and attempt to launch game directly
            if (isAutoLaunchEnabled() && attemptAutoLaunch(primaryApplicationStage)) {
                Logging.info("Auto-launch successful - bypassing GDK interface");
                            // Keep the primary stage alive but hidden to prevent application shutdown
            primaryApplicationStage.setTitle("GDK (Auto-Launch Mode)");
            primaryApplicationStage.hide(); // Hide the stage completely instead of showing it
                return; // Exit startup process, game is running
            }

            // Normal GDK startup process
            Logging.info("Auto-launch failed or disabled - proceeding with normal GDK startup");
            startNormalGDK(primaryApplicationStage);

        } catch (Exception startupError) {
            Logging.error("GDK application startup failed: " + startupError.getMessage(), startupError);
            throw new RuntimeException("Failed to start GDK application", startupError);
        }
    }

    /**
     * Check if auto-launch functionality is enabled
     */
    private static boolean isAutoLaunchEnabled() {
        try {
            Path autoLaunchFile = Paths.get(AUTO_LAUNCH_ENABLED_FILE);
            if (!Files.exists(autoLaunchFile)) {
                return false; // Default to disabled
            }
            String content = Files.readString(autoLaunchFile).trim();
            return Boolean.parseBoolean(content);
        } catch (Exception e) {
            Logging.error("Error checking auto-launch status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Attempt to auto-launch a game from saved state
     */
    private static boolean attemptAutoLaunch(Stage primaryApplicationStage) {
        try {
            // Check if required files exist
            if (!Files.exists(Paths.get(JSON_PERSISTENCE_FILE))) {
                Logging.info("Auto-launch: No saved JSON found");
                return false;
            }
            if (!Files.exists(Paths.get(SELECTED_GAME_FILE))) {
                Logging.info("Auto-launch: No saved game selection found");
                return false;
            }

            // Load saved JSON and game selection
            String savedJson = Files.readString(Paths.get(JSON_PERSISTENCE_FILE)).trim();
            String selectedGameName = Files.readString(Paths.get(SELECTED_GAME_FILE)).trim();
            
            if (savedJson.isEmpty() || selectedGameName.isEmpty()) {
                Logging.info("Auto-launch: Saved data is empty");
                return false;
            }

            Logging.info("Auto-launch: Attempting to launch " + selectedGameName + " with saved JSON");

            // Discover and load modules
            String modulesDirectoryPath = launcher.GDKApplication.MODULES_DIRECTORY_PATH;
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
                if (selectedGameName.equals(module.getGameName())) {
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
            Map<String, Object> jsonData;
            try {
                jsonData = jsonMapper.readValue(savedJson, Map.class);
            } catch (Exception e) {
                Logging.info("Auto-launch: Invalid JSON syntax in saved data");
                return false;
            }

            // Configure the primary stage for proper game display
            primaryApplicationStage.setTitle(selectedModule.getGameName());
            primaryApplicationStage.setMinWidth(800);
            primaryApplicationStage.setMinHeight(600);
            primaryApplicationStage.setWidth(1200);
            primaryApplicationStage.setHeight(900);
            primaryApplicationStage.setOpacity(1.0);

            // Use the utility class to prepare the game with configuration (identical to pressing Launch Game)
            Map<String, Object> jsonConfigurationData = launcher.utils.GameLaunchUtil.parseJsonString(savedJson);
            boolean configSuccess = launcher.utils.GameLaunchUtil.launchGameWithConfiguration(selectedModule, jsonConfigurationData, true);
            
            if (configSuccess) {
                // Create a separate stage for the game so closing it doesn't shut down the app
                Stage gameStage = new Stage();
                gameStage.setTitle(selectedModule.getGameName());
                gameStage.setMinWidth(800);
                gameStage.setMinHeight(600);
                gameStage.setWidth(1200);
                gameStage.setHeight(900);
                gameStage.setOpacity(1.0);
                
                // Now launch the game using the separate stage
                Scene gameScene = selectedModule.launchGame(gameStage);
                
                if (gameScene != null) {
                    // Game launched successfully
                    gameStage.setScene(gameScene);
                    gameStage.show();
                    
                    // Create server simulator for the auto-launched game (using normal GDK method)
                    createServerSimulatorForAutoLaunch(gameStage, selectedModule);
                    
                    // Set up return to lobby functionality
                    setupAutoLaunchReturnToLobby(gameStage, selectedModule);
                    
                    // Start transcript recording with game metadata
                    String gameName = selectedModule.getMetadata().getGameName();
                    String gameVersion = selectedModule.getMetadata().getGameVersion();
                    launcher.utils.TranscriptRecorder.startSession(gameName, gameVersion);
                    
                    // Set up the lobby return callback for games
                    MessagingBridge.setLobbyReturnCallback(() -> {
                        Logging.info("Auto-launched game requested return to lobby - starting GDK");
                        Platform.runLater(() -> startNormalGDK(primaryApplicationStage));
                    });
                    
                    Logging.info("Auto-launch: Successfully launched " + selectedGameName);
                    return true;
                } else {
                    Logging.info("Auto-launch: Game returned null scene");
                    return false;
                }
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
     * Create a minimal controller instance for auto-launch functionality
     */
    private static launcher.gui.GDKGameLobbyController createMinimalControllerForAutoLaunch() {
        try {
            // Create a new controller instance
            launcher.gui.GDKGameLobbyController controller = new launcher.gui.GDKGameLobbyController();
            
            // Create a minimal ViewModel for the controller
            launcher.gui.GDKViewModel viewModel = new launcher.gui.GDKViewModel();
            viewModel.setPrimaryStage(new Stage()); // Temporary stage
            
            // Set the ViewModel in the controller
            controller.setViewModel(viewModel);
            
            // Initialize the controller to ensure jsonDataMapper is set up
            controller.initialize(null, null);
            
            return controller;
        } catch (Exception e) {
            Logging.error("Auto-launch: Error creating controller: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Create and configure the server simulator for auto-launched games
     */
    private static void createServerSimulatorForAutoLaunch(Stage primaryApplicationStage, GameModule gameModule) {
        try {
            // Create server simulator stage
            Stage serverSimulatorStage = new Stage();
            serverSimulatorStage.setTitle("Server Simulator");
            serverSimulatorStage.setWidth(600);
            serverSimulatorStage.setHeight(400);
            
            // Load server simulator FXML exactly like normal GDK does
            URL fxmlResourceUrl = launcher.gui.GDKViewModel.class.getResource("/server-simulator/ServerSimulator.fxml");
            if (fxmlResourceUrl == null) {
                Logging.error("Server simulator FXML not found");
                return;
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlResourceUrl);
            Scene serverSimulatorScene = new Scene(fxmlLoader.load());
            ServerSimulatorController serverSimulatorController = fxmlLoader.getController();
            
            // Apply CSS styling exactly like normal GDK does
            URL cssResourceUrl = launcher.gui.GDKViewModel.class.getResource("/server-simulator/server-simulator.css");
            if (cssResourceUrl != null) {
                serverSimulatorScene.getStylesheets().add(cssResourceUrl.toExternalForm());
            }
            
            // Configure the server simulator stage exactly like normal GDK
            serverSimulatorStage.setScene(serverSimulatorScene);
            serverSimulatorStage.show();
            
            // Set up message handling exactly like normal GDK does
            if (serverSimulatorController != null) {
                serverSimulatorController.setMessageHandler(messageText -> {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        java.util.Map<String, Object> messageMap;
                        try {
                            messageMap = mapper.readValue(messageText, java.util.Map.class);
                        } catch (Exception parseError) {
                            // Treat as plain chat text
                            messageMap = new java.util.HashMap<>();
                            messageMap.put("function", "message");
                            messageMap.put("from", "server");
                            messageMap.put("text", messageText);
                        }
                        
                        // Record to transcript
                        launcher.utils.TranscriptRecorder.recordToGame(messageMap);
                        
                        // Send message to game
                        java.util.Map<String, Object> response = gameModule.handleMessage(messageMap);
                        if (response == null) {
                            response = new java.util.HashMap<>();
                            response.put("function", "ack");
                            response.put("status", "ok");
                            Object of = messageMap.get("function");
                            if (of != null) response.put("of", of);
                            response.put("timestamp", java.time.Instant.now().toString());
                        }
                        
                        // Record from game
                        launcher.utils.TranscriptRecorder.recordFromGame(response);
                        String responseText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
                        serverSimulatorController.addReceivedMessageToDisplay(responseText);
                    } catch (Exception e) {
                        serverSimulatorController.addReceivedMessageToDisplay("ERROR: " + e.getMessage());
                    }
                });
                
                // Set up message bridge consumer for game messages exactly like normal GDK
                MessagingBridge.setConsumer(msg -> {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(msg);
                        serverSimulatorController.addReceivedMessageToDisplay(pretty);
                        
                        // Send confirmation back to the game
                        java.util.Map<String, Object> ack = new java.util.HashMap<>();
                        ack.put("function", "ack");
                        ack.put("status", "ok");
                        Object of = (msg != null) ? msg.get("function") : null;
                        if (of != null) ack.put("of", of);
                        ack.put("timestamp", java.time.Instant.now().toString());
                        gameModule.handleMessage(ack);
                    } catch (Exception ignored) {
                    }
                });
                
                // Also mirror messages to lobby JSON output exactly like normal GDK
                MessagingBridge.addConsumer(msg -> {
                    try {
                        // Record the message to the transcript
                        launcher.utils.TranscriptRecorder.recordFromGame(msg);
                        
                        // Present end message or others back to the lobby UI if needed
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(msg);
                        if (serverSimulatorController != null) {
                            serverSimulatorController.addReceivedMessageToDisplay(pretty);
                        }
                    } catch (Exception ignored) {}
                });
                
                // Set up close handler exactly like normal GDK - just close the simulator, don't return to GDK
                serverSimulatorStage.setOnCloseRequest(event -> {
                    Logging.info("🔒 Server simulator window closing");
                    serverSimulatorController.onClose();
                    // Don't trigger return to GDK - just close the simulator
                });
            }
            
            // Store the server simulator stage reference so we can close it when the game closes
            // We'll use a custom property on the primary stage to store this reference
            primaryApplicationStage.setUserData(serverSimulatorStage);
            
            Logging.info("🔧 Server simulator created successfully for auto-launched game");
        } catch (Exception e) {
            Logging.error("❌ Error creating server simulator for auto-launch: " + e.getMessage(), e);
        }
    }
    
    /**
     * Set up the return to lobby functionality for auto-launched games
     */
    private static void setupAutoLaunchReturnToLobby(Stage primaryApplicationStage, GameModule gameModule) {
        // Set up a close handler that will clean up the game and generate transcript when closed
        // Then automatically start the normal GDK interface
        primaryApplicationStage.setOnCloseRequest(event -> {
            Logging.info("Auto-launched game window closing - cleaning up and generating transcript");
            
            // Close the server simulator if it exists
            Object userData = primaryApplicationStage.getUserData();
            if (userData instanceof Stage) {
                Stage serverSimulatorStage = (Stage) userData;
                Logging.info("🔧 Closing server simulator for auto-launched game");
                serverSimulatorStage.close();
            }
            
            // Create an end message to trigger transcript generation
            Map<String, Object> endMessage = new HashMap<>();
            endMessage.put("function", "end");
            endMessage.put("reason", "user_closed_game_window");
            endMessage.put("timestamp", java.time.Instant.now().toString());
            
            // Record the end message - this will automatically trigger transcript generation
            // recordFromGame() calls endSessionIfEndDetected() internally, so don't call it manually
            launcher.utils.TranscriptRecorder.recordFromGame(endMessage);
            
            // Clean up the game
            try {
                gameModule.stopGame();
                Logging.info("🎮 Auto-launched game stopped successfully");
            } catch (Exception e) {
                Logging.error("❌ Error stopping auto-launched game: " + e.getMessage());
            }
            
            // Automatically start the normal GDK interface for a seamless experience
            Logging.info("Auto-launched game closed - starting normal GDK interface");
            Platform.runLater(() -> startNormalGDK(primaryApplicationStage));
        });
        
        // Only set up the messaging bridge callback for games that use returnToLobby()
        // This is the ONLY way to return to GDK now
        MessagingBridge.setLobbyReturnCallback(() -> {
            Logging.info("Auto-launched game requested return to lobby - starting GDK");
            
            // Close the server simulator if it exists
            Object userData = primaryApplicationStage.getUserData();
            if (userData instanceof Stage) {
                Stage serverSimulatorStage = (Stage) userData;
                Logging.info("🔧 Closing server simulator for auto-launched game");
                serverSimulatorStage.close();
            }
            
            Platform.runLater(() -> startNormalGDK(primaryApplicationStage));
        });
        
        // Note: Now the game close handler only cleans up and generates transcript
        // It does NOT automatically return to GDK - only explicit returnToLobby() calls do
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