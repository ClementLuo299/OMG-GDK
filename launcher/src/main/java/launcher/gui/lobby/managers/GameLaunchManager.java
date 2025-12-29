package launcher.gui.lobby.managers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.subcontrollers.JsonConfigurationController;
import launcher.utils.game.GameLaunchUtil;
import launcher.utils.gui.DialogUtil;

import java.util.Map;

/**
 * Manages game launching operations for the lobby controller.
 * Handles validation, configuration parsing, and coordination with the ViewModel.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class GameLaunchManager {
    
    private final GDKViewModel applicationViewModel;
    private final JsonConfigurationController jsonConfigurationController;
    private final MessageManager messageManager;
    
    /**
     * Create a new GameLaunchManager.
     * 
     * @param applicationViewModel The application ViewModel for launching games
     * @param jsonConfigurationController The JSON configuration controller
     * @param messageManager The message manager for user feedback
     */
    public GameLaunchManager(GDKViewModel applicationViewModel,
                             JsonConfigurationController jsonConfigurationController,
                             MessageManager messageManager) {
        this.applicationViewModel = applicationViewModel;
        this.jsonConfigurationController = jsonConfigurationController;
        this.messageManager = messageManager;
    }
    
    /**
     * Launch a game with the provided configuration.
     * 
     * @param selectedGameModule The game module to launch
     * @param jsonConfigurationData The parsed JSON configuration data
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     * @return true if launch was successful, false otherwise
     */
    public boolean launchGame(GameModule selectedGameModule, Map<String, Object> jsonConfigurationData, boolean isAutoLaunch) {
        // Validate ViewModel is available
        if (applicationViewModel == null) {
            Logging.error("❌ applicationViewModel is null - cannot launch game");
            if (!isAutoLaunch) {
                DialogUtil.showError("Application Error", "Application ViewModel is not available. Please restart the application.");
                messageManager.addMessage("❌ Error: ViewModel not available");
            }
            return false;
        }
        
        // Validate game launch using ViewModel (business logic)
        GDKViewModel.LaunchValidationResult validation = applicationViewModel.validateGameLaunch(selectedGameModule);
        if (!validation.isValid()) {
            Logging.error("❌ Game launch validation failed: " + validation.errorMessage());
            if (!isAutoLaunch) {
                DialogUtil.showError("Launch Error", validation.errorMessage());
                messageManager.addMessage("❌ Error: " + validation.errorMessage());
            }
            return false;
        }
        
        String gameName = selectedGameModule.getMetadata().getGameName();
        Logging.info("🚀 Preparing to launch game: " + gameName);
        
        // Use the utility class for the core launch logic
        boolean configSuccess = GameLaunchUtil.launchGameWithConfiguration(selectedGameModule, jsonConfigurationData, isAutoLaunch);
        
        if (!configSuccess) {
            Logging.error("❌ Game configuration failed for: " + gameName);
            if (!isAutoLaunch) {
                DialogUtil.showError("Launch Error", "Failed to configure game: " + gameName + ". Check the logs for details.");
                messageManager.addMessage("❌ Failed to configure game: " + gameName);
            }
            return false;
        }
        
        // Get the JSON text for the ViewModel to check game mode
        String jsonText = jsonConfigurationController != null ? 
            jsonConfigurationController.getJsonInputEditor().getText().trim() : "";
        
        // Launch the game using the ViewModel
        try {
            Logging.info("🎮 Calling ViewModel to launch game: " + gameName);
            applicationViewModel.handleLaunchGame(selectedGameModule, jsonText);
            messageManager.addMessage("✅ Launching game: " + gameName);
            return true;
        } catch (Exception e) {
            Logging.error("❌ Exception while launching game: " + gameName, e);
            if (!isAutoLaunch) {
                DialogUtil.showError("Launch Error", "Failed to launch game: " + gameName + "\n\nError: " + e.getMessage());
                messageManager.addMessage("❌ Error launching game: " + e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Launch a game from UI interaction (user clicked launch button).
     * Validates selection and JSON before launching.
     * 
     * @param selectedGameModule The selected game module
     * @return true if launch was successful, false otherwise
     */
    public boolean launchGameFromUI(GameModule selectedGameModule) {
        Logging.info("🚀 Launch button clicked for game: " + 
            (selectedGameModule != null ? selectedGameModule.getMetadata().getGameName() : "null"));
        
        // Get JSON text from UI
        String jsonText = jsonConfigurationController != null ? 
            jsonConfigurationController.getJsonInputEditor().getText().trim() : "";
        
        // Parse JSON using ViewModel (business logic)
        Map<String, Object> jsonConfigurationData = null;
        if (applicationViewModel != null && !jsonText.isEmpty()) {
            jsonConfigurationData = applicationViewModel.parseJsonConfiguration(jsonText);
            if (jsonConfigurationData == null) {
                // Invalid JSON syntax - show UI error
                DialogUtil.showJsonError("Invalid JSON", "Please enter valid JSON configuration.");
                return false;
            }
        }
        
        // Launch with UI configuration
        return launchGame(selectedGameModule, jsonConfigurationData, false);
    }
    
    /**
     * Launch a game with saved JSON configuration (for auto-launch).
     * 
     * @param gameModule The game module to launch
     * @param savedJson The saved JSON configuration string
     * @return true if launch was successful, false otherwise
     */
    public boolean launchGameWithSavedJson(GameModule gameModule, String savedJson) {
        try {
            // Parse the saved JSON using ViewModel (business logic)
            Map<String, Object> jsonConfigurationData = null;
            if (applicationViewModel != null && savedJson != null && !savedJson.trim().isEmpty()) {
                jsonConfigurationData = applicationViewModel.parseJsonConfiguration(savedJson);
                if (jsonConfigurationData == null) {
                    Logging.error("Auto-launch: Failed to parse saved JSON");
                    // Continue with null configuration (let game use defaults)
                }
            }
            
            // Launch the game with the saved configuration
            return launchGame(gameModule, jsonConfigurationData, true);
        } catch (Exception e) {
            Logging.error("Auto-launch: Error launching game with saved JSON: " + e.getMessage(), e);
            return false;
        }
    }
}

