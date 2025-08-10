package launcher.gui;

import gdk.GameModule;
import gdk.Logging;
import launcher.utils.ModuleDiscovery;
import launcher.utils.ModuleCompiler;

import java.io.File;
import launcher.gui.ServerSimulatorController;
import launcher.GDKApplication;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.util.List;

/**
 * ViewModel for the GDK application that manages application state and business logic.
 * 
 * This class serves as the central data and logic layer for the GDK application.
 * It manages the application state, handles game module discovery and loading,
 * coordinates between the UI and game modules, and manages the server simulator.
 * 
 * Key responsibilities:
 * - Handle game module discovery and loading
 * - Coordinate game launching and management
 * - Manage server simulator lifecycle
 * - Handle application cleanup and shutdown
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @edited August 9, 2025  
 * @since 1.0
 */
public class GDKViewModel {

    // ==================== DEPENDENCIES ====================
    
    // Module loader functionality is now handled by static methods in ModuleDiscovery and ModuleCompiler
    
    /**
     * The primary JavaFX stage that hosts the main application window
     */
    private Stage primaryApplicationStage;

    // ==================== GAME STATE ====================
    
    /**
     * The currently running game module, if any
     */
    private GameModule currentlyRunningGame;

    // ==================== SERVER SIMULATOR STATE ====================
    
    /**
     * The stage hosting the server simulator window
     */
    private Stage serverSimulatorStage;
    
    /**
     * The controller for the server simulator interface
     */
    private ServerSimulatorController serverSimulatorController;

    private Scene mainLobbyScene;

    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a new GDK ViewModel.
     * Module discovery and loading is now handled by static methods.
     */
    public GDKViewModel() {
        // No module loader instance needed - using static methods now
    }

    // ==================== PUBLIC SETTERS ====================
    
    /**
     * Set the primary application stage for this ViewModel.
     * 
     * @param primaryApplicationStage The primary stage to set
     */
    public void setPrimaryStage(Stage primaryApplicationStage) {
        this.primaryApplicationStage = primaryApplicationStage;
    }

    public void setMainLobbyScene(Scene mainLobbyScene) {
        this.mainLobbyScene = mainLobbyScene;
    }
    
    /**
     * Public method for games to call when they want to return to the lobby.
     * This method safely cleans up the game and returns to the lobby scene.
     */
    public void returnToLobby() {
        Logging.info("🎮 Game requested return to lobby via returnToLobby()");
        cleanupGameAndServerSimulator();
        // Return to lobby scene
        if (mainLobbyScene != null) {
            primaryApplicationStage.setScene(mainLobbyScene);
            primaryApplicationStage.setTitle("OMG Game Development Kit (GDK)");
        }
    }

    // ==================== PUBLIC ACTION HANDLERS ====================
    
    /**
     * Handle the game launch action initiated by the user.
     * 
     * This method validates the selected game, creates the server simulator,
     * and launches the game with proper error handling.
     * 
     * @param selectedGameModule The game module selected by the user
     */
    public void handleLaunchGame(GameModule selectedGameModule) {
        if (selectedGameModule == null) {
            Logging.error("❌ No game selected for launch");
            return;
        }
        
        try {
            launchGameWithScene(selectedGameModule);
        } catch (Exception gameLaunchError) {
            Logging.error("❌ Error launching game: " + gameLaunchError.getMessage());
        }
    }

    /**
     * Handle the refresh game list action initiated by the user.
     * 
     * This method refreshes the list of available game modules
     * and updates the UI accordingly.
     */
    public void handleRefreshGameList() {
        Logging.info("🔄 Refreshing game list");
        
        try {
            refreshAvailableGameModules();
            Logging.info("✅ Game list refreshed successfully");
        } catch (Exception refreshError) {
            Logging.error("❌ Error refreshing game list: " + refreshError.getMessage());
        }
    }
    
    // Removed manual open; simulator now starts/stops with the game.
    public boolean isGameRunning() {
        return currentlyRunningGame != null;
    }

    // ==================== GAME MANAGEMENT ====================
    
