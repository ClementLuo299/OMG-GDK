package launcher.gui.json_editor;

import javafx.scene.layout.VBox;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import org.fxmisc.richtext.CodeArea;
import launcher.gui.json_editor.config.JsonEditorCodeAreaConfigurator;
import launcher.gui.json_editor.config.JsonEditorFeaturesSetup;
import launcher.gui.json_editor.config.JsonEditorLayoutBuilder;

/**
 * A professional JSON editor using RichTextFX with syntax highlighting,
 * line numbers, and comprehensive editing features.
 * 
 * @author: Clement Luo
 * @date: August 5, 2025
 * @edited: December 27, 2025
 * @since: Beta 1.0
 */
public class JsonEditor extends VBox {
    
    private final CodeArea codeArea;
    private final StringProperty textProperty;
    private final String title;

    // ==================== CONSTRUCTORS ====================
    
    /**
     * Creates a new JSON editor with the specified title.
     * 
     * @param title The title for this JSON editor
     */
    public JsonEditor(String title) {
        this.title = title;
        this.textProperty = new SimpleStringProperty("");
        
        // Create and configure the code area
        this.codeArea = new CodeArea();
        JsonEditorCodeAreaConfigurator.configure(codeArea, textProperty);
        
        // Set up all editor features
        JsonEditorFeaturesSetup.setup(codeArea);
        
        // Build the layout
        JsonEditorLayoutBuilder.build(this, this.title, codeArea);
    }

    /**
     * Creates a new JSON editor with default title.
     */
    public JsonEditor() {
        this("JSON Editor");
    }

    // ==================== PUBLIC API ====================
    
    /**
     * Get the text property for binding.
     */
    public StringProperty textProperty() {
        return textProperty;
    }

    /**
     * Get the current text content.
     */
    public String getText() {
        return codeArea.getText();
    }

    /**
     * Set the text content.
     */
    public void setText(String text) {
        codeArea.replaceText(text);
    }

    /**
     * Clear all content.
     */
    public void clear() {
        codeArea.clear();
    }
} 