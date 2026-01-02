package launcher.features.lobby_features.managers.game_launching;

import gdk.internal.Logging;
import launcher.ui_areas.lobby.managers.messaging.MessageManager;
import launcher.core.ui_features.pop_up_dialogs.DialogUtil;

/**
 * Handles error reporting and user feedback for game launch operations.
 * 
 * <p>This handler is responsible for:
 * <ul>
 *   <li>Reporting launch errors to the user via pop_up_dialogs and messages</li>
 *   <li>Logging errors for debugging</li>
 *   <li>Reporting successful launches</li>
 * </ul>
 * 
 * <p>Error handling behavior differs based on launch mode:
 * <ul>
 *   <li><b>UI launch</b>: Shows error pop_up_dialogs and messages to the user</li>
 *   <li><b>Auto-launch</b>: Only logs errors (no user-visible pop_up_dialogs)</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class GameLaunchErrorHandler {
    
    // ==================== DEPENDENCIES ====================
    
    /** Message manager for displaying user feedback messages. */
    private final MessageManager messageManager;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new GameLaunchErrorHandler.
     * 
     * @param messageManager The message manager for user feedback
     */
    public GameLaunchErrorHandler(MessageManager messageManager) {
        this.messageManager = messageManager;
    }
    
    // ==================== PUBLIC METHODS - ERROR HANDLING ====================
    
    /**
     * Handles error when ViewModel is not available.
     * 
     * <p>For UI launches, shows an error dialog and message.
     * For auto-launches, only logs the error.
     * 
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     */
    public void handleViewModelUnavailable(boolean isAutoLaunch) {
        Logging.error("applicationViewModel is null - cannot launch game");
        
        if (!isAutoLaunch) {
            DialogUtil.showError("Application Error", 
                "Application ViewModel is not available. Please restart the application.");
            messageManager.addMessage("Error: ViewModel not available");
        }
    }
    
    /**
     * Handles validation error during launch.
     * 
     * <p>For UI launches, shows an error dialog and message.
     * For auto-launches, only logs the error.
     * 
     * @param errorMessage The validation error message
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     */
    public void handleValidationError(String errorMessage, boolean isAutoLaunch) {
        Logging.error("Game launch validation failed: " + errorMessage);
        
        if (!isAutoLaunch) {
            DialogUtil.showError("Launch Error", errorMessage);
            messageManager.addMessage("Error: " + errorMessage);
        }
    }
    
    /**
     * Handles game configuration failure.
     * 
     * <p>For UI launches, shows an error dialog and message.
     * For auto-launches, only logs the error.
     * 
     * @param gameName The name of the game that failed to configure
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     */
    public void handleConfigurationFailure(String gameName, boolean isAutoLaunch) {
        Logging.error("Game configuration failed for: " + gameName);
        
        if (!isAutoLaunch) {
            DialogUtil.showError("Launch Error", 
                "Failed to configure game: " + gameName + ". Check the logs for details.");
            messageManager.addMessage("Failed to configure game: " + gameName);
        }
    }
    
    /**
     * Handles exception during game launch execution.
     * 
     * <p>For UI launches, shows an error dialog with exception details and a message.
     * For auto-launches, only logs the error.
     * 
     * @param gameName The name of the game that failed to launch
     * @param exception The exception that occurred
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     */
    public void handleLaunchException(String gameName, Exception exception, boolean isAutoLaunch) {
        Logging.error("Exception while launching game: " + gameName, exception);
        
        if (!isAutoLaunch) {
            DialogUtil.showError("Launch Error", 
                "Failed to launch game: " + gameName + "\n\nError: " + exception.getMessage());
            messageManager.addMessage("Error launching game: " + exception.getMessage());
        }
    }
    
    // ==================== PUBLIC METHODS - SUCCESS REPORTING ====================
    
    /**
     * Reports a successful game launch to the user.
     * 
     * @param gameName The name of the game that was launched
     */
    public void reportSuccessfulLaunch(String gameName) {
        messageManager.addMessage("Launching game: " + gameName);
    }
}

