package launcher.gui.json_editor.building;

import org.fxmisc.richtext.CodeArea;
import launcher.utils.gui.JsonFormatter;

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
        String formattedJson = JsonFormatter.format(codeArea.getText().trim());
        if (formattedJson != null) {
            codeArea.replaceText(formattedJson);
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

