import gdk.GameModule;
import gdk.Logging;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;

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
 * - Manage application state and properties
 * - Handle game module discovery and loading
 * - Coordinate game launching and management
 * - Manage server simulator lifecycle
 * - Provide data binding for UI components
 * - Handle application cleanup and shutdown
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @since 1.0
 */
public class GDKViewModel {

    // ==================== STATUS AND DISPLAY PROPERTIES ====================
    
    /**
     * Current status message displayed to the user
     */
    private final StringProperty currentStatusMessage = new SimpleStringProperty();
    
    /**
     * Name of the currently selected game module
     */
    private final StringProperty selectedGameModuleName = new SimpleStringProperty();
    
    /**
     * Count of available game modules as a string
     */
    private final StringProperty availableGameModulesCount = new SimpleStringProperty();

    // ==================== STATE PROPERTIES ====================
    
    /**
     * Whether a game is currently running
     */
    private final BooleanProperty gameCurrentlyRunning = new SimpleBooleanProperty(false);
    
    /**
     * Whether the server simulator window is currently open
     */
    private final BooleanProperty serverSimulatorCurrentlyOpen = new SimpleBooleanProperty(false);

    // ==================== DATA COLLECTIONS ====================
    
    /**
     * List of available game modules discovered by the module loader
     */
    private final ObservableList<GameModule> availableGameModules = FXCollections.observableArrayList();

    // ==================== DEPENDENCIES ====================
    
    /**
     * Utility for discovering and loading game modules from the filesystem
     */
    private final ModuleLoader gameModuleLoader;
    
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

    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a new GDK ViewModel with the specified module loader.
     * 
     * @param gameModuleLoader The module loader to use for discovering game modules
     */
    public GDKViewModel(ModuleLoader gameModuleLoader) {
        this.gameModuleLoader = gameModuleLoader;
        initializeDefaultApplicationData();
        refreshAvailableGameModules();
    }

    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize the ViewModel with default data values.
     * 
     * This method sets up the initial state of all properties
     * with sensible default values.
     */
    private void initializeDefaultApplicationData() {
        currentStatusMessage.set("Ready to launch games");
        selectedGameModuleName.set("No game selected");
        availableGameModulesCount.set("0");
        gameCurrentlyRunning.set(false);
        serverSimulatorCurrentlyOpen.set(false);
    }

    // ==================== PROPERTY ACCESSORS ====================
    
    /**
     * Get the current status message property for UI binding.
     * 
     * @return The status message property
     */
    public StringProperty currentStatusMessageProperty() { 
        return currentStatusMessage; 
    }
    
    /**
     * Get the selected game module name property for UI binding.
     * 
     * @return The selected game module name property
     */
    public StringProperty selectedGameModuleNameProperty() { 
        return selectedGameModuleName; 
    }
    
    /**
     * Get the available game modules count property for UI binding.
     * 
     * @return The available game modules count property
     */
    public StringProperty availableGameModulesCountProperty() { 
        return availableGameModulesCount; 
    }
    
    /**
     * Get the game currently running property for UI binding.
     * 
     * @return The game currently running property
     */
    public BooleanProperty gameCurrentlyRunningProperty() { 
        return gameCurrentlyRunning; 
    }
    
    /**
     * Get the server simulator currently open property for UI binding.
     * 
     * @return The server simulator currently open property
     */
    public BooleanProperty serverSimulatorCurrentlyOpenProperty() { 
        return serverSimulatorCurrentlyOpen; 
    }
    
    /**
     * Get the list of available game modules for UI binding.
     * 
     * @return The observable list of available game modules
     */
    public ObservableList<GameModule> getAvailableGameModules() { 
        return availableGameModules; 
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

    // ==================== PUBLIC GETTERS ====================
    
    /**
     * Get the currently running game module.
     * 
     * @return The currently running game module, or null if no game is running
     */
    public GameModule getCurrentlyRunningGame() {
        return currentlyRunningGame;
    }
    
    /**
     * Get the server simulator stage.
     * 
     * @return The server simulator stage, or null if not open
     */
    public Stage getServerSimulatorStage() {
        return serverSimulatorStage;
    }
    
    /**
     * Get the server simulator controller.
     * 
     * @return The server simulator controller, or null if not initialized
     */
    public ServerSimulatorController getServerSimulatorController() {
        return serverSimulatorController;
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
        if (!validateGameSelection(selectedGameModule)) {
            return;
        }

        logGameLaunchStart(selectedGameModule);
        
        try {
            createServerSimulator();
            launchGameWithScene(selectedGameModule);
        } catch (Exception gameLaunchError) {
            handleGameLaunchError(gameLaunchError);
        }
    }

    /**
     * Handle the refresh game list action initiated by the user.
     * 
     * This method refreshes the list of available game modules
     * and updates the UI accordingly.
     */
    public void handleRefreshGameList() {
        logRefreshStart();
        
        try {
            refreshAvailableGameModules();
            logRefreshSuccess();
        } catch (Exception refreshError) {
            handleRefreshError(refreshError);
        }
    }

    /**
     * Handle the return to lobby action initiated by the user.
     * 
     * This method cleans up the current game and server simulator,
     * returning the user to the main lobby interface.
     */
    public void handleReturnToLobby() {
        Logging.info("🔙 Returning to GDK lobby");
        setStatusMessage("🔙 Returning to GDK lobby");
        cleanupGameAndServerSimulator();
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
            updateGameStateAfterSuccessfulLaunch(selectedGameModule);
            setupGameCloseHandler();
            Logging.info("🎮 Game launched successfully");
            setStatusMessage("✅ Game launched successfully");
        } else {
            Logging.error("❌ Failed to launch game - null scene returned");
            setStatusMessage("❌ Failed to launch game - null scene returned");
        }
    }
    
