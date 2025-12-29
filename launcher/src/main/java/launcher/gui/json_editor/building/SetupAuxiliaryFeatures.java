package launcher.gui.json_editor.building;

import org.fxmisc.richtext.CodeArea;
import launcher.gui.json_editor.features.CodeColorHighlight;
import launcher.gui.json_editor.features.JsonEditorContextMenu;
import launcher.gui.json_editor.features.KeyboardShortcuts;
import launcher.gui.json_editor.util.JsonEditorFormatter;

/**
 * Sets up all editor features for a JSON editor CodeArea.
 * Orchestrates syntax highlighting, context menu, and keyboard shortcuts.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 28, 2025
 * @since Beta 1.0
 */
public class SetupAuxiliaryFeatures {
    
    /**
     * Set up all editor features for the given code area.
     * 
     * @param codeArea The code area to set up features for
     */
    public static void setup(CodeArea codeArea) {
        // Set up syntax highlighting
        CodeColorHighlight.setup(codeArea);
        
        // Create format action
        Runnable formatAction = JsonEditorFormatter.createFormatAction(codeArea);
        
        // Set up context menu with format action
        JsonEditorContextMenu.setup(codeArea, formatAction);
        
        // Set up keyboard shortcuts with format action
        KeyboardShortcuts.setup(codeArea, formatAction);
    }
}

