package launcher.lifecycle.start.startup_window.create;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import launcher.lifecycle.start.startup_window.styling.StartupWindowTheme;

/**
 * Assembles all components into the main panel with proper spacing.
 * 
 * @author Clement Luo
 * @date December 23, 2025
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
        
        mainPanel.add(Box.createVerticalStrut(StartupWindowTheme.SPACING_MEDIUM));
        mainPanel.add(CenteredComponentCreator.create(titleLabel));
        mainPanel.add(Box.createVerticalStrut(StartupWindowTheme.SPACING_SMALL));
        mainPanel.add(CenteredComponentCreator.create(subtitleLabel));
        mainPanel.add(Box.createVerticalStrut(StartupWindowTheme.SPACING_LARGE));
        mainPanel.add(CenteredComponentCreator.create(progressBar));
        mainPanel.add(Box.createVerticalStrut(StartupWindowTheme.SPACING_SMALL));
        mainPanel.add(CenteredComponentCreator.create(percentageLabel));
        mainPanel.add(Box.createVerticalStrut(StartupWindowTheme.SPACING_MEDIUM));
        mainPanel.add(CenteredComponentCreator.create(statusLabel));
        mainPanel.add(Box.createVerticalStrut(StartupWindowTheme.SPACING_MEDIUM));
    }
}

