package launcher.gui.settings_page;

import gdk.internal.Logging;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.value.ChangeListener;
import com.jfoenix.controls.JFXToggleButton;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import launcher.gui.lobby.ui_logic.GDKGameLobbyController;

/**
 * Controller for the GDK Settings Page.
 * 
 * This class manages the settings interface where users can configure
 * various application preferences, game settings, and advanced options.
 * 
 * Key responsibilities:
 * - Manage all application settings and preferences
 * - Handle settings persistence and restoration
 * - Provide real-time settings updates
 * - Integrate with the main GDK lobby controller
 *
 * @authors Clement Luo
 * @date August 22, 2025
 * @edited August 22, 2025
 * @since 1.0
 */
public class SettingsPageController implements Initializable {

    // ==================== FXML INJECTIONS ====================
    
    // Navigation and Control
    @FXML private Button backButton;
    @FXML private Label statusLabel;
    
    // Settings Controls
    @FXML private JFXToggleButton settingsAutoLaunchToggle;
    @FXML private JFXToggleButton settingsAutoSelectGameToggle;
    @FXML private JFXToggleButton settingsJsonPersistenceToggle;
    @FXML private ComboBox<String> settingsThemeSelector;
    @FXML private Slider settingsFontSizeSlider;
    @FXML private Label settingsFontSizeLabel;
    @FXML private JFXToggleButton settingsAutoRefreshToggle;
    @FXML private JFXToggleButton settingsCompilationCheckToggle;
    @FXML private JFXToggleButton settingsDebugModeToggle;
    @FXML private JFXToggleButton settingsPerformanceModeToggle;
    
    // ==================== DEPENDENCIES ====================
    
    /**
     * Reference to the main GDK lobby controller for settings synchronization
     */
    private GDKGameLobbyController mainController;
    
    /**
     * Reference to the main scene for navigation back to lobby
     */
    private Scene mainScene;
    
    /**
     * Java preferences for persistent settings storage
     */
    private Preferences preferences;
    
    /**
     * Flag to prevent settings change events during initialization
     */
    private boolean isInitializing = false;
    
    // ==================== INITIALIZATION ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logging.info("⚙️ Initializing GDK Settings Page Controller");
        
        // Initialize preferences
        preferences = Preferences.userNodeForPackage(SettingsPageController.class);
        
        // Set up the UI components
        setupUserInterface();
        
        // Set up event handlers
        setupEventHandlers();
        
        // Load saved settings
        loadSettings();
        
        // Set up settings change listeners
        setupSettingsListeners();
        
