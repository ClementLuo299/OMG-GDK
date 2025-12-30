package launcher.gui.lobby.ui_logic.managers.game_launching;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.gui.lobby.GDKViewModel;
import launcher.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;
import launcher.utils.game.GameLaunchUtil;

import java.util.Map;

/**
 * Manages game launching operations for the lobby controller.
 * 
 * <p>This manager coordinates the game launch process by:
 * <ul>
 *   <li>Validating launch prerequisites (ViewModel availability, game selection)</li>
 *   <li>Parsing and applying JSON configuration data</li>
 *   <li>Delegating to ViewModel and utility classes for actual launch execution</li>
 *   <li>Handling errors through the error handler</li>
 * </ul>
 * 
 * <p>Supports two launch modes:
 * <ul>
 *   <li><b>UI launch</b>: User-initiated launch from the lobby UI</li>
 *   <li><b>Auto-launch</b>: Automatic launch with saved configuration</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date December 30, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class GameLaunchingManager {
    
    // ==================== DEPENDENCIES ====================
    
    /** Application ViewModel for business logic operations (validation, launch). */
    private final GDKViewModel applicationViewModel;
    
    /** Controller for JSON input/action buttons in the UI. */
    private final JsonActionButtonsController jsonActionButtonsController;
    
    /** Error handler for reporting launch failures. */
    private final GameLaunchErrorHandler errorHandler;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new GameLaunchingManager.
     * 
     * @param applicationViewModel The application ViewModel for launching games
     * @param jsonActionButtonsController The JSON action buttons controller
     * @param errorHandler The error handler for launch errors
     */
    public GameLaunchingManager(GDKViewModel applicationViewModel,
                             JsonActionButtonsController jsonActionButtonsController,
                             GameLaunchErrorHandler errorHandler) {
        this.applicationViewModel = applicationViewModel;
        this.jsonActionButtonsController = jsonActionButtonsController;
        this.errorHandler = errorHandler;
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Launches a game from UI interaction (user clicked launch button).
     * 
     * <p>This method:
     * <ol>
     *   <li>Retrieves JSON configuration from the UI</li>
     *   <li>Parses and validates the JSON</li>
     *   <li>Launches the game with the parsed configuration</li>
     * </ol>
     * 
     * @param selectedGameModule The selected game module to launch
     * @return true if launch was successful, false otherwise
     */
    public boolean launchGameFromUI(GameModule selectedGameModule) {
        Logging.info("Launch button clicked for game: " + 
            (selectedGameModule != null ? selectedGameModule.getMetadata().getGameName() : "null"));
        
        // Get JSON text from UI
        String jsonText = getJsonTextFromUI();
        
        // Parse and validate JSON configuration
        Map<String, Object> jsonConfigurationData = parseJsonConfiguration(jsonText, false);
        if (jsonConfigurationData == null && !jsonText.isEmpty()) {
            // Invalid JSON syntax - error already reported by parseJsonConfiguration
            return false;
        }
        
        // Launch with UI configuration (not auto-launch)
        return launchGame(selectedGameModule, jsonConfigurationData, false);
    }
    
    /**
     * Launches a game with saved JSON configuration (for auto-launch).
     * 
     * <p>This method is used for automatic launches with previously saved configuration.
     * If JSON parsing fails, the launch continues with default configuration.
     * 
     * @param gameModule The game module to launch
     * @param savedJson The saved JSON configuration string
     * @return true if launch was successful, false otherwise
     */
    public boolean launchGameWithSavedJson(GameModule gameModule, String savedJson) {
        try {
            // Parse the saved JSON (silently fail if invalid, use defaults)
            Map<String, Object> jsonConfigurationData = parseJsonConfiguration(savedJson, true);
            
            // Launch the game with the saved configuration (auto-launch mode)
            return launchGame(gameModule, jsonConfigurationData, true);
        } catch (Exception e) {
            Logging.error("Auto-launch: Error launching game with saved JSON: " + e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Core launch method that performs validation and execution.
     * 
     * <p>This method:
     * <ol>
     *   <li>Validates ViewModel availability</li>
     *   <li>Validates game launch prerequisites</li>
     *   <li>Applies JSON configuration to the game module</li>
     *   <li>Executes the launch via ViewModel</li>
     * </ol>
     * 
     * @param selectedGameModule The game module to launch
     * @param jsonConfigurationData The parsed JSON configuration data (may be null)
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     * @return true if launch was successful, false otherwise
     */
    private boolean launchGame(GameModule selectedGameModule, 
                              Map<String, Object> jsonConfigurationData, 
                              boolean isAutoLaunch) {
        // Validate ViewModel is available
        if (!validateViewModel(isAutoLaunch)) {
            return false;
        }
        
        // Validate game launch using ViewModel (business logic)
        if (!validateGameLaunch(selectedGameModule, isAutoLaunch)) {
            return false;
        }
        
        String gameName = selectedGameModule.getMetadata().getGameName();
        Logging.info("Preparing to launch game: " + gameName);
        
        // Apply JSON configuration to the game module
        if (!applyGameConfiguration(selectedGameModule, jsonConfigurationData, gameName, isAutoLaunch)) {
            return false;
        }
        
        // Execute the launch via ViewModel
        return executeLaunch(selectedGameModule, gameName, isAutoLaunch);
    }
    
    /**
     * Validates that the ViewModel is available.
     * 
     * @param isAutoLaunch Whether this is an auto-launch
     * @return true if ViewModel is available, false otherwise
     */
    private boolean validateViewModel(boolean isAutoLaunch) {
        if (applicationViewModel == null) {
            errorHandler.handleViewModelUnavailable(isAutoLaunch);
            return false;
        }
        return true;
    }
    
    /**
     * Validates that the game launch is allowed.
     * 
     * @param selectedGameModule The game module to validate
     * @param isAutoLaunch Whether this is an auto-launch
     * @return true if validation passes, false otherwise
     */
    private boolean validateGameLaunch(GameModule selectedGameModule, boolean isAutoLaunch) {
        GDKViewModel.LaunchValidationResult validation = 
            applicationViewModel.validateGameLaunch(selectedGameModule);
        
        if (!validation.isValid()) {
            errorHandler.handleValidationError(validation.errorMessage(), isAutoLaunch);
            return false;
        }
        return true;
    }
    
    /**
     * Applies JSON configuration to the game module.
     * 
     * @param selectedGameModule The game module to configure
     * @param jsonConfigurationData The parsed JSON configuration data
     * @param gameName The name of the game (for logging/errors)
     * @param isAutoLaunch Whether this is an auto-launch
     * @return true if configuration was successful, false otherwise
     */
    private boolean applyGameConfiguration(GameModule selectedGameModule,
                                          Map<String, Object> jsonConfigurationData,
                                          String gameName,
                                          boolean isAutoLaunch) {
        boolean configSuccess = GameLaunchUtil.launchGameWithConfiguration(
            selectedGameModule, jsonConfigurationData, isAutoLaunch);
        
        if (!configSuccess) {
            errorHandler.handleConfigurationFailure(gameName, isAutoLaunch);
            return false;
        }
        return true;
    }
    
    /**
     * Executes the actual game launch via ViewModel.
     * 
     * @param selectedGameModule The game module to launch
     * @param gameName The name of the game (for logging/errors)
     * @param isAutoLaunch Whether this is an auto-launch
     * @return true if launch was successful, false otherwise
     */
    private boolean executeLaunch(GameModule selectedGameModule, String gameName, boolean isAutoLaunch) {
        // Get the JSON text for the ViewModel to check game mode
        String jsonText = getJsonTextFromUI();
        
        try {
            Logging.info("Calling ViewModel to launch game: " + gameName);
            applicationViewModel.handleLaunchGame(selectedGameModule, jsonText);
            errorHandler.reportSuccessfulLaunch(gameName);
            return true;
        } catch (Exception e) {
            errorHandler.handleLaunchException(gameName, e, isAutoLaunch);
            return false;
        }
    }
    
    /**
     * Gets JSON text from the UI editor.
     * 
     * @return JSON text string, or empty string if not available
     */
    private String getJsonTextFromUI() {
        return jsonActionButtonsController != null ? 
            jsonActionButtonsController.getJsonInputEditor().getText().trim() : "";
    }
    
    /**
     * Parses JSON configuration text into a map.
     * 
     * @param jsonText The JSON text to parse (may be null or empty)
     * @param isAutoLaunch Whether this is for auto-launch (affects error handling)
     * @return Parsed configuration map, or null if parsing failed or text was empty
     */
    private Map<String, Object> parseJsonConfiguration(String jsonText, boolean isAutoLaunch) {
        if (applicationViewModel == null || jsonText == null || jsonText.trim().isEmpty()) {
            return null;
        }
        
        Map<String, Object> jsonConfigurationData = applicationViewModel.parseJsonConfiguration(jsonText);
        
        if (jsonConfigurationData == null) {
            if (isAutoLaunch) {
                // For auto-launch, silently fail and use defaults
                Logging.error("Auto-launch: Failed to parse saved JSON");
            } else {
                // For UI launch, report error to user
                errorHandler.handleValidationError(
                    "Invalid JSON syntax - Please enter valid JSON configuration.", false);
            }
        }
        
        return jsonConfigurationData;
    }
}

