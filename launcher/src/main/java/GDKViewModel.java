import gdk.GameModule;
import gdk.Logging;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;
import javafx.fxml.FXMLLoader;
import java.net.URL;

/**
 * Manages GUI state and logic for the GDK application
 *
 * @authors Clement Luo
 * @date July 24, 2025
 * @edited July 25, 2025
 * @since 1.0
 */
public class GDKViewModel {

    // ==================== PROPERTIES ====================
    
    private final StringProperty statusMessage = new SimpleStringProperty();
    private final StringProperty selectedGameName = new SimpleStringProperty();
    private final StringProperty availableGamesCount = new SimpleStringProperty();
    private final ObservableList<GameModule> availableGames = FXCollections.observableArrayList();
    private final BooleanProperty gameIsRunning = new SimpleBooleanProperty(false);
    private final BooleanProperty serverSimulatorOpen = new SimpleBooleanProperty(false);

    // ==================== DEPENDENCIES ====================
    
    private final ModuleLoader moduleLoader;
    private GameModule currentGame;
    private Stage primaryStage;
    private Stage serverStage;
    private ServerSimulatorController serverController;

    // ==================== CONSTRUCTOR ====================
    
    public GDKViewModel(ModuleLoader moduleLoader) {
        this.moduleLoader = moduleLoader;
        
        // Initialize with default values
        initializeDefaultData();
        
        // Load available games
        refreshGameList();
    }

    // ==================== PROPERTY ACCESSORS ====================
    
    public StringProperty statusMessageProperty() { return statusMessage; }
    public StringProperty selectedGameNameProperty() { return selectedGameName; }
    public StringProperty availableGamesCountProperty() { return availableGamesCount; }
    public ObservableList<GameModule> getAvailableGames() { return availableGames; }
    public BooleanProperty gameIsRunningProperty() { return gameIsRunning; }
    public BooleanProperty serverSimulatorOpenProperty() { return serverSimulatorOpen; }

    // ==================== PUBLIC ACTION HANDLERS ====================
    
    /**
     * Handles game launch action
     */
    public void handleLaunchGame(GameModule selectedGame) {
        if (selectedGame == null) {
            Logging.error("❌ No game selected for launch");
            setStatusMessage("❌ No game selected for launch");
            return;
        }

        Logging.info("🚀 Launching " + selectedGame.getClass().getSimpleName());
        setStatusMessage("🚀 Launching " + selectedGame.getClass().getSimpleName());

        try {
            // Create server simulator window for testing
            createServerSimulator();
            
            // Launch the game with just the stage (ultra-minimal interface)
            Scene gameScene = selectedGame.launchGame(primaryStage);
            if (gameScene != null) {
                // Update application state
                currentGame = selectedGame;
                gameIsRunning.set(true);
                selectedGameName.set(selectedGame.getClass().getSimpleName());
                
                // Set up game close handler to ensure both game and server simulator close
                primaryStage.setOnCloseRequest(event -> {
                    Logging.info("🔒 Game window closing - cleaning up both game and server simulator");
                    cleanupGameAndServer();
                });
                
                Logging.info("🎮 Game launched successfully");
                setStatusMessage("✅ Game launched successfully");
            } else {
                Logging.error("❌ Failed to launch game - null scene returned");
                setStatusMessage("❌ Failed to launch game - null scene returned");
            }
        } catch (Exception e) {
            Logging.error("❌ Error launching game: " + e.getMessage());
            setStatusMessage("❌ Error launching game: " + e.getMessage());
        }
    }

    /**
     * Handles refresh game list action
     */
    public void handleRefreshGameList() {
        Logging.info("🔄 Refreshing game list");
        setStatusMessage("🔄 Refreshing game list...");
        
        try {
            refreshGameList();
            setStatusMessage("✅ Found " + availableGames.size() + " game module(s)");
        } catch (Exception e) {
            Logging.error("❌ Error refreshing game list: " + e.getMessage());
            setStatusMessage("❌ Error refreshing game list: " + e.getMessage());
        }
    }

    /**
     * Handles return to lobby action
     */
    public void handleReturnToLobby() {
        Logging.info("🔙 Returning to GDK lobby");
        setStatusMessage("🔙 Returning to GDK lobby");
        cleanupGameAndServer();
    }

    // ==================== PUBLIC METHODS ====================
    
    /**
     * Sets the primary stage reference
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Gets the current game
     */
    public GameModule getCurrentGame() {
        return currentGame;
    }

    /**
     * Gets the server stage
     */
    public Stage getServerStage() {
        return serverStage;
    }

