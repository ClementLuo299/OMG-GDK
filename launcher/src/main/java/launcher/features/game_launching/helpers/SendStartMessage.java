package launcher.features.game_launching.helpers;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.transcript_recording.recording.RecordInboundMessage;
import launcher.features.transcript_recording.recording.RecordOutboundMessage;

import java.util.Map;

/**
 * Helper class for sending start messages to game modules.
 *
 * @author Clement Luo
 * @date January 4, 2026
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public final class SendStartMessage {
    
    private SendStartMessage() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Sends a start message to a game module.
     * 
     * @param gameModule The game module to send the message to
     * @param startMessage The start message to send
     * @param isAutoLaunch Whether this is an auto-launch (affects logging verbosity)
     * @return true if the start message was sent successfully, false otherwise
     */
    public static boolean send(GameModule gameModule,
                               Map<String, Object> startMessage,
                               boolean isAutoLaunch) {

        // Send the start message and record the response
        try {

            // Record the start message to the transcript
            RecordOutboundMessage.record(startMessage);
            
            // Send the start message to the game module
            Map<String, Object> response = gameModule.handleMessage(startMessage);
            
            // Record the response to the transcript 
            if (response != null && !isAutoLaunch) {
                Logging.info("Start message acknowledged by " + gameModule.getMetadata().getGameName());
                RecordInboundMessage.record(response);
            }
            return true;
        } catch (Exception e) {
            if (!isAutoLaunch) {
                Logging.error("Failed to send start message: " + e.getMessage());
            }
            return false;
        }
    }
}

