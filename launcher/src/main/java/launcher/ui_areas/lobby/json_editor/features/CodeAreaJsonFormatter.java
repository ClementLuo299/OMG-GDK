package launcher.ui_areas.lobby.json_editor.features;

import launcher.features.json_processing.JsonFormatter;
import launcher.core.ui_features.pop_up_dialogs.DialogUtil;
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
        
        String formattedJson = JsonFormatter.format(jsonText);
        if (formattedJson != null) {
            codeArea.replaceText(formattedJson);
        } else {
            // Show error dialog if formatting fails
            DialogUtil.showError("Error", "Invalid JSON", 
                "The content is not valid JSON.");
        }
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

