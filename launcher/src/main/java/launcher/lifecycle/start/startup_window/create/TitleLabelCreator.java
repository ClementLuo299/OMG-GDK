package launcher.lifecycle.start.startup_window.create;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import launcher.lifecycle.start.startup_window.styling.StartupWindowTheme;

/**
 * Creates a styled title label.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @since Beta 1.0
 */
public class TitleLabelCreator {
    
    /**
     * Creates a styled title label.
     * 
     * @return A configured title label
     */
    public static JLabel create() {
        JLabel label = new JLabel(StartupWindowTheme.TITLE_TEXT);
        label.setFont(StartupWindowTheme.TITLE_FONT);
        label.setForeground(StartupWindowTheme.PRIMARY_TEXT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}

