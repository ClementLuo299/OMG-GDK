package launcher.ui_areas.startup_window.build.builders;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import launcher.ui_areas.startup_window.styling_theme.Colors;
import launcher.ui_areas.startup_window.styling_theme.Padding;

/**
 * Creates the main panel with proper layout and modern styling_theme.
 * Uses rounded corners and shadow effects for a modern look.
 * 
 * <p><b>Internal class - do not import.</b> This class is for internal use within
 * the startup_window package only. Use {@link launcher.ui_areas.startup_window.StartupWindow}
 * as the public API.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 24, 2025
 * @since Beta 1.0
 */
public class MainPanelCreator {
    
    /**
     * Creates the main panel with proper layout and modern styling_theme.
     * 
     * @return A configured main panel with rounded corners and shadow
     */
    public static JPanel create() {
        // Create a simple panel with background color
        JPanel panel = new JPanel();
        panel.setBackground(Colors.BACKGROUND);
        panel.setOpaque(true);
        
        // Use vertical box layout to stack components top to bottom
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Add padding using border
        panel.setBorder(new EmptyBorder(
            Padding.PANEL_PADDING,
            Padding.PANEL_PADDING,
            Padding.PANEL_PADDING,
            Padding.PANEL_PADDING
        ));
        
        return panel;
    }
}

