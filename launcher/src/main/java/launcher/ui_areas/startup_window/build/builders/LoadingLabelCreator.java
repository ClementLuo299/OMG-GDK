package launcher.ui_areas.startup_window.build.components;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import launcher.ui_areas.startup_window.styling.components.StyledLabel;
import launcher.ui_areas.startup_window.styling.theme.Colors;
import launcher.ui_areas.startup_window.styling.theme.TextContent;
import launcher.ui_areas.startup_window.styling.theme.Typography;

/**
 * Creates the loading label for the startup window.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class LoadingLabel {
    
    /**
     * Creates the loading label with modern typography.
     * 
     * @return A configured label with "Loading" text
     */
    public static JLabel create() {
        StyledLabel label = new StyledLabel(TextContent.LOADING_TEXT);
        label.setFont(Typography.STATUS_FONT);
        label.setForeground(Colors.PRIMARY_TEXT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setLetterSpacing(0.01f);
        return label;
    }
}