    /**
     * Launch a game with scene creation and state management.
     * 
     * @param selectedGameModule The game module to launch
     */
    private void launchGameWithScene(GameModule selectedGameModule) {
        Scene gameScene = selectedGameModule.launchGame(primaryApplicationStage);
        if (gameScene != null) {
            primaryApplicationStage.setTitle(selectedGameModule.getGameName());
            primaryApplicationStage.setScene(gameScene);
            updateGameStateAfterSuccessfulLaunch(selectedGameModule);
            // Auto-start server simulator with game (ensure single instance)
            if (serverSimulatorStage == null) {
                createServerSimulator();
            }
            setupGameCloseHandler();
            Logging.info("🎮 Game launched successfully");
        } else {
            Logging.error("❌ Failed to launch game - null scene returned");
        }
    }
    
    /**
     * Update the application state after a successful game launch.
     * 
     * @param selectedGameModule The game module that was successfully launched
     */
    private void updateGameStateAfterSuccessfulLaunch(GameModule selectedGameModule) {
        currentlyRunningGame = selectedGameModule;
        // Set up the lobby return callback for games
        gdk.MessagingBridge.setLobbyReturnCallback(this::returnToLobby);
        
        // Start transcript recording with game metadata
        String gameName = selectedGameModule.getMetadata().getGameName();
        String gameVersion = selectedGameModule.getMetadata().getGameVersion();
        launcher.utils.TranscriptRecorder.startSession(gameName, gameVersion);
        
        Logging.info("🎮 Game launched successfully: " + gameName + " (v" + gameVersion + ")");
        Logging.info("📝 Started transcript recording for game session");
    }
    
