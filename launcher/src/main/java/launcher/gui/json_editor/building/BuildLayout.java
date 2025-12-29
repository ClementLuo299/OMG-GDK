package launcher.gui.json_editor.building;

import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;

/**
 * Builds the layout structure for JSON editor components.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 28, 2025
 * @since Beta 1.0
 */
public class BuildLayout {
    
    /**
     * Build the complete layout for a JSON editor.
     * 
     * @param container The VBox container to build the layout in
     * @param title The title for the editor
     * @param codeArea The code area component to include in the layout
     */
    public static void build(VBox container, String title, CodeArea codeArea) {
        // Container styling
        container.setSpacing(0);
        container.getStyleClass().add("single-json-editor");
        
        // Title label
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-label");
        
        // Add components to container
        container.getChildren().addAll(titleLabel, codeArea);
        
        // Make code area grow vertically
        VBox.setVgrow(codeArea, Priority.ALWAYS);
    }
}

