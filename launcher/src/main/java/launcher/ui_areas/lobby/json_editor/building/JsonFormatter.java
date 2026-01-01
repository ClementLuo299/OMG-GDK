package launcher.ui_areas.lobby.json_editor.building;

import launcher.core.ui.pop_up_dialogs.DialogUtil;

/**
 * Utility class for formatting JSON content.
 * 
 * <p>This class has a single responsibility: formatting JSON text strings
 * with proper indentation for display in the GUI.
 * 
 * <p>This is a GUI-specific utility that shows error pop_up_dialogs when formatting fails,
 * distinguishing it from the business logic JsonFormatter in the lobby package.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 28, 2025
 * @since Beta 1.0
 */
public class JsonFormatter {
    
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
     * Formats JSON text with proper indentation.
     * 
     * <p>This method parses the JSON text and reformats it with pretty printing.
     * If the input is not valid JSON, an error dialog is shown to the user.
     * 
     * @param jsonText The JSON text to format (may be null or empty)
     * @return Formatted JSON text with proper indentation, or null if formatting fails
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

