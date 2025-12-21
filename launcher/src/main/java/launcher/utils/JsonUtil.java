package launcher.utils;

import gdk.internal.Logging;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Utility class for JSON operations.
 * Provides centralized JSON parsing, validation, and formatting functionality.
 * 
 * @authors Clement Luo
 * @date December 20, 2025
 * @edited December 20, 2025
 * @since Beta 1.0
 */
public final class JsonUtil {
    
    /**
     * Shared ObjectMapper instance for JSON operations.
     * ObjectMapper is thread-safe after configuration, so we can reuse it.
     */
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private JsonUtil() {
        throw new AssertionError("JsonUtil should not be instantiated");
    }
    
    /**
     * Validate that a JSON string has valid syntax.
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
    
    /**
     * Parse a JSON string into a Map.
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
    
    /**
     * Get the shared ObjectMapper instance.
     * Useful for classes that need to perform custom JSON operations.
     * 
     * @return The shared ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return jsonMapper;
    }
}

