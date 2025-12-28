package launcher.gui.json_editor.features;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CodeArea;

/**
 * Handles keyboard shortcuts for JSON editor CodeArea components.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @since 1.0
 */
public class JsonEditorKeyboardShortcuts {
    
    /**
     * Set up keyboard shortcuts for the given code area.
     * 
     * @param codeArea The code area to attach keyboard shortcuts to
     * @param formatAction The action to perform when Ctrl+F is pressed
     */
    public static void setup(CodeArea codeArea, Runnable formatAction) {
        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                formatAction.run();
                event.consume();
            }
        });
    }
}

