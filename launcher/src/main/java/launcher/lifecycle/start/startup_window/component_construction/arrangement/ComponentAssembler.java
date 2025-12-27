package launcher.lifecycle.start.startup_window.component_construction.arrangement;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import launcher.lifecycle.start.startup_window.component_construction.components.CenteredComponentCreator;
import launcher.lifecycle.start.startup_window.styling.theme.Spacing;

/**
 * Assembles all components into the main panel with proper spacing.
 * 
 * @author Clement Luo
 * @date December 23, 2025
 * @edited December 23, 2025
 * @since Beta 1.0
 */
public class ComponentAssembler {
    
    /**
     * Adds all components to the main panel with proper spacing.
     * 
     * @param mainPanel The main panel to add components to
     * @param titleLabel The title label
     * @param subtitleLabel The subtitle label
     * @param progressBar The progress bar
     * @param percentageLabel The percentage label
     * @param statusLabel The status label
     */
    public static void assemble(
            JPanel mainPanel,
            JLabel titleLabel,
            JLabel subtitleLabel,
            JProgressBar progressBar,
            JLabel percentageLabel,
            JLabel statusLabel) {
        
        // Add components to the main panel with improved modern spacing
        // Using 8px grid system for consistent vertical rhythm
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_XS)); // Small top margin
        mainPanel.add(CenteredComponentCreator.create(titleLabel));
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_SMALL)); // Title to subtitle
        mainPanel.add(CenteredComponentCreator.create(subtitleLabel));
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_LARGE)); // Subtitle to progress bar
        mainPanel.add(CenteredComponentCreator.create(progressBar));
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_SMALL)); // Progress bar to percentage
        mainPanel.add(CenteredComponentCreator.create(percentageLabel));
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_MEDIUM)); // Percentage to status
        mainPanel.add(CenteredComponentCreator.create(statusLabel));
        mainPanel.add(Box.createVerticalStrut(Spacing.SPACING_XS)); // Small bottom margin
    }
}