    /**
     * Gets the server controller
     */
    public ServerSimulatorController getServerController() {
        return serverController;
    }

    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Initializes default data for the GDK
     */
    private void initializeDefaultData() {
        statusMessage.set("🎮 GDK Ready");
        selectedGameName.set("None");
        availableGamesCount.set("0");
        gameIsRunning.set(false);
        serverSimulatorOpen.set(false);
    }
    
    /**
     * Refreshes the list of available games
     */
    private void refreshGameList() {
        try {
            // Get modules directory from GDKApplication constants
            String modulesDir = GDKApplication.MODULES_DIR;
            Logging.info("📂 Scanning for modules in: " + modulesDir);
            
            // Discover modules
            List<GameModule> discoveredModules = moduleLoader.discoverModules(modulesDir);
            
            availableGames.clear();
            for (GameModule module : discoveredModules) {
                availableGames.add(module);
                Logging.info("📦 Loaded game module: " + module.getClass().getSimpleName());
            }
            
            // Update count
            availableGamesCount.set(String.valueOf(availableGames.size()));
            
            if (availableGames.isEmpty()) {
                Logging.warning("⚠️ No game modules found in " + modulesDir);
            } else {
                Logging.info("✅ Found " + availableGames.size() + " game module(s)");
            }
            
        } catch (Exception e) {
            Logging.error("❌ Error refreshing game list: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates the server simulator window
     */
    private void createServerSimulator() {
        try {
            // Load the server simulator FXML
            URL fxmlUrl = GDKApplication.class.getResource("/server-simulator/ServerSimulator.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("Server Simulator FXML resource not found");
            }
            
            // Create the server simulator scene
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene serverScene = new Scene(loader.load());
            
            // Apply CSS styling
            URL cssUrl = GDKApplication.class.getResource("/server-simulator/server-simulator.css");
            if (cssUrl != null) {
                serverScene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Get controller and set up references
            serverController = loader.getController();
            if (serverController == null) {
                throw new RuntimeException("Server Simulator Controller is null");
            }
            
            // Create and configure the server stage
            serverStage = new Stage();
            serverStage.setScene(serverScene);
            serverStage.setTitle("Server Simulator - " + (currentGame != null ? currentGame.getClass().getSimpleName() : "Game"));
            serverStage.setWidth(400);
            serverStage.setHeight(500);
            serverStage.setMinWidth(350);
            serverStage.setMinHeight(400);
            
            // Position next to main window
            serverStage.setX(primaryStage.getX() + primaryStage.getWidth() + 10);
            serverStage.setY(primaryStage.getY());
            
            // Set up controller with stage and message handler
            serverController.setStage(serverStage);
            serverController.setMessageHandler(this::handleServerMessage);
            
            // Handle window close event - ensure both game and server simulator close
            serverStage.setOnCloseRequest(event -> {
                Logging.info("🔒 Server Simulator window closing - cleaning up both game and server simulator");
                cleanupGameAndServer();
            });
            
            // Show the window
            serverStage.show();
            serverSimulatorOpen.set(true);
            Logging.info("✅ Server Simulator created and shown");
            
        } catch (Exception e) {
            Logging.error("❌ Failed to create Server Simulator: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cleanup method that ensures both game and server simulator are properly closed.
     * Called when either the game window or server simulator window is closed.
     */
    private void cleanupGameAndServer() {
        Logging.info("🧹 Starting cleanup of game and server simulator");
        
        // Clean up current game resources
        if (currentGame != null) {
            Logging.info("🎮 Closing game: " + currentGame.getClass().getSimpleName());
            currentGame.stopGame();
            currentGame = null;
        }
        gameIsRunning.set(false);
        selectedGameName.set("None");
        
        // Close server simulator window
        if (serverStage != null) {
            Logging.info("🔒 Closing server simulator window");
            serverStage.close();
            serverStage = null;
            serverController = null;
        }
        serverSimulatorOpen.set(false);
        
        setStatusMessage("✅ Returned to GDK lobby after cleanup");
    }
    
    /**
     * Handles messages from the server simulator
     */
    private void handleServerMessage(String message) {
        Logging.info("📤 Server message received: " + message);
        
        // Add to server simulator display for feedback
        if (serverController != null) {
            serverController.addReceivedMessage("SENT: " + message);
        }
        
        // TODO: Forward message to the game
        // This would be implemented based on how games expect to receive server messages
        if (currentGame != null) {
            // Example: currentGame.handleServerMessage(message);
            Logging.info("🎮 Forwarding server message to game: " + currentGame.getClass().getSimpleName());
        }
    }
    
    /**
     * Sets the status message
     */
    private void setStatusMessage(String message) {
        statusMessage.set(message);
    }
} 