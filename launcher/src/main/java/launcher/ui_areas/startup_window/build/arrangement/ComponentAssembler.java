package launcher.ui_areas.startup_window.build.arrangement;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import launcher.ui_areas.startup_window.styling_theme.Spacing;

/**
 * Assembles all components into the main panel with proper spacing.
 * 
 * <p><b>Internal class - do not import.</b> This class is for internal use within
 * the startup_window package only. Use {@link launcher.ui_areas.startup_window.StartupWindow}
 * as the public API.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited January 1, 2026
 * @since Beta 1.0
 */
public class ComponentAssembler {
    
    /**
     * Adds all components to the main panel with proper spacing.
     * 
     * @param mainPanel The main panel to add components to
     * @param spinner The load_modules spinner
     * @param loadingLabel The load_modules label
     */
    public static void assemble(
            JPanel mainPanel,
            JComponent spinner,
            JLabel loadingLabel) {
        
        // Set alignment for centering (BoxLayout centers components with CENTER_ALIGNMENT)
        spinner.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        loadingLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        
        // Add components to the main panel with minimal spacing
        // Using 8px grid system for consistent vertical rhythm
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_SMALL)); // Top margin
        mainPanel.add(spinner);
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_XS)); // Spinner to label
        mainPanel.add(loadingLabel);
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_SMALL)); // Bottom margin
    }
}

