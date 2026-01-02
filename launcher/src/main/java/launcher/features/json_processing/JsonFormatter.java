package launcher.features.json_processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Map;

/**
 * Handles JSON formatting operations.
 * 
 * <p>This class has a single responsibility: formatting JSON content in various ways.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Formatting JSON responses (Map to JSON) with pretty printing for display</li>
 *   <li>Formatting JSON maps to compact strings</li>
 *   <li>Formatting JSON strings with pretty printing</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited January 2025
 * @since Beta 1.0
 */
public class JsonFormatter {
    
    // ==================== CONSTANTS ====================
    
    /** Shared ObjectMapper instance for JSON operations. */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private JsonFormatter() {
        throw new AssertionError("JsonFormatter should not be instantiated");
    }
    
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
    
    /**
     * Formats JSON text with proper indentation.
     * 
     * <p>This method parses the JSON text and reformats it with pretty printing.
     * If the input is not valid JSON, returns null.
     * 
     * @param jsonText The JSON text to format (may be null or empty)
     * @return Formatted JSON text with proper indentation, or null if formatting fails
     */
    public static String format(String jsonText) {
        if (jsonText == null || jsonText.trim().isEmpty()) {
            return null;
        }
        
        try {
            JsonNode jsonNode = JSON_MAPPER.readTree(jsonText);
            return JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (Exception e) {
            return null;
        }
    }
}

