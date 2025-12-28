package launcher.gui.json_editor.features;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.TwoDimensional.Bias;

/**
 * Handles auto-indentation for JSON editor CodeArea components.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @since 1.0
 */
public class JsonEditorAutoIndent {
    
    /**
     * Set up auto-indentation for the given code area.
     * 
     * @param codeArea The code area to apply auto-indentation to
     */
    public static void setup(CodeArea codeArea) {
        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                int caretPosition = codeArea.getCaretPosition();
                int paragraph = codeArea.offsetToPosition(caretPosition, Bias.Backward).getMajor();
                String currentLine = codeArea.getText(paragraph, paragraph + 1);
                
                // Get indentation
                StringBuilder indent = new StringBuilder();
                for (char c : currentLine.toCharArray()) {
                    if (c == ' ' || c == '\t') {
                        indent.append(c);
                    } else {
                        break;
                    }
                }
                
                // Add extra indent if line ends with {
                if (currentLine.trim().endsWith("{")) {
                    indent.append("    ");
                }
                
                codeArea.insertText(codeArea.getCaretPosition(), "\n" + indent);
                event.consume();
            }
        });
    }
}

