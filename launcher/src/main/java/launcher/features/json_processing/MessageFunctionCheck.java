package launcher.features.json_processing;

import java.util.Map;

/**
 * Utility class for checking message functions.
 * 
 * <p>This class checks message function fields for validation purposes.
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited January 6, 2026
 * @since 1.0
 */
public final class MessageFunctionCheck {
    
    private MessageFunctionCheck() {
        throw new AssertionError("MessageFunctionCheck should not be instantiated");
    }
    
    /**
     * Checks if a message map is a valid start message.
     * A valid start message must have function="start" or function="ui_initialization".
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
        if (!"start".equals(func) && !"ui_initialization".equals(func)) {
            throw new IllegalStateException("Start message must have function='start' or function='ui_initialization', got: " + func);
        }
    }
    
    /**
     * Checks if a message is an "end" message.
     * 
     * @param message The message to check
     * @return true if the message is an end message, false otherwise
     */
    public static boolean isEndMessage(Map<String, Object> message) {
        if (message == null) return false;
        Object fn = message.get("function");
        return fn != null && "end".equals(String.valueOf(fn));
    }
}

