package launcher.ui.settings_page;

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

import launcher.ui.lobby.gui.lobby.ui_logic.GDKGameLobbyController;

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
     * Sets the main GDK lobby controller reference.
     * 
     * <p>This method is called by the parent component to provide a reference
     * to the main GDK lobby controller. This allows the settings controller
     * to synchronize settings with the main controller.
     * 
     * @param mainController The main GDK lobby controller
     */
    public void setMainController(GDKGameLobbyController mainController) {
        this.mainController = mainController;
        // Synchronize current settings from main controller
        synchronizeWithMainController();
    }
    
    /**
     * Sets the main scene reference for navigation back to lobby.
     * 
     * <p>This method is called by the parent component to provide a reference
     * to the main lobby scene. This allows the settings controller to navigate
     * back to the lobby when the back button is clicked.
     * 
     * @param mainScene The main lobby scene
     */
    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
        Logging.info("⚙️ Main scene reference set for navigation");
    }
    
    // ==================== USER INTERFACE SETUP ====================
    
    // ==================== USER INTERFACE SETUP ====================
    
    /**
     * Sets up all user interface components.
     * 
     * <p>This method initializes UI components such as the theme selector
     * and font size slider with their default values and configurations.
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
    
    /**
     * Sets up all event handlers for user interactions.
     * 
     * <p>This method configures event handlers for buttons and other
     * interactive UI components that trigger actions.
     */
    private void setupEventHandlers() {
        // Back button: Return to main lobby
        backButton.setOnAction(event -> returnToMainLobby());
    }
    
    /**
     * Sets up listeners for all settings changes.
     * 
     * <p>This method configures change listeners for all settings controls.
     * When a setting changes, it is automatically saved to preferences.
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
     * Creates a settings change listener for a specific setting.
     * 
     * <p>This method creates a generic change listener that automatically
     * saves the setting value to preferences when it changes. The listener
     * only saves if initialization is complete and the new value is not null.
     * 
     * @param settingName The name of the setting to save
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
    
    /**
     * Loads all saved settings from preferences.
     * 
     * <p>This method loads all settings from Java Preferences and applies
     * them to the UI components. The initialization flag is set to prevent
     * triggering save operations during loading.
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
     * Saves a single setting to preferences.
     * 
     * <p>This method saves a single setting key-value pair to Java Preferences.
     * Errors are logged but do not throw exceptions.
     * 
     * @param key The setting key
     * @param value The setting value (as a string)
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
     * Saves all current settings to preferences.
     * 
     * <p>This method saves all current UI component values to Java Preferences.
     * This is typically called when navigating away from the settings page
     * to ensure all changes are persisted.
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
    
    /**
     * Synchronizes settings with the main GDK lobby controller.
     * 
     * <p>This method synchronizes settings between the settings page controller
     * and the main GDK lobby controller. Currently, this is a placeholder for
     * future synchronization logic.
     */
    private void synchronizeWithMainController() {
        if (mainController != null) {
            // This method can be used to sync settings between controllers
            // For now, we'll just log that synchronization is available
            Logging.info("⚙️ Settings controller synchronized with main controller");
        }
    }
    
    /**
     * Updates the status label with a message.
     * 
     * <p>This method updates the status label to display feedback messages
     * to the user about settings operations.
     * 
     * @param message The status message to display
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    /**
     * Returns to the main lobby.
     * 
     * <p>This method saves all current settings, then navigates back to the
     * main lobby scene by changing the stage's scene. This ensures all settings
     * are persisted before leaving the settings page.
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
     * Gets the current auto-launch setting.
     * 
     * <p>This method returns whether auto-launch is currently enabled.
     * Returns false if the toggle button is not available.
     * 
     * @return true if auto-launch is enabled, false otherwise
     */
    public boolean isAutoLaunchEnabled() {
        return settingsAutoLaunchToggle != null ? settingsAutoLaunchToggle.isSelected() : false;
    }
    
    /**
     * Gets the current auto-select game setting.
     * 
     * <p>This method returns whether auto-select game is currently enabled.
     * Returns false if the toggle button is not available.
     * 
     * @return true if auto-select game is enabled, false otherwise
     */
    public boolean isAutoSelectGameEnabled() {
        return settingsAutoSelectGameToggle != null ? settingsAutoSelectGameToggle.isSelected() : false;
    }
    
    /**
     * Gets the current JSON persistence setting.
     * 
     * <p>This method returns whether JSON persistence is currently enabled.
     * Returns true (default) if the toggle button is not available.
     * 
     * @return true if JSON persistence is enabled, false otherwise
     */
    public boolean isJsonPersistenceEnabled() {
        return settingsJsonPersistenceToggle != null ? settingsJsonPersistenceToggle.isSelected() : true;
    }
    
    /**
     * Gets the current theme setting.
     * 
     * <p>This method returns the currently selected theme name.
     * Returns "System Default" if the theme selector is not available.
     * 
     * @return The selected theme name
     */
    public String getSelectedTheme() {
        return settingsThemeSelector != null ? settingsThemeSelector.getValue() : "System Default";
    }
    
    /**
     * Gets the current font size setting.
     * 
     * <p>This method returns the currently selected font size in pixels.
     * Returns 12 (default) if the font size slider is not available.
     * 
     * @return The selected font size in pixels
     */
    public int getFontSize() {
        return settingsFontSizeSlider != null ? (int) settingsFontSizeSlider.getValue() : 12;
    }
} 