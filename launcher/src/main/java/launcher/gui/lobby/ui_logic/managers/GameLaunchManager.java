package launcher.gui.lobby.ui_logic.managers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;
import launcher.utils.game.GameLaunchUtil;

import java.util.Map;

/**
 * Manages game launching operations for the lobby controller.
 * Handles coordination with the ViewModel and game launch execution.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class GameLaunchManager {
    
    private final GDKViewModel applicationViewModel;
    private final JsonActionButtonsController jsonActionButtonsController;
    private final GameLaunchErrorHandler errorHandler;
    
    /**
     * Create a new GameLaunchManager.
     * 
     * @param applicationViewModel The application ViewModel for launching games
     * @param jsonActionButtonsController The JSON action buttons controller
     * @param errorHandler The error handler for launch errors
     */
    public GameLaunchManager(GDKViewModel applicationViewModel,
                             JsonActionButtonsController jsonActionButtonsController,
                             GameLaunchErrorHandler errorHandler) {
        this.applicationViewModel = applicationViewModel;
        this.jsonActionButtonsController = jsonActionButtonsController;
        this.errorHandler = errorHandler;
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
            errorHandler.handleViewModelUnavailable(isAutoLaunch);
            return false;
        }
        
        // Validate game launch using ViewModel (business logic)
        GDKViewModel.LaunchValidationResult validation = applicationViewModel.validateGameLaunch(selectedGameModule);
        if (!validation.isValid()) {
            errorHandler.handleValidationError(validation.errorMessage(), isAutoLaunch);
            return false;
        }
        
        String gameName = selectedGameModule.getMetadata().getGameName();
        Logging.info("🚀 Preparing to launch game: " + gameName);
        
        // Use the utility class for the core launch logic
        boolean configSuccess = GameLaunchUtil.launchGameWithConfiguration(selectedGameModule, jsonConfigurationData, isAutoLaunch);
        
        if (!configSuccess) {
            errorHandler.handleConfigurationFailure(gameName, isAutoLaunch);
            return false;
        }
        
        // Get the JSON text for the ViewModel to check game mode
        String jsonText = jsonActionButtonsController != null ? 
            jsonActionButtonsController.getJsonInputEditor().getText().trim() : "";
        
        // Launch the game using the ViewModel
        try {
            Logging.info("🎮 Calling ViewModel to launch game: " + gameName);
            applicationViewModel.handleLaunchGame(selectedGameModule, jsonText);
            errorHandler.reportSuccessfulLaunch(gameName);
            return true;
        } catch (Exception e) {
            errorHandler.handleLaunchException(gameName, e, isAutoLaunch);
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
        String jsonText = jsonActionButtonsController != null ? 
            jsonActionButtonsController.getJsonInputEditor().getText().trim() : "";
        
        // Parse JSON using ViewModel (business logic)
        Map<String, Object> jsonConfigurationData = null;
        if (applicationViewModel != null && !jsonText.isEmpty()) {
            jsonConfigurationData = applicationViewModel.parseJsonConfiguration(jsonText);
            if (jsonConfigurationData == null) {
                // Invalid JSON syntax - report error
                errorHandler.handleValidationError("Invalid JSON syntax - Please enter valid JSON configuration.", false);
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

