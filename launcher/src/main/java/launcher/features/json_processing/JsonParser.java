package launcher.features.json_processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdk.internal.Logging;
import launcher.features.json_processing.validation.MessageFunctionCheck;

import java.util.Map;

/**
 * Utility class for parsing JSON strings.
 * 
 * @author Clement Luo
 * @date January 4, 2026
 * @since Beta 1.0
 */
public final class JsonParser {
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    private JsonParser() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Parses a JSON string into a map.
     * Returns null if the text is empty or parsing fails.
     */
    public static Map<String, Object> parse(String jsonText) {
        if (jsonText == null || jsonText.trim().isEmpty()) {
            return null;
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = JSON_MAPPER.readValue(jsonText.trim(), Map.class);
            return data;
        } catch (Exception e) {
            Logging.error("Failed to parse JSON: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parses and validates a start message JSON string.
     * 
     * @param jsonText The JSON string to parse and validate
     * @return The parsed and validated start message map
     * @throws IllegalStateException If the string is empty, invalid JSON, or not a valid start message
     */
    public static Map<String, Object> parseAndValidateStartMessage(String jsonText) {
        if (jsonText == null || jsonText.trim().isEmpty()) {
            throw new IllegalStateException("Start message is required");
        }
        
        Map<String, Object> message = parse(jsonText);
        if (message == null) {
            throw new IllegalStateException("Invalid JSON in start message");
        }
        
        MessageFunctionCheck.checkIfMessageIsStartMessage(message);
        return message;
    }
}

