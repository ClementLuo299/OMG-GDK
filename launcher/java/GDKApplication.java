import com.gdk.shared.game.GameModule;
import com.gdk.shared.game.GameMode;
import com.gdk.shared.game.GameEvent;
import com.gdk.shared.game.GameEventHandler;
import com.gdk.shared.utils.error_handling.Logging;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;
import java.util.function.Consumer;

/**
 * Simple Game Development Kit (GDK) Application.
 * Allows developers to easily run and test game modules.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited July 22, 2025
 * @since 1.0
 */
public class GDKApplication extends Application {
    
    private Stage primaryStage;
    private Scene lobbyScene;
    private GameModule currentGame;
    private boolean gameIsRunning = false;
    private Stage serverStage;
    private ServerSimulatorController serverController;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            // Create and configure the UI
            createUI();
            
            // Configure the stage
            primaryStage.setTitle("OMG Game Development Kit");
            primaryStage.setWidth(1200);
            primaryStage.setHeight(900);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
            
            // Show the stage
            primaryStage.show();
            
            Logging.info("✅ GDK started successfully");
            
        } catch (Exception e) {
            Logging.error("❌ Failed to start GDK: " + e.getMessage(), e);
            showError("Startup Error", "Failed to start GDK: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        Logging.info("🔄 GDK shutting down");
    }

    /**
     * Creates the main UI.
     */
    private void createUI() {
        try {
            // Load the GDK Game Lobby FXML
            URL fxmlUrl = GDKApplication.class.getResource("/GDKGameLobby.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("FXML resource not found: /GDKGameLobby.fxml");
            }
            
            Logging.info("📂 Loading FXML from: " + fxmlUrl);
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            lobbyScene = new Scene(loader.load());
            
            // Apply GDK lobby CSS
            URL cssUrl = GDKApplication.class.getResource("/gdk-lobby.css");
            if (cssUrl != null) {
                lobbyScene.getStylesheets().add(cssUrl.toExternalForm());
                Logging.info("📂 CSS loaded from: " + cssUrl);
            }
            
            // Get the controller and set the primary stage
            GDKGameLobbyController controller = loader.getController();
            if (controller != null) {
                controller.setPrimaryStage(primaryStage);
                controller.setGDKApplication(this);
                Logging.info("✅ Controller initialized successfully");
            } else {
                throw new RuntimeException("Controller is null");
            }
            
            // Set the scene
            primaryStage.setScene(lobbyScene);
            
            Logging.info("✅ GDK Game Lobby loaded successfully");
            
        } catch (Exception e) {
            Logging.error("❌ Failed to load GDK Game Lobby: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize GDK UI", e);
        }
    }
    
    /**
     * Launches a game with the specified parameters.
     * This method is called by the FXML controller.
     */
    public void launchGame(GameModule selectedGame) {
        if (selectedGame == null) {
            Logging.error("❌ No game selected for launch");
            return;
        }

        Logging.info("🚀 Launching " + selectedGame.getGameName());

        try {
            // Create server simulator window
            createServerSimulator();
            
            // Create game event handler that points to the GDK application
            GameEventHandler eventHandler = this::handleGameEvent;
            Logging.info("🎮 Created event handler for " + selectedGame.getGameName() + " - handler: " + (eventHandler != null ? "valid" : "null"));
            
            javafx.scene.Scene gameScene = selectedGame.launchGame(primaryStage, null, 0, null, eventHandler);
            if (gameScene != null) {
                currentGame = selectedGame;
                gameIsRunning = true;
                primaryStage.setScene(gameScene);
                primaryStage.setTitle(selectedGame.getGameName() + " - GDK");
                Logging.info("🎮 Game launched successfully - GDK event handler connected");
                Logging.info("🎮 Current state - gameIsRunning: " + gameIsRunning + ", currentGame: " + (currentGame != null ? currentGame.getGameName() : "null"));
            } else {
                Logging.error("❌ Failed to launch game - null scene returned");
            }
        } catch (Exception e) {
            Logging.error("❌ Error launching game: " + e.getMessage());
        }
    }
    
    /**
     * Creates the server simulator window.
     */
    private void createServerSimulator() {
        try {
            // Load the server simulator FXML
            URL fxmlUrl = GDKApplication.class.getResource("/ServerSimulator.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("Server Simulator FXML resource not found");
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene serverScene = new Scene(loader.load());
            
            // Apply CSS
            URL cssUrl = GDKApplication.class.getResource("/server-simulator.css");
            if (cssUrl != null) {
                serverScene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Get controller
            serverController = loader.getController();
            if (serverController == null) {
                throw new RuntimeException("Server Simulator Controller is null");
            }
            
            // Create stage
            serverStage = new Stage();
            serverStage.setScene(serverScene);
            serverStage.setTitle("Server Simulator - " + (currentGame != null ? currentGame.getGameName() : "Game"));
            serverStage.setWidth(400);
            serverStage.setHeight(500);
            serverStage.setMinWidth(350);
            serverStage.setMinHeight(400);
            
            // Position next to main window
            serverStage.setX(primaryStage.getX() + primaryStage.getWidth() + 10);
            serverStage.setY(primaryStage.getY());
            
            // Set up controller
            serverController.setStage(serverStage);
            serverController.setGDKApplication(this);
            serverController.setMessageHandler(this::handleServerMessage);
            
            // Handle window close
            serverStage.setOnCloseRequest(event -> {
                Logging.info("🔒 Server Simulator window closing");
                serverController.onClose();
                serverStage = null;
                serverController = null;
            });
            
            // Show the window
            serverStage.show();
            Logging.info("✅ Server Simulator created and shown");
            
        } catch (Exception e) {
            Logging.error("❌ Failed to create Server Simulator: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handles messages from the server simulator.
     */
    private void handleServerMessage(String message) {
        Logging.info("📤 Server message received: " + message);
        
        // Add to server simulator display
        if (serverController != null) {
            serverController.addReceivedMessage("SENT: " + message);
        }
        
        // TODO: Forward message to the game
        // This would be implemented based on how games expect to receive server messages
        if (currentGame != null) {
            // Example: currentGame.handleServerMessage(message);
            Logging.info("🎮 Forwarding server message to game: " + currentGame.getGameName());
        }
    }
    


    /**
     * Handles game events from running games.
     * This method is called by games to communicate state changes.
     */
    private void handleGameEvent(GameEvent event) {
        Logging.info("🎮 Game Event: " + event.getEventType() + " - " + event.getMessage());
        
        switch (event.getEventType()) {
            case BACK_TO_LOBBY_REQUESTED:
                Logging.info("🔙 GDK received BACK_TO_LOBBY_REQUESTED event");
                Logging.info("🔙 Current game state - gameIsRunning: " + gameIsRunning + ", currentGame: " + (currentGame != null ? currentGame.getGameName() : "null"));
                returnToLobby();
                break;
            case GAME_ENDED:
                gameIsRunning = false;
                currentGame = null;
                Logging.info("🎮 Game ended, ready for new game");
                break;
            case ERROR_OCCURRED:
                Logging.error("❌ Game Error: " + event.getMessage());
                break;
            default:
                // Log other events
                Logging.info("🎮 " + event.getMessage());
                break;
        }
    }
    
    /**
     * Returns to the GDK lobby from a running game.
     */
    private void returnToLobby() {
        Logging.info("🔙 returnToLobby() called - gameIsRunning: " + gameIsRunning + ", currentGame: " + (currentGame != null ? currentGame.getGameName() : "null") + ", lobbyScene: " + (lobbyScene != null ? "exists" : "null"));
        
        if (gameIsRunning && currentGame != null) {
            Logging.info("🔙 Returning to GDK lobby");
            
            // Clean up current game
            currentGame.onGameClose();
            gameIsRunning = false;
            currentGame = null;
            
            // Close server simulator
            if (serverStage != null) {
                Logging.info("🔒 Closing server simulator");
                serverStage.close();
                serverStage = null;
                serverController = null;
            }
            
            // Return to lobby scene - ensure we're on the JavaFX Application Thread
            if (lobbyScene != null) {
                javafx.application.Platform.runLater(() -> {
                    primaryStage.setScene(lobbyScene);
                    primaryStage.setTitle("OMG Game Development Kit");
                    Logging.info("✅ Returned to GDK lobby");
                });
            } else {
                Logging.error("❌ lobbyScene is null - cannot return to lobby");
            }
        } else {
            Logging.warning("⚠️ Cannot return to lobby - gameIsRunning: " + gameIsRunning + ", currentGame: " + (currentGame != null ? "exists" : "null"));
        }
    }
    
    /**
     * Shows an error dialog.
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            Logging.error("❌ Fatal error: " + e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 