    /**
     * Update the application state after a successful game launch.
     * 
     * @param selectedGameModule The game module that was successfully launched
     */
    private void updateGameStateAfterSuccessfulLaunch(GameModule selectedGameModule) {
        currentlyRunningGame = selectedGameModule;
        gameCurrentlyRunning.set(true);
        selectedGameModuleName.set(selectedGameModule.getClass().getSimpleName());
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
            serverSimulatorCurrentlyOpen.set(true);
            Logging.info("🔧 Server simulator created successfully");
        } catch (Exception serverSimulatorError) {
            Logging.error("❌ Failed to create server simulator: " + serverSimulatorError.getMessage());
            setStatusMessage("❌ Failed to create server simulator");
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
            serverSimulatorCurrentlyOpen.set(false);
            serverSimulatorController.onClose();
        });
    }
    
    /**
     * Handle messages from the server simulator.
     * 
     * This method processes messages sent from the server simulator
     * and forwards them to the currently running game.
     * 
     * @param message The message from the server simulator
     */
    private void handleServerSimulatorMessage(String message) {
        if (currentlyRunningGame != null) {
            try {
                // Forward the message to the game
                Logging.info("📤 Forwarding message to game: " + message);
                // Note: This would need to be implemented based on the GameModule interface
            } catch (Exception messageError) {
                Logging.error("❌ Error forwarding message to game: " + messageError.getMessage());
            }
        } else {
            Logging.warning("⚠️ No game running to receive message: " + message);
        }
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
            gameCurrentlyRunning.set(false);
            selectedGameModuleName.set("No game selected");
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
            serverSimulatorCurrentlyOpen.set(false);
            Logging.info("🔧 Server simulator cleaned up");
        }
    }

    // ==================== UTILITY METHODS ====================
    
    /**
     * Set the current status message and update the UI.
     * 
     * @param statusMessage The status message to display
     */
    private void setStatusMessage(String statusMessage) {
        currentStatusMessage.set(statusMessage);
    }
    
    /**
     * Validate that a game module is selected for launching.
     * 
     * @param selectedGameModule The game module to validate
     * @return true if the game module is valid, false otherwise
     */
    private boolean validateGameSelection(GameModule selectedGameModule) {
        if (selectedGameModule == null) {
            Logging.error("❌ No game selected for launch");
            setStatusMessage("❌ No game selected for launch");
            return false;
        }
        return true;
    }
    
    /**
     * Log the start of a game launch operation.
     * 
     * @param selectedGameModule The game module being launched
     */
    private void logGameLaunchStart(GameModule selectedGameModule) {
        Logging.info("🚀 Launching " + selectedGameModule.getClass().getSimpleName());
        setStatusMessage("🚀 Launching " + selectedGameModule.getClass().getSimpleName());
    }
    
    /**
     * Handle errors that occur during game launch.
     * 
     * @param gameLaunchError The exception that occurred
     */
    private void handleGameLaunchError(Exception gameLaunchError) {
        Logging.error("❌ Error launching game: " + gameLaunchError.getMessage());
        setStatusMessage("❌ Error launching game: " + gameLaunchError.getMessage());
    }
    
    /**
     * Log the start of a refresh operation.
     */
    private void logRefreshStart() {
        Logging.info("🔄 Refreshing game list");
        setStatusMessage("🔄 Refreshing game list...");
    }
    
    /**
     * Log the successful completion of a refresh operation.
     */
    private void logRefreshSuccess() {
        setStatusMessage("✅ Found " + availableGameModules.size() + " game module(s)");
    }
    
    /**
     * Handle errors that occur during refresh operations.
     * 
     * @param refreshError The exception that occurred
     */
    private void handleRefreshError(Exception refreshError) {
        Logging.error("❌ Error refreshing game list: " + refreshError.getMessage());
        setStatusMessage("❌ Error refreshing game list: " + refreshError.getMessage());
    }
    
    /**
     * Refresh the list of available game modules.
     * 
     * This method scans the modules directory for available game modules
     * and updates the internal list and UI accordingly.
     */
    private void refreshAvailableGameModules() {
        try {
            availableGameModules.clear();
            
            String modulesDirectoryPath = GDKApplication.MODULES_DIRECTORY_PATH;
            Logging.info("📂 Scanning for modules in: " + modulesDirectoryPath);
            
            List<GameModule> discoveredModules = gameModuleLoader.discoverModules(modulesDirectoryPath);
            availableGameModules.addAll(discoveredModules);
            
            availableGameModulesCount.set(String.valueOf(availableGameModules.size()));
            Logging.info("✅ Found " + availableGameModules.size() + " game module(s)");
            
        } catch (Exception moduleDiscoveryError) {
            Logging.error("❌ Error discovering modules: " + moduleDiscoveryError.getMessage());
            setStatusMessage("❌ Error discovering modules");
        }
    }
} 