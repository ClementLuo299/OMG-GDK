package launcher.features.persistence;

import gdk.internal.Logging;
import launcher.ui_areas.lobby.json_editor.JsonEditor;
import com.jfoenix.controls.JFXToggleButton;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages JSON persistence to/from file system.
 * 
 * <p>This class has a single responsibility: managing persistence of JSON content,
 * persistence toggle state, and selected game name to/from the file system.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Loading saved JSON content and persistence settings on startup</li>
 *   <li>Saving JSON content when persistence is enabled</li>
 *   <li>Managing persistence toggle state</li>
 *   <li>Persisting selected game name</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since 1.0
 */
public class JsonPersistenceManager {
    
    // ==================== CONSTANTS ====================
    
    /** File file_paths for persisted JSON content. */
    private static final String JSON_PERSISTENCE_FILE = "saved/gdk-json_processing-persistence.txt";
    
    /** File file_paths for persistence toggle state. */
    private static final String PERSISTENCE_TOGGLE_FILE = "saved/gdk-persistence-toggle.txt";
    
    /** File file_paths for selected game name. */
    private static final String SELECTED_GAME_FILE = "saved/gdk-selected-game.txt";
    
    // ==================== DEPENDENCIES ====================
    
    /** The JSON input editor to save/load content from. */
    private final JsonEditor jsonInputEditor;
    
    /** The toggle button that controls persistence state. */
    private final JFXToggleButton jsonPersistenceToggle;
    
    // ==================== STATE ====================
    
    /** Flag to prevent message spam during persistence settings ui_loading. */
    private boolean isLoadingPersistenceSettings = false;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new JsonPersistenceManager.
     * 
     * @param jsonInputEditor The JSON input editor to save/load content from
     * @param jsonPersistenceToggle The toggle button for persistence state
     */
    public JsonPersistenceManager(JsonEditor jsonInputEditor, JFXToggleButton jsonPersistenceToggle) {
        this.jsonInputEditor = jsonInputEditor;
        this.jsonPersistenceToggle = jsonPersistenceToggle;
    }
    
    // ==================== PUBLIC METHODS - LOADING ====================
    
    /**
     * Loads saved JSON content and persistence toggle state on startup.
     * 
     * <p>This method:
     * <ul>
     *   <li>Loads the persistence toggle state from file</li>
     *   <li>If persistence is enabled, loads the saved JSON content</li>
     *   <li>Sets a flag to prevent persistence messages during ui_loading</li>
     * </ul>
     */
    public void loadPersistenceSettings() {
        try {
            // Set flag to prevent persistence messages during ui_loading
            isLoadingPersistenceSettings = true;
            
            // Load persistence toggle state
            loadPersistenceToggleState();
            
            // Load saved JSON content if persistence is enabled
            if (jsonPersistenceToggle.isSelected()) {
                loadSavedJsonContent();
            }
            
            // Clear flag after ui_loading is complete
            isLoadingPersistenceSettings = false;
            
        } catch (Exception e) {
            Logging.error("❌ Error ui_loading persistence settings: " + e.getMessage(), e);
            // Clear flag even on error
            isLoadingPersistenceSettings = false;
        }
    }
    
    // ==================== PUBLIC METHODS - SAVING ====================
    
    /**
     * Saves JSON content to file if persistence is enabled.
     * 
     * <p>This method only saves if the persistence toggle is selected.
     * If persistence is disabled, this method returns without saving.
     */
    public void saveJsonContent() {
        if (!jsonPersistenceToggle.isSelected()) {
            return; // Don't save if persistence is disabled
        }
        
        try {
            ensureSavedDirectoryExists();
            String jsonContent = jsonInputEditor.getText();
            Path jsonFile = Paths.get(JSON_PERSISTENCE_FILE);
            Files.writeString(jsonFile, jsonContent);
            Logging.info("📋 Saved JSON input content to file");
        } catch (Exception e) {
            Logging.error("❌ Error saving JSON content: " + e.getMessage(), e);
        }
    }
    
