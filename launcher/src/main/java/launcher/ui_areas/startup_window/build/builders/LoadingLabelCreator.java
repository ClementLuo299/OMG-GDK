package launcher.ui_areas.startup_window.build.builders;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import launcher.ui_areas.startup_window.styling_theme.Colors;
import launcher.ui_areas.startup_window.styling_theme.Labels;
import launcher.ui_areas.startup_window.styling_theme.Font;

/**
 * Creates the loading label for the startup window.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class LoadingLabelCreator {
    
    /**
     * Creates the loading label with modern typography.
     * 
     * @return A configured label with "Loading" text
     */
    public static JLabel create() {
        JLabel label = new JLabel(Labels.LOADING_TEXT);
        label.setFont(Font.STATUS_FONT);
        label.setForeground(Colors.PRIMARY_TEXT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}

