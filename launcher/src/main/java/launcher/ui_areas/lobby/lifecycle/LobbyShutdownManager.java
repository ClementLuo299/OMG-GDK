package launcher.ui_areas.lobby.lifecycle;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.persistence.JsonPersistenceManager;
import launcher.ui_areas.lobby.json_editor.JsonEditor;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;
import com.jfoenix.controls.JFXToggleButton;

/**
 * Manages shutdown operations for the lobby controller.
 * Handles saving settings, persistence, and cleanup on application exit.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public class LobbyShutdownManager {
    
    private final JsonEditor jsonInputEditor;
    private final JFXToggleButton jsonPersistenceToggle;
    private final GameSelectionController gameSelectionController;
    
    /**
     * Create a new LobbyShutdownManager.
     * 
     * @param jsonInputEditor The JSON input editor
     * @param jsonPersistenceToggle The JSON persistence toggle
     * @param gameSelectionController The game selection controller (may be null)
     */
    public LobbyShutdownManager(JsonEditor jsonInputEditor,
                                 JFXToggleButton jsonPersistenceToggle,
                                 GameSelectionController gameSelectionController) {
        this.jsonInputEditor = jsonInputEditor;
        this.jsonPersistenceToggle = jsonPersistenceToggle;
        this.gameSelectionController = gameSelectionController;
    }
    
    /**
     * Handle application shutdown and save settings.
     * Saves JSON content, persistence toggle state, and selected game.
     * Called when the application is closing (e.g., exit button clicked).
     */
    public void handleShutdown() {
        try {
            // Save all persistence settings
            JsonPersistenceManager.save(jsonInputEditor, jsonPersistenceToggle);
            
            // Save selected game
            GameModule selectedGame = gameSelectionController != null ? 
                gameSelectionController.getSelectedGameModule() : null;
            if (selectedGame != null) {
                JsonPersistenceManager.saveSelectedGame(selectedGame.getMetadata().getGameName());
            }
            
            Logging.info("Application settings saved successfully");
        } catch (Exception e) {
            Logging.error("Error saving application settings: " + e.getMessage(), e);
        }
    }
}

