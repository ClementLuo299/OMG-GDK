package launcher.gui.lobby;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Map;

/**
 * Handles JSON formatting operations.
 * Business logic for formatting JSON data structures.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class JsonFormatter {
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    /**
     * Format a JSON response for display with pretty printing.
     * 
     * @param response The response map to format
     * @return A formatted JSON string with proper indentation, or error message if formatting fails
     */
    public static String formatJsonResponse(Map<String, Object> response) {
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
    
    /**
     * Format a JSON map to a simple string (no pretty printing).
     * 
     * @param data The data map to format
     * @return A JSON string representation
     * @throws JsonProcessingException if formatting fails
     */
    public static String formatJsonSimple(Map<String, Object> data) throws JsonProcessingException {
        return JSON_MAPPER.writeValueAsString(data);
    }
}

