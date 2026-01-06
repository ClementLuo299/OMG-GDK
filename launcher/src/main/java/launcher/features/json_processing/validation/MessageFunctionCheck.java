package launcher.features.game_messaging.validation;

import java.util.Map;

/**
 * Utility class for checking if a message is a start message.
 * 
 * <p>This class checks that a message has the required function field
 * (function="start" or function="init").
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited January 5, 2026
 * @since 1.0
 */
public final class MessageFunctionCheck {
    
    private MessageFunctionCheck() {
        throw new AssertionError("StartMessageUtil should not be instantiated");
    }
    
    /**
     * Checks if a message map is a valid start message.
     * A valid start message must have function="start" or function="init".
     * 
     * @param message The message map to check
     * @throws IllegalStateException If the message is not a valid start message
     */
    public static void checkIfMessageIsStartMessage(Map<String, Object> message) {
        if (message == null) {
            throw new IllegalStateException("Start message is required");
        }
        
        Object function = message.get("function");
        if (!(function instanceof String)) {
            throw new IllegalStateException("Start message must have a 'function' field");
        }
        
        String func = (String) function;
        if (!"start".equals(func) && !"init".equals(func)) {
            throw new IllegalStateException("Start message must have function='start' or function='init', got: " + func);
        }
    }
}

