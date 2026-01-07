package launcher.features.json_processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdk.internal.Logging;

import java.util.Map;

/**
 * Utility class for parsing JSON strings.
 * 
 * @author Clement Luo
 * @date January 4, 2026
 * @edited January 6, 2026
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
    
}

