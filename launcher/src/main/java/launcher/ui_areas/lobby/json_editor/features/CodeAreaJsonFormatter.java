package launcher.ui_areas.lobby.json_editor.features;

import launcher.features.json_processing.JsonFormatter;
import launcher.features.json_processing.JsonParser;
import launcher.ui_areas.shared.dialogs.DialogUtil;
import org.fxmisc.richtext.CodeArea;

/**
 * Handles JSON formatting for CodeArea components.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 28, 2025
 * @since Beta 1.0
 */
public class CodeAreaJsonFormatter {
    
    /**
     * Format the JSON content in the given code area.
     * 
     * @param codeArea The code area containing JSON to format
     */
    public static void format(CodeArea codeArea) {
        String jsonText = codeArea.getText().trim();
        if (jsonText == null || jsonText.isEmpty()) {
            return;
        }
        
        // Parse the JSON string first, then format it
        java.util.Map<String, Object> parsed = JsonParser.parse(jsonText);
        if (parsed == null) {
            // Show error dialog if parsing fails
            DialogUtil.showError("Error", "Invalid JSON", 
                "The content is not valid JSON.");
            return;
        }
        
        String formattedJson = JsonFormatter.format(parsed);
        codeArea.replaceText(formattedJson);
    }
    
    /**
     * Create a Runnable that formats the given code area when executed.
     * 
     * @param codeArea The code area to format
     * @return A Runnable that formats the code area
     */
    public static Runnable createFormatAction(CodeArea codeArea) {
        return () -> format(codeArea);
    }
}

