package launcher.gui.lobby.managers;

import gdk.internal.Logging;
import launcher.gui.json_editor.SingleJsonEditor;
import com.jfoenix.controls.JFXToggleButton;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages JSON persistence to/from file system.
 * 
 * Handles saving and loading JSON content, toggle state, and selected game.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class JsonPersistenceManager {
    
    private static final String JSON_PERSISTENCE_FILE = "saved/gdk-json-persistence.txt";
    private static final String PERSISTENCE_TOGGLE_FILE = "saved/gdk-persistence-toggle.txt";
    private static final String SELECTED_GAME_FILE = "saved/gdk-selected-game.txt";
    
    private final SingleJsonEditor jsonInputEditor;
    private final JFXToggleButton jsonPersistenceToggle;
    
    private boolean isLoadingPersistenceSettings = false;
    
    /**
     * Create a new JsonPersistenceManager.
     * 
     * @param jsonInputEditor The JSON input editor to save/load
     * @param jsonPersistenceToggle The toggle button for persistence state
     */
    public JsonPersistenceManager(SingleJsonEditor jsonInputEditor, JFXToggleButton jsonPersistenceToggle) {
        this.jsonInputEditor = jsonInputEditor;
        this.jsonPersistenceToggle = jsonPersistenceToggle;
    }
    
    /**
     * Check if currently loading persistence settings (prevents message spam during load).
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoadingPersistenceSettings() {
        return isLoadingPersistenceSettings;
    }
    
    /**
     * Load saved JSON content and persistence toggle state on startup.
     */
    public void loadPersistenceSettings() {
        try {
            // Set flag to prevent persistence messages during loading
            isLoadingPersistenceSettings = true;
            
            // Load persistence toggle state
            loadPersistenceToggleState();
            
            // Load saved JSON content if persistence is enabled
            if (jsonPersistenceToggle.isSelected()) {
                loadSavedJsonContent();
            }
            
            // Clear flag after loading is complete
            isLoadingPersistenceSettings = false;
            
        } catch (Exception e) {
            Logging.error("❌ Error loading persistence settings: " + e.getMessage(), e);
            // Clear flag even on error
            isLoadingPersistenceSettings = false;
        }
    }
    
    /**
     * Load the persistence toggle state from file.
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
            Logging.error("❌ Error loading persistence toggle state: " + e.getMessage(), e);
            // Default to enabled on error
            jsonPersistenceToggle.setSelected(true);
        }
    }
    
    /**
     * Load saved JSON content from file.
     */
    private void loadSavedJsonContent() {
        try {
            Path jsonFile = Paths.get(JSON_PERSISTENCE_FILE);
            if (Files.exists(jsonFile)) {
                String savedJson = Files.readString(jsonFile);
                jsonInputEditor.setText(savedJson);
            }
        } catch (Exception e) {
            Logging.error("❌ Error loading saved JSON content: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save JSON content to file if persistence is enabled.
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
     * Save persistence toggle state to file.
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
     * Clear the JSON persistence file.
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
    
    /**
     * Persist the selected game module's name.
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
    
    /**
     * Ensure the saved directory exists.
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

