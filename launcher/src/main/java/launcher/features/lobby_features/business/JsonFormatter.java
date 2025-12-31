package launcher.features.lobby_features.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Map;

/**
 * Handles JSON formatting operations.
 * 
 * <p>This class has a single responsibility: converting Map objects to JSON strings
 * with various formatting options (pretty-printed or compact).
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Formatting JSON responses with pretty printing for display</li>
 *   <li>Formatting JSON maps to compact strings</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class JsonFormatter {
    
    // ==================== CONSTANTS ====================
    
    /** Shared ObjectMapper instance for JSON operations. */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    // ==================== PUBLIC METHODS ====================
    
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
     * Formats a JSON map to a compact string (no pretty printing).
     * 
     * <p>This method produces a single-line JSON string without indentation.
     * 
     * @param data The data map to format
     * @return A JSON string representation
     * @throws JsonProcessingException if formatting fails
     */
    public static String formatJsonSimple(Map<String, Object> data) throws JsonProcessingException {
        return JSON_MAPPER.writeValueAsString(data);
    }
}

