package launcher.lifecycle.start.startup_window.initialization.components;

import java.awt.FlowLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import launcher.lifecycle.start.startup_window.styling.StartupWindowTheme;

/**
 * Creates a centered wrapper panel for a component.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 23, 2025
 * @since Beta 1.0
 */
public class CenteredComponentCreator {
    
    /**
     * Creates a centered wrapper panel for a component.
     * 
     * @param component The component to center
     * @return A panel containing the centered component
     */
    public static JPanel create(JComponent component) {
        // Create a wrapper panel with centered flow layout
        // This ensures components are centered horizontally within the main panel's vertical layout
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(StartupWindowTheme.BACKGROUND);
        panel.add(component);
        return panel;
    }
}

