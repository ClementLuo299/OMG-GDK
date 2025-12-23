package launcher.lifecycle.start.startup_window.create;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import launcher.lifecycle.start.startup_window.styling.StartupWindowTheme;

/**
 * Creates the main panel with proper layout and styling.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @since Beta 1.0
 */
public class MainPanelCreator {
    
    /**
     * Creates the main panel with proper layout and styling.
     * 
     * @return A configured main panel
     */
    public static JPanel create() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                StartupWindowTheme.BORDER,
                StartupWindowTheme.BORDER_WIDTH
            ),
            new EmptyBorder(
                StartupWindowTheme.PANEL_PADDING,
                StartupWindowTheme.PANEL_PADDING,
                StartupWindowTheme.PANEL_PADDING,
                StartupWindowTheme.PANEL_PADDING
            )
        ));
        panel.setBackground(StartupWindowTheme.BACKGROUND);
        return panel;
    }
}

