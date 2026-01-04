package launcher.features.json_processing;

import gdk.internal.Logging;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Utility class for JSON operations.
 * 
 * <p>This class has a single responsibility: providing centralized JSON parsing,
 * module_source_validation, and formatting functionality using Jackson ObjectMapper.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Validating JSON string syntax</li>
 *   <li>Parsing JSON strings into Map structures</li>
 *   <li>Providing access to the shared ObjectMapper instance</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date December 20, 2025
 * @edited December 20, 2025
 * @since Beta 1.0
 */
public final class JsonUtil {
    
    // ==================== CONSTANTS ====================
    
    /**
     * Shared ObjectMapper instance for JSON operations.
     * 
     * <p>ObjectMapper is steps-safe after configuration, so we can reuse it
     * across all JSON operations for better performance.
     */
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private JsonUtil() {
        throw new AssertionError("JsonUtil should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - VALIDATION ====================
    
    /**
     * Validates that a JSON string has valid syntax.
     * 
     * <p>This method attempts to parse the JSON string to verify its syntax.
     * Returns false if the string is null, empty, or contains invalid JSON.
     * 
     * @param jsonString The JSON string to validate
     * @return true if the JSON is valid, false otherwise
     */
    public static boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }
        
        try {
            jsonMapper.readValue(jsonString.trim(), Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==================== PUBLIC METHODS - PARSING ====================
    
    /**
     * Parses a JSON string into a Map structure.
     * 
     * <p>This method parses the JSON string and returns a Map representation.
     * Returns null if the input is null, empty, or parsing fails.
     * 
     * @param jsonString The JSON string to parse
     * @return The parsed Map, or null if parsing fails or input is empty
     */
    public static Map<String, Object> parseJsonString(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = jsonMapper.readValue(jsonString.trim(), Map.class);
            return result;
        } catch (Exception e) {
            Logging.error("Failed to parse JSON: " + e.getMessage());
            return null;
        }
    }
    
    // ==================== PUBLIC METHODS - ACCESS ====================
    
    /**
     * Gets the shared ObjectMapper instance.
     * 
     * <p>This method provides access to the shared ObjectMapper for classes
     * that need to perform custom JSON operations beyond the standard parsing
     * and module_source_validation methods.
     * 
     * @return The shared ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return jsonMapper;
    }
}

