package launcher.utils.gui;

/**
 * Utility class for formatting JSON content.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 28, 2025
 * @since Beta 1.0
 */
public class JsonFormatter {
    
    /**
     * Format JSON text with proper indentation.
     * 
     * @param jsonText The JSON text to format
     * @return Formatted JSON text, or null if formatting fails
     */
    public static String format(String jsonText) {
        if (jsonText == null || jsonText.trim().isEmpty()) {
            return null;
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = 
                new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(jsonText);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (Exception e) {
            DialogUtil.showError("Error", "Invalid JSON", 
                "The content is not valid JSON: " + e.getMessage());
            return null;
        }
    }
}

