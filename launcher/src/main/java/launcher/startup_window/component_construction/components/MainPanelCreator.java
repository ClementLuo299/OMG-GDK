package launcher.startup_window.component_construction.components;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import launcher.startup_window.styling.components.RoundedPanel;
import launcher.startup_window.styling.theme.Dimensions;

/**
 * Creates the main panel with proper layout and modern styling.
 * Uses rounded corners and shadow effects for a modern look.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 24, 2025
 * @since Beta 1.0
 */
public class MainPanelCreator {
    
    /**
     * Creates the main panel with proper layout and modern styling.
     * 
     * @return A configured main panel with rounded corners and shadow
     */
    public static JPanel create() {
        // Use custom rounded panel with shadow
        RoundedPanel panel = new RoundedPanel(
            Dimensions.CORNER_RADIUS,
            true // Enable shadow
        );
        
        // Use vertical box layout to stack components top to bottom
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Add padding using border (shadow is drawn by the panel itself)
        panel.setBorder(new EmptyBorder(
            Dimensions.PANEL_PADDING,
            Dimensions.PANEL_PADDING,
            Dimensions.PANEL_PADDING,
            Dimensions.PANEL_PADDING
        ));
        
        return panel;
    }
}

