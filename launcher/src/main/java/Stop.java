import gdk.GameModule;
import gdk.Logging;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles shutdown of the GDK application
 *
 * @authors Clement Luo
 * @date July 24, 2025
 * @edited July 24, 2025s
 * @since 1.0
 */
public class Stop {

    // ==================== CONSTANTS ====================
    
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 10;
    
    // ==================== DEPENDENCIES ====================
    
    private final GDKViewModel viewModel;
    private final Stage primaryStage;
    private final Scene lobbyScene;

    // ==================== CONSTRUCTOR ====================
    
    public Stop(GDKViewModel viewModel, Stage primaryStage, Scene lobbyScene) {
        this.viewModel = viewModel;
        this.primaryStage = primaryStage;
        this.lobbyScene = lobbyScene;
    }

    // ==================== PUBLIC SHUTDOWN METHODS ====================
    
    /**
     * Initiates graceful shutdown of the GDK application
     */
    public void stop() {
        Logging.info("🔄 Starting GDK shutdown process");
        
        try {
            // Perform cleanup operations
            performCleanup();
            
            // Return to lobby if needed
            returnToLobby();
            
            // Log successful shutdown
            Logging.info("✅ GDK shutdown completed successfully");
            
        } catch (Exception e) {
            Logging.error("❌ Error during GDK shutdown: " + e.getMessage(), e);
        }
    }
    
    /**
     * Forces immediate shutdown (use only in emergency)
     */
    public void forceStop() {
        Logging.warning("⚠️ Force shutting down GDK");
        
        try {
            // Force cleanup without waiting
            forceCleanup();
            
            // Exit application
            Platform.exit();
            
            Logging.info("✅ GDK force shutdown completed");
            
        } catch (Exception e) {
            Logging.error("❌ Error during force shutdown: " + e.getMessage(), e);
            System.exit(1);
        }
    }
    
    /**
     * Shuts down with timeout
     */
    public CompletableFuture<Boolean> stopWithTimeout() {
        Logging.info("⏱️ Starting timed GDK shutdown");
        
        CompletableFuture<Boolean> shutdownFuture = new CompletableFuture<>();
        
        try {
            // Start shutdown process
            stop();
            
            // Set timeout
            CompletableFuture.delayedExecutor(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .execute(() -> {
                    if (!shutdownFuture.isDone()) {
                        Logging.warning("⏰ Shutdown timeout reached, forcing shutdown");
                        forceStop();
                        shutdownFuture.complete(false);
                    }
                });
            
            shutdownFuture.complete(true);
            
        } catch (Exception e) {
            Logging.error("❌ Error during timed shutdown: " + e.getMessage(), e);
            shutdownFuture.complete(false);
        }
        
        return shutdownFuture;
    }

    // ==================== PRIVATE CLEANUP METHODS ====================
    
    /**
     * Performs cleanup operations
     */
    private void performCleanup() {
        Logging.info("🧹 Performing GDK cleanup");
        
        try {
            // Clean up current game if running
            cleanupCurrentGame();
            
            // Clean up server simulator
            cleanupServerSimulator();
            
            // Clean up ViewModel
            cleanupViewModel();
            
            Logging.info("✅ GDK cleanup completed");
            
        } catch (Exception e) {
            Logging.error("❌ Error during cleanup: " + e.getMessage(), e);
        }
    }
    
    /**
     * Forces cleanup without waiting
     */
    private void forceCleanup() {
        Logging.warning("⚠️ Force cleaning up GDK resources");
        
        try {
            // Force close current game
            if (viewModel != null && viewModel.getCurrentGame() != null) {
                GameModule currentGame = viewModel.getCurrentGame();
                Logging.info("🎮 Force closing game: " + currentGame.getClass().getSimpleName());
                currentGame.onGameClose();
            }
            
            // Force close server simulator
            if (viewModel != null && viewModel.getServerStage() != null) {
                Logging.info("🔒 Force closing server simulator");
                viewModel.getServerStage().close();
            }
            
            Logging.info("✅ Force cleanup completed");
            
        } catch (Exception e) {
            Logging.error("❌ Error during force cleanup: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cleans up the current game
     */
    private void cleanupCurrentGame() {
        if (viewModel != null && viewModel.getCurrentGame() != null) {
            GameModule currentGame = viewModel.getCurrentGame();
            Logging.info("🎮 Cleaning up current game: " + currentGame.getClass().getSimpleName());
            
            try {
                currentGame.onGameClose();
                Logging.info("✅ Game cleanup completed");
            } catch (Exception e) {
                Logging.error("❌ Error cleaning up game: " + e.getMessage(), e);
            }
        } else {
            Logging.info("ℹ️ No current game to clean up");
        }
    }
    
    /**
     * Cleans up the server simulator
     */
    private void cleanupServerSimulator() {
        if (viewModel != null && viewModel.getServerStage() != null) {
            Logging.info("🔒 Cleaning up server simulator");
            
            try {
                Stage serverStage = viewModel.getServerStage();
                if (serverStage.isShowing()) {
                    serverStage.close();
                    Logging.info("✅ Server simulator cleanup completed");
                }
            } catch (Exception e) {
                Logging.error("❌ Error cleaning up server simulator: " + e.getMessage(), e);
            }
        } else {
            Logging.info("ℹ️ No server simulator to clean up");
        }
    }
    
    /**
     * Cleans up the ViewModel
     */
    private void cleanupViewModel() {
        if (viewModel != null) {
            Logging.info("🧠 Cleaning up ViewModel");
            
            try {
                // Reset ViewModel state
                viewModel.handleReturnToLobby();
                Logging.info("✅ ViewModel cleanup completed");
            } catch (Exception e) {
                Logging.error("❌ Error cleaning up ViewModel: " + e.getMessage(), e);
            }
        } else {
            Logging.info("ℹ️ No ViewModel to clean up");
        }
    }
    
    /**
     * Returns to the lobby scene
     */
    private void returnToLobby() {
        if (primaryStage != null && lobbyScene != null) {
            Logging.info("🔙 Returning to GDK lobby");
            
            try {
                Platform.runLater(() -> {
                    primaryStage.setScene(lobbyScene);
                    primaryStage.setTitle("OMG Game Development Kit");
                    Logging.info("✅ Returned to GDK lobby");
                });
            } catch (Exception e) {
                Logging.error("❌ Error returning to lobby: " + e.getMessage(), e);
            }
        } else {
            Logging.info("ℹ️ Cannot return to lobby - missing stage or scene");
        }
    }
    
    /**
     * Gets shutdown status information
     */
    public String getStopStatus() {
        StringBuilder status = new StringBuilder();
        status.append("GDK Shutdown Status:\n");
        
        if (viewModel != null) {
            status.append("- ViewModel: ").append(viewModel.getCurrentGame() != null ? "Has active game" : "No active game").append("\n");
            status.append("- Server Simulator: ").append(viewModel.getServerStage() != null ? "Open" : "Closed").append("\n");
            status.append("- Game Running: ").append(viewModel.gameIsRunningProperty().get() ? "Yes" : "No").append("\n");
        } else {
            status.append("- ViewModel: Not available\n");
        }
        
        status.append("- Primary Stage: ").append(primaryStage != null ? "Available" : "Not available").append("\n");
        status.append("- Lobby Scene: ").append(lobbyScene != null ? "Available" : "Not available").append("\n");
        
        return status.toString();
    }
} 