package launcher.features.game_launching;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.ui_areas.lobby.GDKViewModel;
import launcher.features.game_launching.helpers.SendStartMessage;
import launcher.features.json_processing.JsonParser;
import launcher.features.json_processing.MessageFunctionCheck;

import java.util.Map;

/**
 * Launches games.
 * 
 * @author Clement Luo
 * @date December 30, 2025
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public class LaunchGame {
    
    /**
     * Launches a game with the provided ViewModel, game module, and start message.
     * 
     * @param viewModel The ViewModel to use for launching
     * @param gameModule The game module to launch
     * @param startMessage The JSON start message string
     * @param isAutoLaunch Whether this is an auto-launch (affects logging verbosity)
     * @throws IllegalStateException If validation fails
     * @throws Exception If the launch fails
     */
    public static void launch(GDKViewModel viewModel,
                              GameModule gameModule,
                              String startMessage,
                              boolean isAutoLaunch) throws Exception {

        // Validate the prerequisites
        if (viewModel == null) {
            throw new IllegalStateException("ViewModel not available");
        }
        if (gameModule == null) {
            throw new IllegalStateException("No game module is selected");
        }
        
        // Parse and validate the start message
        if (startMessage == null || startMessage.trim().isEmpty()) {
            throw new IllegalStateException("Start message is required");
        }
        Map<String, Object> startMessageMap = JsonParser.parse(startMessage);
        if (startMessageMap == null) {
            throw new IllegalStateException("Invalid JSON in start message");
        }
        MessageFunctionCheck.checkIfMessageIsStartMessage(startMessageMap);
        
        // Send the start message to the game module
        if (!SendStartMessage.send(gameModule, startMessageMap, isAutoLaunch)) {
            throw new IllegalStateException("Failed to send start message");
        }
        
        // Launch the game via the ViewModel
        Logging.info("Calling ViewModel to launch game: " + gameModule.getMetadata().getGameName());
        viewModel.handleLaunchGame(gameModule, startMessage);
    }
}

