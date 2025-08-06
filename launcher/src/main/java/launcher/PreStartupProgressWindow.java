package launcher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pre-Startup Progress Window
 * 
 * Shows a progress window BEFORE JavaFX starts, using Swing for immediate display.
 * This window appears instantly when the application launches and shows progress
 * during the JavaFX initialization phase.
 * 
 * @author Clement Luo
 * @since 1.0
 */
public class PreStartupProgressWindow {
    
    private JFrame progressFrame;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    // Progress tracking
    private AtomicInteger currentStep = new AtomicInteger(0);
    private int totalSteps = 6; // Pre-JavaFX + JavaFX steps
    
    /**
     * Initialize the pre-startup progress window
     */
    public PreStartupProgressWindow() {
        System.out.println("🚀 Creating pre-startup progress window...");
        createProgressWindow();
    }
    
    /**
     * Create and configure the progress window UI using Swing
     */
    private void createProgressWindow() {
        // Set up the main frame
        progressFrame = new JFrame("GDK Game Development Kit");
        progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        progressFrame.setResizable(false);
        progressFrame.setAlwaysOnTop(true);
        
        // Create UI components (this also sets up the layout)
        createUIComponents();
        
        // Center the window
        progressFrame.pack();
        progressFrame.setLocationRelativeTo(null);
        
        System.out.println("✅ Pre-startup progress window created");
    }
    
    /**
     * Create and configure UI components
     */
    private void createUIComponents() {
        // Title label
        JLabel titleLabel = new JLabel("GDK Game Development Kit");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Initializing...");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(189, 195, 199));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Progress bar
        progressBar = new JProgressBar(0, totalSteps);
        progressBar.setPreferredSize(new Dimension(500, 20));
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        
        // Status label
        statusLabel = new JLabel("Starting up...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(44, 62, 80));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        

        
        // Set up the layout with the created components
        setupLayout(titleLabel, subtitleLabel);
    }
    
    /**
     * Set up the main layout
     */
    private void setupLayout(JLabel titleLabel, JLabel subtitleLabel) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(new Color(248, 249, 250));
        
        // Add components with spacing
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createCenteredComponent(titleLabel));
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(createCenteredComponent(subtitleLabel));
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createCenteredComponent(progressBar));
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createCenteredComponent(statusLabel));
        mainPanel.add(Box.createVerticalStrut(10));
        
        progressFrame.setContentPane(mainPanel);
    }
    
    /**
     * Create a centered component
     */
    private JPanel createCenteredComponent(JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(248, 249, 250));
        panel.add(component);
        return panel;
    }
    
    /**
     * Show the progress window
     */
    public void show() {
        System.out.println("🎬 Showing pre-startup progress window");
        
        // Show on EDT to ensure thread safety
        if (SwingUtilities.isEventDispatchThread()) {
            progressFrame.setVisible(true);
        } else {
            SwingUtilities.invokeLater(() -> progressFrame.setVisible(true));
        }
        

    }
    
    /**
     * Hide the progress window
     */
    public void hide() {
        System.out.println("🏁 Hiding pre-startup progress window");
        
        if (SwingUtilities.isEventDispatchThread()) {
            progressFrame.setVisible(false);
            progressFrame.dispose();
        } else {
            SwingUtilities.invokeLater(() -> {
                progressFrame.setVisible(false);
                progressFrame.dispose();
            });
        }
    }
    
    /**
     * Update progress and status
     * @param step The current step (0 to totalSteps)
     * @param status The status message
     */
    public void updateProgress(int step, String status) {
        SwingUtilities.invokeLater(() -> {
            currentStep.set(step);
            progressBar.setValue(step);
            progressBar.setString(step + "/" + totalSteps + " (" + (step * 100 / totalSteps) + "%)");
            statusLabel.setText(status);
            

            
            System.out.println("📊 Progress: " + step + "/" + totalSteps + " - " + status);
        });
    }
    

    
    /**
     * Set the total number of steps
     * @param totalSteps The total number of initialization steps
     */
    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
        SwingUtilities.invokeLater(() -> {
            progressBar.setMaximum(totalSteps);
        });
    }
    
    /**
     * Get the progress frame for potential JavaFX integration
     * @return The progress frame
     */
    public JFrame getProgressFrame() {
        return progressFrame;
    }
} 