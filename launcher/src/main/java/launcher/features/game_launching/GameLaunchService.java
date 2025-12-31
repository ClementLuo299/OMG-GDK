package launcher.features.game_launching;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.lobby_features.business.GDKViewModel;

import java.util.Map;

/**
 * Business logic service for game launching operations.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>Validating launch prerequisites</li>
 *   <li>Parsing JSON configuration data</li>
 *   <li>Applying configuration to game modules</li>
 *   <li>Executing game launches via ViewModel</li>
 * </ul>
 * 
 * <p>This service does NOT handle UI updates or error reporting to users.
 * Those responsibilities belong to UI logic classes.
 * 
 * @author Clement Luo
 * @date December 30, 2025
 * @since Beta 1.0
 */
public class GameLaunchService {
    
    // ==================== DEPENDENCIES ====================
    
    /** Application ViewModel for business logic operations. */
    private final GDKViewModel applicationViewModel;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new GameLaunchService.
     * 
     * @param applicationViewModel The application ViewModel for launching games
     */
    public GameLaunchService(GDKViewModel applicationViewModel) {
        this.applicationViewModel = applicationViewModel;
    }
    
    // ==================== PUBLIC METHODS ====================
    
    // ==================== PUBLIC METHODS - VALIDATION ====================
    
    /**
     * Validates that the ViewModel is available.
     * 
     * <p>This method checks if the ViewModel dependency has been set.
     * 
     * @return true if ViewModel is available, false otherwise
     */
    public boolean isViewModelAvailable() {
        return applicationViewModel != null;
    }
    
    /**
     * Validates that a game launch is allowed.
     * 
     * <p>This method checks if the game module is valid and ready to launch.
     * 
     * @param selectedGameModule The game module to validate
     * @return Validation result containing whether launch is valid and error message if not
     */
    public GDKViewModel.LaunchValidationResult validateGameLaunch(GameModule selectedGameModule) {
        if (applicationViewModel == null) {
            return new GDKViewModel.LaunchValidationResult(false, "ViewModel not available");
        }
        return applicationViewModel.validateGameLaunch(selectedGameModule);
    }
    
    // ==================== PUBLIC METHODS - CONFIGURATION ====================
    
    /**
     * Parses JSON configuration text into a map.
     * 
     * <p>This method delegates to the ViewModel to parse the JSON configuration.
     * Returns null if the ViewModel is unavailable or the text is empty.
     * 
     * @param jsonText The JSON text to parse (may be null or empty)
     * @return Parsed configuration map, or null if parsing failed or text was empty
     */
    public Map<String, Object> parseJsonConfiguration(String jsonText) {
        if (applicationViewModel == null || jsonText == null || jsonText.trim().isEmpty()) {
            return null;
        }
        return applicationViewModel.parseJsonConfiguration(jsonText);
    }
    
    /**
     * Applies JSON configuration to a game module.
     * 
     * <p>This method uses GameLaunchUtil to apply the configuration to the game module.
     * The configuration is applied before the game is launched.
     * 
     * @param selectedGameModule The game module to configure
     * @param jsonConfigurationData The parsed JSON configuration data
     * @param isAutoLaunch Whether this is an auto-launch operation
     * @return true if configuration was successful, false otherwise
     */
    public boolean applyGameConfiguration(GameModule selectedGameModule,
                                        Map<String, Object> jsonConfigurationData,
                                        boolean isAutoLaunch) {
        return GameLaunchUtil.launchGameWithConfiguration(
            selectedGameModule, jsonConfigurationData, isAutoLaunch);
    }
    
    // ==================== PUBLIC METHODS - LAUNCHING ====================
    
    /**
     * Executes the actual game launch via ViewModel.
     * 
     * <p>This method delegates to the ViewModel to handle the game launch.
     * The ViewModel will create the game scene, set up the server simulator,
     * and manage the game lifecycle.
     * 
     * @param selectedGameModule The game module to launch
     * @param jsonText The JSON text for the ViewModel to check game mode
     * @throws IllegalStateException If the ViewModel is not available
     * @throws Exception If the launch fails
     */
    public void executeLaunch(GameModule selectedGameModule, String jsonText) throws Exception {
        if (applicationViewModel == null) {
            throw new IllegalStateException("ViewModel not available");
        }
        
        String gameName = selectedGameModule.getMetadata().getGameName();
        Logging.info("Calling ViewModel to launch game: " + gameName);
        applicationViewModel.handleLaunchGame(selectedGameModule, jsonText);
    }
}