    /**
     * Set up the close handler for the game window.
     * 
     * This method configures what happens when the user closes
     * the game window, ensuring proper cleanup.
     */
    private void setupGameCloseHandler() {
        primaryApplicationStage.setOnCloseRequest(event -> {
            Logging.info("🎮 Game window closing");
            cleanupGameAndServerSimulator();
            // Return to lobby scene when game window closes
            if (mainLobbyScene != null) {
                primaryApplicationStage.setScene(mainLobbyScene);
                primaryApplicationStage.setTitle("OMG Game Development Kit (GDK)");
            }
        });
        
        // Handle WINDOW_CLOSE_REQUEST events from games (like "Back to Lobby" button)
        primaryApplicationStage.addEventHandler(javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
            Logging.info("🎮 Game requested return to lobby");
            cleanupGameAndServerSimulator();
            // Return to lobby scene
            if (mainLobbyScene != null) {
                primaryApplicationStage.setScene(mainLobbyScene);
                primaryApplicationStage.setTitle("OMG Game Development Kit (GDK)");
            }
        });
    }

    // ==================== SERVER SIMULATOR MANAGEMENT ====================
    
    /**
     * Create and configure the server simulator window.
     * 
     * This method loads the server simulator FXML, creates the stage,
     * and sets up the controller for communication with games.
     */
    private void createServerSimulator() {
        try {
            Scene serverSimulatorScene = loadServerSimulatorScene();
            configureServerSimulatorStage(serverSimulatorScene);
            setupServerSimulatorCloseHandler();
            // Wire message handler to route messages to the currently running game
            if (serverSimulatorController != null) {
                serverSimulatorController.setMessageHandler(messageText -> {
                    try {
                        if (currentlyRunningGame == null) {
                            serverSimulatorController.addReceivedMessageToDisplay("ERROR: No game running");
                            return;
                        }
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
                        java.util.Map<String, Object> response = currentlyRunningGame.handleMessage(messageMap);
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
                // Plug in reverse bridge: messages from game -> simulator display
                gdk.MessagingBridge.setConsumer(msg -> {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(msg);
                        serverSimulatorController.addReceivedMessageToDisplay(pretty);
                        // Send confirmation back to the game
                        if (currentlyRunningGame != null) {
                            java.util.Map<String, Object> ack = new java.util.HashMap<>();
                            ack.put("function", "ack");
                            ack.put("status", "ok");
                            Object of = (msg != null) ? msg.get("function") : null;
                            if (of != null) ack.put("of", of);
                            ack.put("timestamp", java.time.Instant.now().toString());
                            currentlyRunningGame.handleMessage(ack);
                        }
                    } catch (Exception ignored) {
                    }
                });
                // Also mirror messages to lobby JSON output (if requested from game)
                gdk.MessagingBridge.addConsumer(msg -> {
                    try {
                        // Present end message or others back to the lobby UI if needed
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(msg);
                        if (serverSimulatorController != null) {
                            serverSimulatorController.addReceivedMessageToDisplay(pretty);
                        }
                    } catch (Exception ignored) {}
                });
            }
            Logging.info("🔧 Server simulator created successfully");
        } catch (Exception serverSimulatorError) {
            Logging.error("❌ Failed to create server simulator: " + serverSimulatorError.getMessage());
        }
    }
    
    /**
     * Load the server simulator scene from FXML resources.
     * 
     * @return The loaded server simulator scene
     * @throws Exception if FXML loading fails
     */
    private Scene loadServerSimulatorScene() throws Exception {
        URL fxmlResourceUrl = getClass().getResource("/server-simulator/ServerSimulator.fxml");
        if (fxmlResourceUrl == null) {
            throw new RuntimeException("Server simulator FXML not found");
        }
        
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlResourceUrl);
        Scene serverSimulatorScene = new Scene(fxmlLoader.load());
        
        // Store the controller reference
        this.serverSimulatorController = fxmlLoader.getController();
        
        // Apply CSS styling if available
        URL cssResourceUrl = getClass().getResource("/server-simulator/server-simulator.css");
        if (cssResourceUrl != null) {
            serverSimulatorScene.getStylesheets().add(cssResourceUrl.toExternalForm());
        }
        
        return serverSimulatorScene;
    }
    
    /**
     * Configure the server simulator stage with proper settings.
     * 
     * @param serverSimulatorScene The scene to set on the stage
     */
    private void configureServerSimulatorStage(Scene serverSimulatorScene) {
        serverSimulatorStage = new Stage();
        serverSimulatorStage.setTitle("Server Simulator");
        serverSimulatorStage.setWidth(600);
        serverSimulatorStage.setHeight(400);
        serverSimulatorStage.setScene(serverSimulatorScene);
        serverSimulatorStage.show();
    }
    
    /**
     * Set up the close handler for the server simulator window.
     * 
     * This method configures what happens when the user closes
     * the server simulator window.
     */
    private void setupServerSimulatorCloseHandler() {
        serverSimulatorStage.setOnCloseRequest(event -> {
            Logging.info("🔒 Server simulator window closing");
            serverSimulatorController.onClose();
        });
    }

    // ==================== CLEANUP ====================
    
    /**
     * Clean up both the current game and server simulator.
     * 
     * This method ensures proper cleanup of all running components
     * when returning to the lobby or shutting down.
     */
    private void cleanupGameAndServerSimulator() {
        cleanupCurrentGame();
        cleanupServerSimulator();
    }
    
    /**
     * Clean up the currently running game.
     * 
     * This method stops the game and resets the game state.
     */
    private void cleanupCurrentGame() {
        if (currentlyRunningGame != null) {
            try {
                currentlyRunningGame.stopGame();
                Logging.info("🎮 Game stopped successfully");
            } catch (Exception gameStopError) {
                Logging.error("❌ Error stopping game: " + gameStopError.getMessage());
            }
            
            currentlyRunningGame = null;
        }
    }
    
    /**
     * Clean up the server simulator.
     * 
     * This method closes the server simulator window and resets
     * the server simulator state.
     */
    private void cleanupServerSimulator() {
        if (serverSimulatorStage != null) {
            serverSimulatorStage.close();
            serverSimulatorStage = null;
            serverSimulatorController = null;
            Logging.info("🔧 Server simulator cleaned up");
        }
    }

    // ==================== UTILITY METHODS ====================
    
    /**
     * Refresh the list of available game modules.
     * 
     * This method scans the modules directory for available game modules
     * and updates the internal list and UI accordingly.
     */
    private void refreshAvailableGameModules() {
        try {
            String modulesDirectoryPath = GDKApplication.MODULES_DIRECTORY_PATH;
            Logging.info("📂 Scanning for modules in: " + modulesDirectoryPath);
            
            // Use ModuleDiscovery to find valid modules, then ModuleCompiler to load them
            List<File> validModuleDirectories = ModuleDiscovery.getValidModuleDirectories(new File(modulesDirectoryPath));
            List<GameModule> discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
            Logging.info("✅ Found " + discoveredModules.size() + " game module(s)");
            
        } catch (Exception moduleDiscoveryError) {
            Logging.error("❌ Error discovering modules: " + moduleDiscoveryError.getMessage());
        }
    }
} 