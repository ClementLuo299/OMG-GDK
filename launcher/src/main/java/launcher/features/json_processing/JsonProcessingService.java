package launcher.features.json_processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdk.internal.Logging;

import java.util.Map;

/**
 * Business logic service for JSON processing operations.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>Parsing JSON configuration data</li>
 *   <li>Formatting JSON responses</li>
 * </ul>
 * 
 * <p>This service does NOT handle UI updates.
 * Those responsibilities belong to UI logic classes.
 * 
 * @author Clement Luo
 * @date December 30, 2025
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class JsonProcessingService {
    
    // ==================== CONSTANTS ====================
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new JsonProcessingService.
     */
    public JsonProcessingService() {
        // No dependencies needed - service is now self-contained
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Parses JSON configuration text into a map.
     * 
     * <p>This method parses a JSON string into a Map structure for use in game configuration.
     * Returns null if the input is empty or parsing fails.
     * 
     * @param jsonText The JSON text to parse
     * @return Parsed configuration map, or null if parsing fails or text is empty/null
     */
    public Map<String, Object> parseJsonConfiguration(String jsonText) {
        if (jsonText == null || jsonText.trim().isEmpty()) {
            return null;
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> configurationData = JSON_MAPPER.readValue(jsonText.trim(), Map.class);
            return configurationData;
        } catch (Exception e) {
            Logging.error("❌ Failed to parse JSON: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Formats a JSON response for display.
     * 
     * <p>This method uses JsonFormatter to format the response with pretty printing
     * for better readability in the UI.
     * 
     * @param response The response map to format
     * @return Formatted JSON string with proper indentation
     */
    public String formatJsonResponse(Map<String, Object> response) {
        return JsonFormatter.formatJsonResponse(response);
    }
}

