package launcher.ui_areas.lobby.json_editor.building;

import javafx.beans.property.StringProperty;
import org.fxmisc.richtext.CodeArea;
import launcher.ui_areas.lobby.json_editor.features.AutoIndent;

/**
 * Configures the core CodeArea component for JSON editing with line numbers,
 * styling, and auto-indentation.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 28, 2025
 * @since Beta 1.0
 */
public class ConfigureCodeArea {
    
    private static final int PREFERRED_WIDTH = 600;
    
    /**
     * Configure a CodeArea for JSON editing.
     * 
     * @param codeArea The code area to configure
     * @param textProperty The text property to bind to the code area
     */
    public static void configure(CodeArea codeArea, StringProperty textProperty) {
        // Line numbers
        codeArea.setParagraphGraphicFactory(org.fxmisc.richtext.LineNumberFactory.get(codeArea));
        
        // Text wrapping
        codeArea.setWrapText(true);
        
        // Preferred width (height will expand automatically)
        codeArea.setPrefWidth(PREFERRED_WIDTH);
        
        // Remove any internal margins or padding
        codeArea.setStyle("-fx-margin: 0; -fx-padding: 0;");
        
        // Apply CSS class for styling
        codeArea.getStyleClass().add("code-area");
        
        // Bind text property
        textProperty.bind(codeArea.textProperty());
        
        // Set up auto-indent
        AutoIndent.setup(codeArea);
    }
}

