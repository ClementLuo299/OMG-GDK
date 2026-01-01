package launcher.ui_areas.lobby.managers.game_launching;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.game_launching.GameLaunchService;
import launcher.features.lobby_features.business.GDKViewModel;
import launcher.ui_areas.lobby.subcontrollers.JsonActionButtonsController;

import java.util.Map;

/**
 * Manages UI coordination for game launching operations.
 * 
 * <p>This manager handles UI-related operations for game launching:
 * <ul>
 *   <li>Retrieving JSON configuration from UI components</li>
 *   <li>Coordinating launch flow with business logic service</li>
 *   <li>Reporting errors and success to users via error handler</li>
 * </ul>
 * 
 * <p>Business logic (validation, parsing, launching) is delegated to {@link GameLaunchService}.
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
    
    /** Business logic service for game launching operations. */
    private final GameLaunchService launchService;
    
    /** Controller for JSON input/action buttons in the UI. */
    private final JsonActionButtonsController jsonActionButtonsController;
    
    /** Error handler for reporting launch failures to UI. */
    private final launcher.features.lobby_features.managers.game_launching.GameLaunchErrorHandler errorHandler;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new GameLaunchingManager.
     * 
     * @param launchService The business logic service for game launching
     * @param jsonActionButtonsController The JSON action buttons controller
     * @param errorHandler The error handler for launch errors
     */
    public GameLaunchingManager(GameLaunchService launchService,
                             JsonActionButtonsController jsonActionButtonsController,
                             launcher.features.lobby_features.managers.game_launching.GameLaunchErrorHandler errorHandler) {
        this.launchService = launchService;
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
     *   <li>Delegates parsing to business service</li>
     *   <li>Coordinates launch flow with business service</li>
     *   <li>Reports results to UI</li>
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
        
        // Parse JSON using business service
        Map<String, Object> jsonConfigurationData = launchService.parseJsonConfiguration(jsonText);
        if (jsonConfigurationData == null && !jsonText.isEmpty()) {
            // Invalid JSON syntax - report error to UI
            errorHandler.handleValidationError(
                "Invalid JSON syntax - Please enter valid JSON configuration.", false);
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
            // Parse the saved JSON using business service (silently fail if invalid, use defaults)
            Map<String, Object> jsonConfigurationData = launchService.parseJsonConfiguration(savedJson);
            if (jsonConfigurationData == null && savedJson != null && !savedJson.trim().isEmpty()) {
                Logging.error("Auto-launch: Failed to parse saved JSON");
            }
            
            // Launch the game with the saved configuration (auto-launch mode)
            return launchGame(gameModule, jsonConfigurationData, true);
        } catch (Exception e) {
            Logging.error("Auto-launch: Error launching game with saved JSON: " + e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Core launch method that coordinates UI and business logic.
     * 
     * <p>This method:
     * <ol>
     *   <li>Validates ViewModel availability (reports to UI if not)</li>
     *   <li>Validates game launch prerequisites (delegates to business service)</li>
     *   <li>Applies JSON configuration (delegates to business service)</li>
     *   <li>Executes the launch (delegates to business service)</li>
     *   <li>Reports results to UI</li>
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
        // Validate ViewModel is available (report to UI if not)
        if (!launchService.isViewModelAvailable()) {
            errorHandler.handleViewModelUnavailable(isAutoLaunch);
            return false;
        }
        
        // Validate game launch using business service
        GDKViewModel.LaunchValidationResult validation = 
            launchService.validateGameLaunch(selectedGameModule);
        if (!validation.isValid()) {
            errorHandler.handleValidationError(validation.errorMessage(), isAutoLaunch);
            return false;
        }
        
        String gameName = selectedGameModule.getMetadata().getGameName();
        Logging.info("Preparing to launch game: " + gameName);
        
        // Apply JSON configuration using business service
        boolean configSuccess = launchService.applyGameConfiguration(
            selectedGameModule, jsonConfigurationData, isAutoLaunch);
        if (!configSuccess) {
            errorHandler.handleConfigurationFailure(gameName, isAutoLaunch);
            return false;
        }
        
        // Execute the launch using business service
        try {
            String jsonText = getJsonTextFromUI();
            launchService.executeLaunch(selectedGameModule, jsonText);
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
}

