package launcher.gui.lobby.managers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.gui.lobby.subcontrollers.GameSelectionController;

/**
 * Manages lifecycle operations for the lobby controller.
 * Handles shutdown, persistence, and cleanup.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class LobbyLifecycleManager {
    
    private final JsonPersistenceManager jsonPersistenceManager;
    private final GameSelectionController gameSelectionController;
    
    /**
     * Create a new LobbyLifecycleManager.
     * 
     * @param jsonPersistenceManager The JSON persistence manager
     * @param gameSelectionController The game selection controller (may be null)
     */
    public LobbyLifecycleManager(JsonPersistenceManager jsonPersistenceManager,
                                 GameSelectionController gameSelectionController) {
        this.jsonPersistenceManager = jsonPersistenceManager;
        this.gameSelectionController = gameSelectionController;
    }
    
    /**
     * Handle application shutdown and save settings.
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
            
            Logging.info("📋 Application settings saved successfully");
        } catch (Exception e) {
            Logging.error("❌ Error saving application settings: " + e.getMessage(), e);
        }
    }
}

