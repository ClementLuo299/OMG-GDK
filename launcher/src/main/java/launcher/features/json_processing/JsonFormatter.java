package launcher.features.json_processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Map;

/**
 * Utility class for formatting JSON content.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited January 6, 2026
 * @since Beta 1.0
 */
public final class JsonFormatter {
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    private JsonFormatter() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Formats a JSON response for display with pretty printing.
     * 
     * <p>This method attempts to format with pretty printing first.
     * If that fails, it falls back to compact formatting.
     * If both fail, it returns an error message string.
     * 
     * @param response The response map to format
     * @return A formatted JSON string with proper indentation, or error message if formatting fails
     */
    public static String format(Map<String, Object> response) {
        try {
            // Use pretty printing for better readability
            ObjectWriter writer = JSON_MAPPER.writerWithDefaultPrettyPrinter();
            return writer.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            // Fallback to simple formatting if pretty printing fails
            try {
                return JSON_MAPPER.writeValueAsString(response);
            } catch (JsonProcessingException ex) {
                return "Error formatting response: " + ex.getMessage();
            }
        }
    }
    
}

