package launcher.gui.json_editor.features;

import javafx.scene.control.MenuItem;
import org.fxmisc.richtext.CodeArea;

/**
 * Handles context menu setup for JSON editor CodeArea components.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 28, 2025
 * @since Beta 1.0
 */
public class JsonEditorContextMenu {
    
    /**
     * Set up context menu for the given code area.
     * 
     * @param codeArea The code area to attach the context menu to
     * @param formatAction The action to perform when format is selected
     */
    public static void setup(CodeArea codeArea, Runnable formatAction) {
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        
        MenuItem formatItem = new MenuItem("Format JSON");
        formatItem.setOnAction(e -> formatAction.run());
        
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> codeArea.copy());
        
        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setOnAction(e -> codeArea.cut());
        
        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setOnAction(e -> codeArea.paste());
        
        MenuItem selectAllItem = new MenuItem("Select All");
        selectAllItem.setOnAction(e -> codeArea.selectAll());
        
        contextMenu.getItems().addAll(formatItem, copyItem, cutItem, pasteItem, selectAllItem);
        codeArea.setContextMenu(contextMenu);
    }
}

