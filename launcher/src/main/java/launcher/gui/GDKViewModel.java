package launcher.gui;

import gdk.api.GameModule;
import gdk.internal.Logging;
import gdk.internal.MessagingBridge;
import launcher.utils.ModuleDiscovery;
import launcher.utils.ModuleCompiler;
import launcher.utils.PathUtil;

import java.io.File;

import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

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
 * @edited August 12, 2025  
 * @since 1.0
 */
public class GDKViewModel {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ObjectWriter JSON_PRETTY_WRITER = JSON_MAPPER.writerWithDefaultPrettyPrinter();

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
    
    /**
     * Flag to track if the game has requested the server simulator to be closed
     */
    private boolean serverSimulatorRequestedClosed = false;
    
    private Scene mainLobbyScene;

    /** Subscription for the per-game server simulator consumer. */
    private MessagingBridge.Subscription serverSimulatorSubscription;

    /** Subscription for transcript recording during a game session. */
    private MessagingBridge.Subscription transcriptSubscription;

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
     * Handle the launch game action initiated by the user.
     * 
     * This method validates the selected game, creates the server simulator,
     * and launches the game with proper error handling.
     * 
     * @param selectedGameModule The game module selected by the user
     * @param jsonConfiguration The JSON configuration for the game (can be null)
     */
    public void handleLaunchGame(GameModule selectedGameModule, String jsonConfiguration) {
        if (selectedGameModule == null) {
            Logging.error("❌ No game selected for launch");
            return;
        }
        
        try {
            launchGameWithScene(selectedGameModule, jsonConfiguration);
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
    
    /**
     * Check if a game is currently running.
     * 
     * @return true if a game is running, false otherwise
     */
    public boolean isGameRunning() {
        return currentlyRunningGame != null;
    }
    
    /**
     * Get the current game scene if a game is running.
     * This method is used by auto-launch functionality.
     * 
     * @return The current game scene, or null if no game is running
     */
    public Scene getCurrentGameScene() {
        if (currentlyRunningGame != null && primaryApplicationStage != null) {
            return primaryApplicationStage.getScene();
        }
        return null;
    }

    // ==================== GAME MANAGEMENT ====================
    
    /**
     * Launch a game with scene creation and state management.
     * 
     * @param selectedGameModule The game module to launch
     * @param jsonConfiguration The JSON configuration for the game (can be null)
     */
    private void launchGameWithScene(GameModule selectedGameModule, String jsonConfiguration) {
        // Reset game state BEFORE launching the game
        serverSimulatorRequestedClosed = false;
        Logging.info("🔍 DEBUG: Reset game state - serverSimulatorRequestedClosed: " + serverSimulatorRequestedClosed);
        
        // Check if this is single player mode or local multiplayer mode from the JSON configuration
        boolean isSinglePlayerMode = isSinglePlayerModeFromJson(jsonConfiguration);
        boolean isLocalMultiplayerMode = isLocalMultiplayerModeFromJson(jsonConfiguration);
        
        if (isSinglePlayerMode) {
            Logging.info("🤖 Single player mode detected from JSON - will skip server simulator creation");
        }
        if (isLocalMultiplayerMode) {
            Logging.info("🤖 Local multiplayer mode detected from JSON - will skip server simulator creation");
        }
        
        // Set up MessagingBridge consumer BEFORE launching the game
        setupMessagingBridgeConsumer();
        
        // Also set up transcript recording consumer
        if (transcriptSubscription != null) {
            transcriptSubscription.unsubscribe();
            transcriptSubscription = null;
        }
        transcriptSubscription = MessagingBridge.addConsumer(msg -> {
            try {
                // Record the message to the transcript
                launcher.utils.TranscriptRecorder.recordFromGame(msg);
            } catch (Exception ignored) {}
        });
        
        Scene gameScene = selectedGameModule.launchGame(primaryApplicationStage);
        if (gameScene != null) {
            primaryApplicationStage.setTitle(selectedGameModule.getMetadata().getGameName());
            primaryApplicationStage.setScene(gameScene);
            updateGameStateAfterSuccessfulLaunch(selectedGameModule);
            
            // Auto-start server simulator with game (ensure single instance) - with delay to allow game to configure
            if (serverSimulatorStage == null) {
                // Add a small delay to allow the game to process start messages and potentially close the simulator
                new Thread(() -> {
                    try {
                        Thread.sleep(1000); // 1 second delay
                        javafx.application.Platform.runLater(() -> {
                            // Check if the game has requested the server simulator to be closed OR if it's single player mode
                            if (serverSimulatorStage == null && !serverSimulatorRequestedClosed && !isSinglePlayerMode && !isLocalMultiplayerMode) {
                                Logging.info("🤖 Creating server simulator after delay (not single player mode and no closure requested)");
                                createServerSimulator();
                            } else if (serverSimulatorRequestedClosed) {
                                Logging.info("🤖 Skipping server simulator creation - game requested closure");
                            } else if (isSinglePlayerMode) {
                                Logging.info("🤖 Skipping server simulator creation - single player mode detected from JSON");
                            } else if (isLocalMultiplayerMode) {
                                Logging.info("🤖 Skipping server simulator creation - local multiplayer mode detected from JSON");
                            } else {
                                Logging.info("🔍 DEBUG: Server simulator already exists, not creating new one");
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
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
        MessagingBridge.setLobbyReturnCallback(this::returnToLobby);
        
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
                        java.util.Map<String, Object> messageMap;
                        try {
                            messageMap = JSON_MAPPER.readValue(messageText, java.util.Map.class);
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
                        String responseText = JSON_PRETTY_WRITER.writeValueAsString(response);
                        serverSimulatorController.addReceivedMessageToDisplay(responseText);
                    } catch (Exception e) {
                        serverSimulatorController.addReceivedMessageToDisplay("ERROR: " + e.getMessage());
                    }
                });
                // Note: MessagingBridge consumer is now set up separately when game launches
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
            
            // End transcript session and save transcript
            launcher.utils.TranscriptRecorder.endSessionIfEndDetected(null);
            launcher.utils.TranscriptRecorder.saveCurrentTranscript();
            Logging.info("📝 Transcript session ended and saved");
            
            currentlyRunningGame = null;
        }

        cleanupMessagingBridgeSubscriptions();
    }

    /**
     * Remove any per-game MessagingBridge consumers that were registered.
     */
    private void cleanupMessagingBridgeSubscriptions() {
        if (serverSimulatorSubscription != null) {
            serverSimulatorSubscription.unsubscribe();
            serverSimulatorSubscription = null;
        }
        if (transcriptSubscription != null) {
            transcriptSubscription.unsubscribe();
            transcriptSubscription = null;
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
    
    /**
     * Close the server simulator window without affecting the game.
     * This is used when games (like single player) don't need the server simulator.
     */
    private void closeServerSimulator() {
        if (serverSimulatorStage != null) {
            serverSimulatorStage.close();
            serverSimulatorStage = null;
            serverSimulatorController = null;
            Logging.info("🤖 Server simulator closed by game request (single player mode)");
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
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            Logging.info("📂 Scanning for modules in: " + modulesDirectoryPath);
            
            // Use ModuleDiscovery to find valid modules, then ModuleCompiler to load them
            List<File> validModuleDirectories = ModuleDiscovery.getValidModuleDirectories(new File(modulesDirectoryPath));
            List<GameModule> discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
            Logging.info("✅ Found " + discoveredModules.size() + " game module(s)");
            
        } catch (Exception moduleDiscoveryError) {
            Logging.error("❌ Error discovering modules: " + moduleDiscoveryError.getMessage());
        }
    }

    /**
     * Set up the MessagingBridge consumer to handle messages from the currently running game.
     * This consumer is responsible for receiving messages from the game and
     * forwarding them to the server simulator for display and processing.
     */
    private void setupMessagingBridgeConsumer() {
        if (serverSimulatorSubscription != null) {
            serverSimulatorSubscription.unsubscribe();
            serverSimulatorSubscription = null;
        }

        serverSimulatorSubscription = MessagingBridge.addConsumer(msg -> {
            try {
                Logging.info("🔍 DEBUG: Received message from game: " + (msg != null ? msg.get("function") : "null"));
                
                // Check if this is a request to close the server simulator
                if (msg != null && "close_server_simulator".equals(msg.get("function"))) {
                    Logging.info("🤖 Game requested server simulator closure: " + msg.get("reason"));
                    Logging.info("🔍 DEBUG: Setting serverSimulatorRequestedClosed from false to true");
                    serverSimulatorRequestedClosed = true;
                    Logging.info("🔍 DEBUG: serverSimulatorRequestedClosed is now: " + serverSimulatorRequestedClosed);
                    closeServerSimulator();
                    return;
                }
                
                String pretty = JSON_PRETTY_WRITER.writeValueAsString(msg);
                if (serverSimulatorController != null) {
                    serverSimulatorController.addReceivedMessageToDisplay(pretty);
                }
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
            } catch (Exception e) {
                Logging.error("❌ Error handling MessagingBridge message: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Check if the current game is single player mode by examining the JSON configuration.
     * This is a much simpler and more reliable approach than the MessagingBridge system.
     */
    private boolean isSinglePlayerModeFromJson(String jsonConfiguration) {
        try {
            if (jsonConfiguration != null && !jsonConfiguration.trim().isEmpty()) {
                // Parse the JSON to check the gameMode field
                java.util.Map<String, Object> jsonData = JSON_MAPPER.readValue(jsonConfiguration, java.util.Map.class);
                
                Object gameMode = jsonData.get("gameMode");
                if (gameMode instanceof String) {
                    String mode = (String) gameMode;
                    boolean isSinglePlayer = "single_player".equals(mode);
                    Logging.info("🔍 DEBUG: JSON gameMode: " + mode + ", isSinglePlayer: " + isSinglePlayer);
                    return isSinglePlayer;
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error checking game mode from JSON: " + e.getMessage());
        }
        
        Logging.info("🔍 DEBUG: Could not determine game mode from JSON, defaulting to false");
        return false;
    }

    /**
     * Check if the current game is local multiplayer mode by examining the JSON configuration.
     * This is a much simpler and more reliable approach than the MessagingBridge system.
     */
    private boolean isLocalMultiplayerModeFromJson(String jsonConfiguration) {
        try {
            if (jsonConfiguration != null && !jsonConfiguration.trim().isEmpty()) {
                // Parse the JSON to check the gameMode field
                java.util.Map<String, Object> jsonData = JSON_MAPPER.readValue(jsonConfiguration, java.util.Map.class);
                
                Object gameMode = jsonData.get("gameMode");
                if (gameMode instanceof String) {
                    String mode = (String) gameMode;
                    boolean isLocalMultiplayer = "local_multiplayer".equals(mode);
                    Logging.info("🔍 DEBUG: JSON gameMode: " + mode + ", isLocalMultiplayer: " + isLocalMultiplayer);
                    return isLocalMultiplayer;
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error checking game mode from JSON: " + e.getMessage());
        }
        
        Logging.info("🔍 DEBUG: Could not determine game mode from JSON, defaulting to false");
        return false;
    }
} 
