package launcher.lifecycle.start.startup_window.initialization.components;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import launcher.lifecycle.start.startup_window.styling.StartupWindowTheme;

/**
 * Creates styled labels for the startup window.
 * Consolidates all label creation logic into a single class.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 23, 2025
 * @since Beta 1.0
 */
public class LabelCreator {
    
    /**
     * Enum representing different label types with their styling.
     * Each type has its own text, font, and color from the theme.
     */
    public enum LabelType {
        TITLE(StartupWindowTheme.TITLE_TEXT, StartupWindowTheme.TITLE_FONT, StartupWindowTheme.PRIMARY_TEXT),
        SUBTITLE(StartupWindowTheme.INITIAL_SUBTITLE, StartupWindowTheme.SUBTITLE_FONT, StartupWindowTheme.SECONDARY_TEXT),
        PERCENTAGE(StartupWindowTheme.INITIAL_PERCENTAGE, StartupWindowTheme.PERCENTAGE_FONT, StartupWindowTheme.SECONDARY_TEXT),
        STATUS(StartupWindowTheme.INITIAL_STATUS, StartupWindowTheme.STATUS_FONT, StartupWindowTheme.PRIMARY_TEXT);
        
        private final String text;
        private final java.awt.Font font;
        private final java.awt.Color foreground;
        
        // Each label type stores its styling configuration
        LabelType(String text, java.awt.Font font, java.awt.Color foreground) {
            this.text = text;
            this.font = font;
            this.foreground = foreground;
        }
        
        public String getText() {
            return text;
        }
        
        public java.awt.Font getFont() {
            return font;
        }
        
        public java.awt.Color getForeground() {
            return foreground;
        }
    }
    
    /**
     * Creates a styled label of the specified type.
     * 
     * @param type The type of label to create
     * @return A configured label
     */
    public static JLabel create(LabelType type) {
        // Create label with text, font, and color from the specified type
        JLabel label = new JLabel(type.getText());
        label.setFont(type.getFont());
        label.setForeground(type.getForeground());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}

