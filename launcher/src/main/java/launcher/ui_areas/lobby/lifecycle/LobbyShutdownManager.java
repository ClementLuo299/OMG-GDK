package launcher.ui_areas.lobby.core.lifecycle;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.persistence.JsonPersistenceManager;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;

/**
 * Manages shutdown operations for the lobby controller.
 * Handles saving settings, persistence, and cleanup on application exit.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class LobbyShutdownManager {
    
    // ==================== DEPENDENCIES ====================
    
    private final JsonPersistenceManager jsonPersistenceManager;
    private final GameSelectionController gameSelectionController;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a new LobbyShutdownManager.
     * 
     * @param jsonPersistenceManager The JSON persistence manager
     * @param gameSelectionController The game selection controller (may be null)
     */
    public LobbyShutdownManager(JsonPersistenceManager jsonPersistenceManager,
                                 GameSelectionController gameSelectionController) {
        this.jsonPersistenceManager = jsonPersistenceManager;
        this.gameSelectionController = gameSelectionController;
    }
    
    // ==================== LIFECYCLE OPERATIONS ====================
    
    /**
     * Handle application shutdown and save settings.
     * Saves JSON content, persistence toggle state, and selected game.
     * Called when the application is closing (e.g., exit button clicked).
     */
    public void handleShutdown() {
        try {
            if (jsonPersistenceManager != null) {
                // Save JSON content if persistence is enabled
                jsonPersistenceManager.saveJsonContent();
                
                // Save persistence toggle state
                jsonPersistenceManager.savePersistenceToggleState();
                
                // Save selected game
                GameModule selectedGame = gameSelectionController != null ? 
                    gameSelectionController.getSelectedGameModule() : null;
                if (selectedGame != null) {
                    jsonPersistenceManager.persistSelectedGame(selectedGame.getMetadata().getGameName());
                }
            }
            
            Logging.info("Application settings saved successfully");
        } catch (Exception e) {
            Logging.error("Error saving application settings: " + e.getMessage(), e);
        }
    }
}

