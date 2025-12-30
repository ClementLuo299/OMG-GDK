package launcher.gui.lobby.ui_logic.managers;

import gdk.internal.Logging;
import launcher.utils.gui.DialogUtil;

/**
 * Handles error reporting for game launch operations.
 * Manages UI error dialogs and message reporting for launch failures.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class GameLaunchErrorHandler {
    
    private final MessageManager messageManager;
    
    /**
     * Create a new GameLaunchErrorHandler.
     * 
     * @param messageManager The message manager for user feedback
     */
    public GameLaunchErrorHandler(MessageManager messageManager) {
        this.messageManager = messageManager;
    }
    
    /**
     * Handle error when ViewModel is not available.
     * 
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     */
    public void handleViewModelUnavailable(boolean isAutoLaunch) {
        Logging.error("❌ applicationViewModel is null - cannot launch game");
        if (!isAutoLaunch) {
            DialogUtil.showError("Application Error", "Application ViewModel is not available. Please restart the application.");
            messageManager.addMessage("❌ Error: ViewModel not available");
        }
    }
    
    /**
     * Handle validation error.
     * 
     * @param errorMessage The validation error message
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     */
    public void handleValidationError(String errorMessage, boolean isAutoLaunch) {
        Logging.error("❌ Game launch validation failed: " + errorMessage);
        if (!isAutoLaunch) {
            DialogUtil.showError("Launch Error", errorMessage);
            messageManager.addMessage("❌ Error: " + errorMessage);
        }
    }
    
    /**
     * Handle game configuration failure.
     * 
     * @param gameName The name of the game that failed to configure
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     */
    public void handleConfigurationFailure(String gameName, boolean isAutoLaunch) {
        Logging.error("❌ Game configuration failed for: " + gameName);
        if (!isAutoLaunch) {
            DialogUtil.showError("Launch Error", "Failed to configure game: " + gameName + ". Check the logs for details.");
            messageManager.addMessage("❌ Failed to configure game: " + gameName);
        }
    }
    
    /**
     * Handle launch exception.
     * 
     * @param gameName The name of the game that failed to launch
     * @param exception The exception that occurred
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     */
    public void handleLaunchException(String gameName, Exception exception, boolean isAutoLaunch) {
        Logging.error("❌ Exception while launching game: " + gameName, exception);
        if (!isAutoLaunch) {
            DialogUtil.showError("Launch Error", "Failed to launch game: " + gameName + "\n\nError: " + exception.getMessage());
            messageManager.addMessage("❌ Error launching game: " + exception.getMessage());
        }
    }
    
    /**
     * Report successful launch.
     * 
     * @param gameName The name of the game that was launched
     */
    public void reportSuccessfulLaunch(String gameName) {
        messageManager.addMessage("✅ Launching game: " + gameName);
    }
}

