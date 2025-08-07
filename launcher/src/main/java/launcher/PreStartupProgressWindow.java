package launcher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;
import java.util.TimerTask;

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
    private int totalSteps = 15; // More granular steps for better progress tracking
    
    // Animation support
    private Timer animationTimer;
    private String fullMessage = "";
    private int currentCharIndex = 0;
    private boolean isAnimating = false;
    
    // Progress bar animation
    private Timer progressAnimationTimer;
    private float shimmerOffset = 0.0f;
    
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
        
        // Remove the close button from the title bar by using undecorated window
        progressFrame.setUndecorated(true);
        progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Enable window shadow (if supported)
        try {
            progressFrame.setBackground(new Color(0, 0, 0, 0));
        } catch (Exception e) {
            // Fallback for systems that don't support transparency
            progressFrame.setBackground(new Color(248, 249, 250));
        }
        
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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Initializing");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(149, 165, 166));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Progress bar
        progressBar = new JProgressBar(0, totalSteps);
        progressBar.setPreferredSize(new Dimension(500, 25));
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        progressBar.setBorderPainted(false);
        progressBar.setOpaque(false);
        
        // Ultra-modern progress bar styling
        progressBar.setUI(new BasicProgressBarUI() {
            @Override
            protected void paintDeterminate(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int width = c.getWidth();
                int height = c.getHeight();
                
                // Create shadow effect
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, width - 4, height - 4, 15, 15);
                
                // Draw background with rounded corners and gradient
                GradientPaint bgGradient = new GradientPaint(
                    0, 0, new Color(44, 62, 80),
                    0, height, new Color(52, 73, 94)
                );
                g2d.setPaint(bgGradient);
                g2d.fillRoundRect(0, 0, width, height, 15, 15);
                
                // Calculate progress
                int progressWidth = (int) (width * progressBar.getPercentComplete());
                
                // Draw progress with advanced gradient and rounded corners
                if (progressWidth > 0) {
                    // Main gradient
                    GradientPaint mainGradient = new GradientPaint(
                        0, 0, new Color(46, 204, 113),
                        progressWidth, 0, new Color(52, 152, 219)
                    );
                    g2d.setPaint(mainGradient);
                    g2d.fillRoundRect(2, 2, progressWidth - 4, height - 4, 13, 13);
                    
                    // Add animated shimmer effect
                    int shimmerWidth = progressWidth / 3;
                    int shimmerX = (int) (shimmerOffset * progressWidth);
                    GradientPaint shimmerGradient = new GradientPaint(
                        shimmerX, 0, new Color(255, 255, 255, 0),
                        shimmerX + shimmerWidth / 2, 0, new Color(255, 255, 255, 50)
                    );
                    g2d.setPaint(shimmerGradient);
                    g2d.fillRoundRect(2, 2, progressWidth - 4, height - 4, 13, 13);
                    
                    // Add top highlight
                    g2d.setColor(new Color(255, 255, 255, 60));
                    g2d.fillRoundRect(2, 2, progressWidth - 4, (height - 4) / 3, 13, 13);
                    
                    // Add bottom shadow
                    g2d.setColor(new Color(0, 0, 0, 20));
                    g2d.fillRoundRect(2, height - (height - 4) / 3, progressWidth - 4, (height - 4) / 3, 13, 13);
                }
                
                // Draw border with gradient
                GradientPaint borderGradient = new GradientPaint(
                    0, 0, new Color(44, 62, 80),
                    0, height, new Color(52, 73, 94)
                );
                g2d.setPaint(borderGradient);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRoundRect(0, 0, width - 1, height - 1, 15, 15);
                
                // Add inner glow
                g2d.setColor(new Color(46, 204, 113, 30));
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(1, 1, width - 3, height - 3, 14, 14);
                
                g2d.dispose();
            }
            
            @Override
            protected void paintIndeterminate(Graphics g, JComponent c) {
                paintDeterminate(g, c);
            }
        });
        
        // Status label
        statusLabel = new JLabel("Starting up...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(new Color(52, 73, 94));
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
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        mainPanel.setBackground(new Color(255, 255, 255));
        
        // Add subtle border
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(40, 40, 40, 40)
        ));
        
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
        panel.setBackground(new Color(255, 255, 255));
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
            startProgressAnimation();
        } else {
            SwingUtilities.invokeLater(() -> {
                progressFrame.setVisible(true);
                startProgressAnimation();
            });
        }
        

    }
    
    /**
     * Hide the progress window
     */
    public void hide() {
        System.out.println("🏁 Hiding pre-startup progress window");
        
        // Stop any ongoing animation
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
        }
        
        // Stop progress animation
        if (progressAnimationTimer != null) {
            progressAnimationTimer.cancel();
            progressAnimationTimer = null;
        }
        
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
     * Start the progress bar shimmer animation
     */
    private void startProgressAnimation() {
        if (progressAnimationTimer != null) {
            progressAnimationTimer.cancel();
        }
        
        progressAnimationTimer = new Timer();
        progressAnimationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                shimmerOffset += 0.1f;
                if (shimmerOffset > 1.0f) {
                    shimmerOffset = 0.0f;
                }
                
                SwingUtilities.invokeLater(() -> {
                    progressBar.repaint();
                });
            }
        }, 0, 50); // 20 FPS animation
    }
    
    /**
     * Update progress and status with animated text
     * @param step The current step (0 to totalSteps)
     * @param status The status message
     */
    public void updateProgress(int step, String status) {
        SwingUtilities.invokeLater(() -> {
            currentStep.set(step);
            progressBar.setValue(step);
            progressBar.setString(step + "/" + totalSteps + " (" + (step * 100 / totalSteps) + "%)");
            
            // Start animated text display
            animateText(status);
            
            System.out.println("📊 Progress: " + step + "/" + totalSteps + " - " + status);
        });
    }
    
    /**
     * Animate text with repeating animated dots
     * @param message The full message to animate
     */
    private void animateText(String message) {
        // Stop any existing animation
        if (animationTimer != null) {
            animationTimer.cancel();
        }
        
        // Check if message ends with "..."
        if (message.endsWith("...")) {
            String baseMessage = message.substring(0, message.length() - 3);
            fullMessage = baseMessage;
            currentCharIndex = 0;
            isAnimating = true;
            
            // Create new timer for animation
            animationTimer = new Timer();
            animationTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        StringBuilder animatedText = new StringBuilder(baseMessage);
                        for (int i = 0; i < currentCharIndex; i++) {
                            animatedText.append(".");
                        }
                        statusLabel.setText(animatedText.toString());
                        
                        // Cycle through 0, 1, 2, 3 dots (repeating)
                        currentCharIndex = (currentCharIndex + 1) % 4;
                    });
                }
            }, 0, 300); // 300ms delay between dots for better effect
        } else {
            // For messages without dots, just set the text directly
            statusLabel.setText(message);
        }
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