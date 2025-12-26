package launcher.lifecycle.start.startup_window.component_construction.components;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import launcher.lifecycle.start.startup_window.styling.ModernLabel;
import launcher.lifecycle.start.startup_window.styling.StartupWindowTheme;

/**
 * Creates styled labels for the startup window with modern typography.
 * Consolidates all label creation logic into a single class.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 24, 2025
 * @since Beta 1.0
 */
public class LabelCreator {
    
    /**
     * Enum representing different label types with their styling.
     * Each type has its own text, font, color, and letter spacing from the theme.
     */
    public enum LabelType {
        // Title: larger, bold, with slight letter spacing
        TITLE(StartupWindowTheme.TITLE_TEXT, StartupWindowTheme.TITLE_FONT, StartupWindowTheme.PRIMARY_TEXT, 0.02f),
        // Subtitle: lighter weight, subtle letter spacing
        SUBTITLE(StartupWindowTheme.INITIAL_SUBTITLE, StartupWindowTheme.SUBTITLE_FONT, StartupWindowTheme.SECONDARY_TEXT, 0.01f),
        // Percentage: medium weight, no letter spacing
        PERCENTAGE(StartupWindowTheme.INITIAL_PERCENTAGE, StartupWindowTheme.PERCENTAGE_FONT, StartupWindowTheme.SECONDARY_TEXT, 0.0f),
        // Status: bold, slight letter spacing
        STATUS(StartupWindowTheme.INITIAL_STATUS, StartupWindowTheme.STATUS_FONT, StartupWindowTheme.PRIMARY_TEXT, 0.01f);
        
        private final String text;
        private final java.awt.Font font;
        private final java.awt.Color foreground;
        private final float letterSpacing;
        
        // Each label type stores its styling configuration
        LabelType(String text, java.awt.Font font, java.awt.Color foreground, float letterSpacing) {
            this.text = text;
            this.font = font;
            this.foreground = foreground;
            this.letterSpacing = letterSpacing;
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
        
        public float getLetterSpacing() {
            return letterSpacing;
        }
    }
    
    /**
     * Creates a styled label of the specified type with modern typography.
     * 
     * @param type The type of label to create
     * @return A configured label with letter spacing support
     */
    public static JLabel create(LabelType type) {
        // Use ModernLabel for letter spacing support
        ModernLabel label = new ModernLabel(type.getText());
        label.setFont(type.getFont());
        label.setForeground(type.getForeground());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setLetterSpacing(type.getLetterSpacing());
        return label;
    }
}

