package launcher.lifecycle.start.startup_window.initialization.labels;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import launcher.lifecycle.start.startup_window.styling.StartupWindowTheme;

/**
 * Creates a styled status label.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @since Beta 1.0
 */
public class StatusLabelCreator {
    
    /**
     * Creates a styled status label.
     * 
     * @return A configured status label
     */
    public static JLabel create() {
        JLabel label = new JLabel(StartupWindowTheme.INITIAL_STATUS);
        label.setFont(StartupWindowTheme.STATUS_FONT);
        label.setForeground(StartupWindowTheme.PRIMARY_TEXT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}