        Logging.info("✅ GDK Settings Page Controller initialized successfully");
    }
    
    // ==================== DEPENDENCY INJECTION ====================
    
    /**
     * Set the main GDK lobby controller reference.
     * 
     * @param mainController The main GDK lobby controller
     */
    public void setMainController(GDKGameLobbyController mainController) {
        this.mainController = mainController;
        // Synchronize current settings from main controller
        synchronizeWithMainController();
    }
    
    /**
     * Set the main scene reference for navigation back to lobby.
     * 
     * @param mainScene The main lobby scene
     */
    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
        Logging.info("⚙️ Main scene reference set for navigation");
    }
    
    // ==================== USER INTERFACE SETUP ====================
    
    /**
     * Set up all user interface components.
     */
    private void setupUserInterface() {
        // Theme selector setup (with null check)
        if (settingsThemeSelector != null) {
            ObservableList<String> themes = FXCollections.observableArrayList("Light", "Dark", "System Default");
            settingsThemeSelector.setItems(themes);
            settingsThemeSelector.setValue("System Default");
        }
        
        // Font size slider setup (with null check)
        if (settingsFontSizeSlider != null && settingsFontSizeLabel != null) {
            settingsFontSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (!isInitializing) {
                    settingsFontSizeLabel.setText(newValue.intValue() + "px");
                }
            });
        }
    }
    
    // ==================== EVENT HANDLER SETUP ====================
    
    /**
     * Set up all event handlers for user interactions.
     */
    private void setupEventHandlers() {
        // Back button: Return to main lobby
        backButton.setOnAction(event -> returnToMainLobby());
    }
    
    // ==================== SETTINGS LISTENERS ====================
    
    /**
     * Set up listeners for all settings changes.
     */
    private void setupSettingsListeners() {
        // General settings listeners (with null checks)
        if (settingsAutoLaunchToggle != null) {
            settingsAutoLaunchToggle.selectedProperty().addListener(createSettingsListener("autoLaunch"));
        }
        if (settingsAutoSelectGameToggle != null) {
            settingsAutoSelectGameToggle.selectedProperty().addListener(createSettingsListener("autoSelectGame"));
        }
        if (settingsJsonPersistenceToggle != null) {
            settingsJsonPersistenceToggle.selectedProperty().addListener(createSettingsListener("jsonPersistence"));
        }
        if (settingsThemeSelector != null) {
            settingsThemeSelector.valueProperty().addListener(createSettingsListener("theme"));
        }
        if (settingsFontSizeSlider != null) {
            settingsFontSizeSlider.valueProperty().addListener(createSettingsListener("fontSize"));
        }
        
        // Game settings listeners (with null checks)
        if (settingsAutoRefreshToggle != null) {
            settingsAutoRefreshToggle.selectedProperty().addListener(createSettingsListener("autoRefresh"));
        }
        if (settingsCompilationCheckToggle != null) {
            settingsCompilationCheckToggle.selectedProperty().addListener(createSettingsListener("compilationCheck"));
        }
        
        // Advanced settings listeners (with null checks)
        if (settingsDebugModeToggle != null) {
            settingsDebugModeToggle.selectedProperty().addListener(createSettingsListener("debugMode"));
        }
        if (settingsPerformanceModeToggle != null) {
            settingsPerformanceModeToggle.selectedProperty().addListener(createSettingsListener("performanceMode"));
        }

    }
    
    /**
     * Create a settings change listener for a specific setting.
     * 
     * @param settingName The name of the setting
     * @return A ChangeListener that saves the setting when it changes
     */
    private <T> ChangeListener<T> createSettingsListener(String settingName) {
        return (observable, oldValue, newValue) -> {
            if (!isInitializing && newValue != null) {
                saveSetting(settingName, newValue.toString());
                updateStatus("Setting '" + settingName + "' updated");
            }
        };
    }
    
    // ==================== SETTINGS MANAGEMENT ====================
    
    /**
     * Load all saved settings from preferences.
     */
    private void loadSettings() {
        isInitializing = true;
        
        try {
                    // General settings (with null checks)
        if (settingsAutoLaunchToggle != null) {
            settingsAutoLaunchToggle.setSelected(preferences.getBoolean("autoLaunch", false));
        }
        if (settingsAutoSelectGameToggle != null) {
            settingsAutoSelectGameToggle.setSelected(preferences.getBoolean("autoSelectGame", false));
        }
        if (settingsJsonPersistenceToggle != null) {
            settingsJsonPersistenceToggle.setSelected(preferences.getBoolean("jsonPersistence", true));
        }
        
        if (settingsThemeSelector != null) {
            String theme = preferences.get("theme", "System Default");
            settingsThemeSelector.setValue(theme);
        }
        
        if (settingsFontSizeSlider != null && settingsFontSizeLabel != null) {
            double fontSize = preferences.getDouble("fontSize", 12.0);
            settingsFontSizeSlider.setValue(fontSize);
            settingsFontSizeLabel.setText((int)fontSize + "px");
        }
        
        // Game settings (with null checks)
        if (settingsAutoRefreshToggle != null) {
            settingsAutoRefreshToggle.setSelected(preferences.getBoolean("autoRefresh", false));
        }
        if (settingsCompilationCheckToggle != null) {
            settingsCompilationCheckToggle.setSelected(preferences.getBoolean("compilationCheck", true));
        }
        
        // Advanced settings (with null checks)
        if (settingsDebugModeToggle != null) {
            settingsDebugModeToggle.setSelected(preferences.getBoolean("debugMode", false));
        }
        
        if (settingsPerformanceModeToggle != null) {
            settingsPerformanceModeToggle.setSelected(preferences.getBoolean("performanceMode", false));
        }
            
            updateStatus("Settings loaded successfully");
            
        } catch (Exception e) {
            Logging.error("❌ Error loading settings: " + e.getMessage(), e);
            updateStatus("Error loading settings: " + e.getMessage());
        } finally {
            isInitializing = false;
        }
    }
    
    /**
     * Save a single setting to preferences.
     * 
     * @param key The setting key
     * @param value The setting value
     */
    private void saveSetting(String key, String value) {
        try {
            preferences.put(key, value);
            Logging.info("⚙️ Setting saved: " + key + " = " + value);
        } catch (Exception e) {
            Logging.error("❌ Error saving setting " + key + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Save all current settings to preferences.
     */
    private void saveAllSettings() {
        try {
                    // General settings (with null checks)
        if (settingsAutoLaunchToggle != null) {
            preferences.putBoolean("autoLaunch", settingsAutoLaunchToggle.isSelected());
        }
        if (settingsAutoSelectGameToggle != null) {
            preferences.putBoolean("autoSelectGame", settingsAutoSelectGameToggle.isSelected());
        }
        if (settingsJsonPersistenceToggle != null) {
            preferences.putBoolean("jsonPersistence", settingsJsonPersistenceToggle.isSelected());
        }
            if (settingsThemeSelector != null) {
                preferences.put("theme", settingsThemeSelector.getValue());
            }
            if (settingsFontSizeSlider != null) {
                preferences.putDouble("fontSize", settingsFontSizeSlider.getValue());
            }
            
            // Game settings (with null checks)
            if (settingsAutoRefreshToggle != null) {
                preferences.putBoolean("autoRefresh", settingsAutoRefreshToggle.isSelected());
            }
            if (settingsCompilationCheckToggle != null) {
                preferences.putBoolean("compilationCheck", settingsCompilationCheckToggle.isSelected());
            }
            
            // Advanced settings (with null checks)
            if (settingsDebugModeToggle != null) {
                preferences.putBoolean("debugMode", settingsDebugModeToggle.isSelected());
            }
            if (settingsPerformanceModeToggle != null) {
                preferences.putBoolean("performanceMode", settingsPerformanceModeToggle.isSelected());
            }
            
            updateStatus("All settings saved successfully");
            Logging.info("⚙️ All settings saved successfully");
            
        } catch (Exception e) {
            Logging.error("❌ Error saving all settings: " + e.getMessage(), e);
            updateStatus("Error saving settings: " + e.getMessage());
        }
    }
    
    // ==================== MAIN CONTROLLER INTEGRATION ====================
    
    /**
     * Synchronize settings with the main GDK lobby controller.
     */
    private void synchronizeWithMainController() {
        if (mainController != null) {
            // This method can be used to sync settings between controllers
            // For now, we'll just log that synchronization is available
            Logging.info("⚙️ Settings controller synchronized with main controller");
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Update the status label with a message.
     * 
     * @param message The status message to display
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    /**
     * Return to the main lobby.
     */
    private void returnToMainLobby() {
        try {
            // Save any unsaved changes
            saveAllSettings();
            
            // Get the current stage and change back to the main scene
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            if (currentStage != null && mainScene != null) {
                currentStage.setScene(mainScene);
                currentStage.setTitle("OMG Game Development Kit");
                Logging.info("🔄 Returned to main lobby");
            } else {
                Logging.warning("⚠️ Could not return to main lobby - scene references missing");
            }
            
        } catch (Exception e) {
            Logging.error("❌ Error returning to main lobby: " + e.getMessage(), e);
        }
    }
    
    // ==================== PUBLIC API ====================
    
    /**
     * Get the current auto-launch setting.
     * 
     * @return true if auto-launch is enabled
     */
    public boolean isAutoLaunchEnabled() {
        return settingsAutoLaunchToggle != null ? settingsAutoLaunchToggle.isSelected() : false;
    }
    
    /**
     * Get the current auto-select game setting.
     * 
     * @return true if auto-select game is enabled
     */
    public boolean isAutoSelectGameEnabled() {
        return settingsAutoSelectGameToggle != null ? settingsAutoSelectGameToggle.isSelected() : false;
    }
    
    /**
     * Get the current JSON persistence setting.
     * 
     * @return true if JSON persistence is enabled
     */
    public boolean isJsonPersistenceEnabled() {
        return settingsJsonPersistenceToggle != null ? settingsJsonPersistenceToggle.isSelected() : true;
    }
    
    /**
     * Get the current theme setting.
     * 
     * @return The selected theme name
     */
    public String getSelectedTheme() {
        return settingsThemeSelector != null ? settingsThemeSelector.getValue() : "System Default";
    }
    
    /**
     * Get the current font size setting.
     * 
     * @return The selected font size
     */
    public int getFontSize() {
        return settingsFontSizeSlider != null ? (int) settingsFontSizeSlider.getValue() : 12;
    }
} 