    /**
     * Saves persistence toggle state to file.
     * 
     * <p>The toggle state is saved as a boolean string representation.
     */
    public void savePersistenceToggleState() {
        try {
            ensureSavedDirectoryExists();
            boolean isEnabled = jsonPersistenceToggle.isSelected();
            Path toggleFile = Paths.get(PERSISTENCE_TOGGLE_FILE);
            Files.writeString(toggleFile, String.valueOf(isEnabled));
            Logging.info("📋 Saved persistence toggle state: " + (isEnabled ? "enabled" : "disabled"));
        } catch (Exception e) {
            Logging.error("❌ Error saving persistence toggle state: " + e.getMessage(), e);
        }
    }
    
    /**
     * Persists the selected game module's name to file.
     * 
     * <p>This method silently ignores null or "None" game names.
     * Failures are silently ignored as this is not a critical operation.
     * 
     * @param gameName The name of the selected game module
     */
    public void persistSelectedGame(String gameName) {
        try {
            if (gameName == null || gameName.equals("None")) return;
            ensureSavedDirectoryExists();
            Files.writeString(Paths.get(SELECTED_GAME_FILE), gameName);
        } catch (Exception ignored) {
            // Silently fail - not critical
        }
    }
    
    // ==================== PUBLIC METHODS - UTILITY ====================
    
    /**
     * Checks if currently ui_loading persistence settings.
     * 
     * <p>This flag prevents message spam during the initial load operation.
     * 
     * @return true if ui_loading, false otherwise
     */
    public boolean isLoadingPersistenceSettings() {
        return isLoadingPersistenceSettings;
    }
    
    /**
     * Clears the JSON persistence file.
     * 
     * <p>This method deletes the persisted JSON content file if it exists.
     */
    public void clearJsonPersistenceFile() {
        try {
            Path jsonFile = Paths.get(JSON_PERSISTENCE_FILE);
            if (Files.exists(jsonFile)) {
                Files.delete(jsonFile);
                Logging.info("🗑️ Cleared JSON persistence file");
            }
        } catch (Exception e) {
            Logging.error("❌ Error clearing JSON persistence file: " + e.getMessage(), e);
        }
    }
    
    // ==================== PRIVATE METHODS - LOADING ====================
    
    /**
     * Loads the persistence toggle state from file.
     * 
     * <p>If the file doesn't exist or an error occurs, defaults to enabled (true).
     */
    private void loadPersistenceToggleState() {
        try {
            Path toggleFile = Paths.get(PERSISTENCE_TOGGLE_FILE);
            if (Files.exists(toggleFile)) {
                String toggleState = Files.readString(toggleFile).trim();
                boolean isEnabled = Boolean.parseBoolean(toggleState);
                jsonPersistenceToggle.setSelected(isEnabled);
            } else {
                // Default to enabled if no saved state
                jsonPersistenceToggle.setSelected(true);
            }
        } catch (Exception e) {
            Logging.error("❌ Error ui_loading persistence toggle state: " + e.getMessage(), e);
            // Default to enabled on error
            jsonPersistenceToggle.setSelected(true);
        }
    }
    
    /**
     * Loads saved JSON content from file.
     * 
     * <p>If the file doesn't exist, this method does nothing.
     * Errors are logged but do not throw exceptions.
     */
    private void loadSavedJsonContent() {
        try {
            Path jsonFile = Paths.get(JSON_PERSISTENCE_FILE);
            if (Files.exists(jsonFile)) {
                String savedJson = Files.readString(jsonFile);
                jsonInputEditor.setText(savedJson);
            }
        } catch (Exception e) {
            Logging.error("❌ Error ui_loading saved JSON content: " + e.getMessage(), e);
        }
    }
    
    // ==================== PRIVATE METHODS - UTILITY ====================
    
    /**
     * Ensures the saved directory exists, creating it if necessary.
     * 
     * <p>This method creates the "saved" directory if it doesn't exist.
     * Errors are logged but do not throw exceptions.
     */
    private void ensureSavedDirectoryExists() {
        try {
            Path savedDir = Paths.get("saved");
            if (!Files.exists(savedDir)) {
                Files.createDirectories(savedDir);
                Logging.info("📁 Created saved directory: " + savedDir.toAbsolutePath());
            }
        } catch (Exception e) {
            Logging.error("❌ Failed to create saved directory: " + e.getMessage());
        }
    }
}

