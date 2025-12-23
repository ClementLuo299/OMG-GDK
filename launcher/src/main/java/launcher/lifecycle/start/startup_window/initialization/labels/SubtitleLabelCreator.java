package launcher.lifecycle.start.startup_window.initialization.labels;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import launcher.lifecycle.start.startup_window.styling.StartupWindowTheme;

/**
 * Creates a styled subtitle label.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @since Beta 1.0
 */
public class SubtitleLabelCreator {
    
    /**
     * Creates a styled subtitle label.
     * 
     * @return A configured subtitle label
     */
    public static JLabel create() {
        JLabel label = new JLabel(StartupWindowTheme.INITIAL_SUBTITLE);
        label.setFont(StartupWindowTheme.SUBTITLE_FONT);
        label.setForeground(StartupWindowTheme.SECONDARY_TEXT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}

