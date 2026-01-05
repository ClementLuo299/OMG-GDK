package launcher.ui_areas.lobby.game_launching;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.game_launching.LaunchGame;
import launcher.ui_areas.lobby.GDKViewModel;
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
 * <p>Business logic (module_source_validation, parsing, launching) is delegated to {@link LaunchGame}.
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
    
    /** ViewModel for launching games. */
    private final GDKViewModel viewModel;
    
    /** Controller for JSON input/action buttons in the UI. */
    private final JsonActionButtonsController jsonActionButtonsController;
    
    /** Error handler for reporting launch failures to UI. */
    private final GameLaunchErrorHandler errorHandler;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new GameLaunchingManager.
     * 
     * @param viewModel The ViewModel for launching games
     * @param jsonActionButtonsController The JSON action buttons controller
     * @param errorHandler The error handler for launch errors
     */
    public GameLaunchingManager(GDKViewModel viewModel,
                             JsonActionButtonsController jsonActionButtonsController,
                             GameLaunchErrorHandler errorHandler) {
        this.viewModel = viewModel;
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
        
        // Validate JSON syntax (if provided)
        if (!jsonText.isEmpty()) {
            Map<String, Object> testParse = launcher.features.json_processing.JsonParser.parse(jsonText);
            if (testParse == null) {
                errorHandler.handleValidationError(
                    "Invalid JSON syntax - Please enter valid JSON configuration.", false);
                return false;
            }
        }
        
        // Launch with UI configuration (not auto-launch)
        return launchGame(selectedGameModule, jsonText, false);
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
            // Launch the game with the saved configuration (auto-launch mode)
            return launchGame(gameModule, savedJson, true);
        } catch (Exception e) {
            Logging.error("Auto-launch: Error launching game with saved JSON: " + e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Core launch method that coordinates UI and business logic.
     * 
     * @param gameModule The game module to launch
     * @param startMessage The JSON start message string
     * @param isAutoLaunch Whether this is an auto-launch (affects error handling)
     * @return true if launch was successful, false otherwise
     */
    private boolean launchGame(GameModule gameModule, String startMessage, boolean isAutoLaunch) {
        try {
            String gameName = gameModule != null ? gameModule.getMetadata().getGameName() : "unknown";
            Logging.info("Preparing to launch game: " + gameName);
            
            LaunchGame.launch(viewModel, gameModule, startMessage, isAutoLaunch);
            errorHandler.reportSuccessfulLaunch(gameName);
            return true;
        } catch (IllegalStateException e) {
            if (viewModel == null) {
                errorHandler.handleViewModelUnavailable(isAutoLaunch);
            } else if (gameModule == null) {
                errorHandler.handleValidationError(e.getMessage(), isAutoLaunch);
            } else {
                errorHandler.handleConfigurationFailure(
                    gameModule.getMetadata().getGameName(), isAutoLaunch);
            }
            return false;
        } catch (Exception e) {
            String gameName = gameModule != null ? gameModule.getMetadata().getGameName() : "unknown";
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

