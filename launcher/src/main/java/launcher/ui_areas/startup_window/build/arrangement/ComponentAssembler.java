package launcher.ui_areas.startup_window.component_construction.arrangement;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import launcher.ui_areas.startup_window.component_construction.components.CenteredComponentCreator;
import launcher.ui_areas.startup_window.styling.theme.Spacing;

/**
 * Assembles all components into the main panel with proper spacing.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited January 2025
 * @since Beta 1.0
 */
public class ComponentAssembler {
    
    /**
     * Adds all components to the main panel with proper spacing.
     * 
     * @param mainPanel The main panel to add components to
     * @param spinner The loading spinner
     * @param loadingLabel The loading label
     */
    public static void assemble(
            JPanel mainPanel,
            JComponent spinner,
            JLabel loadingLabel) {
        
        // Add components to the main panel with minimal spacing
        // Using 8px grid system for consistent vertical rhythm
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_SMALL)); // Top margin
        mainPanel.add(CenteredComponentCreator.create(spinner));
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_XS)); // Spinner to label
        mainPanel.add(CenteredComponentCreator.create(loadingLabel));
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_SMALL)); // Bottom margin
    }
}

