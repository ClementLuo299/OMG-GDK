package launcher.core.lifecycle.start.startup_window.styling.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import launcher.core.lifecycle.start.startup_window.styling.theme.Colors;
import launcher.core.lifecycle.start.startup_window.styling.theme.Dimensions;

/**
 * Custom modern progress bar UI with rounded corners, glow effects, and vibrant gradients.
 * 
 * @author Clement Luo
 * @date August 8, 2025
 * @edited December 26, 2025
 * @since Beta 1.0
 */
public class ProgressBarStyling extends BasicProgressBarUI {
    
    /** Smooth progress value (0.0 to 1.0) used for animation instead of progressBar.getPercentComplete(). */
    private Double smoothProgress = null;
    
    /**
     * Sets the smooth progress value for animation.
     * When set, this overrides the progress bar's internal percentage calculation.
     * 
     * @param progress The progress value (0.0 to 1.0), or null to use default behavior
     */
    public void setSmoothProgress(Double progress) {
        this.smoothProgress = progress;
    }
    
    /**
     * Paints the determinate progress bar with modern styling including rounded corners,
     * gradients, glow effects, and borders. This method orchestrates the entire rendering
     * process by calling specialized painting methods for each visual component.
     * 
     * @param g The graphics context used for painting
     * @param c The component (progress bar) being painted
     */
    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            // Get component bounds directly - no inset manipulation
            Rectangle bounds = c.getBounds();
            int width = bounds.width;
            int height = bounds.height;
            
            // Skip rendering if component has invalid dimensions
            if (width <= 0 || height <= 0) {
                return;
            }
            
            // Set component-level clip: ALL drawing is constrained to component bounds
            // Use the component's actual bounds (0, 0, width, height)
            Shape originalClip = g2d.getClip();
            Rectangle componentClip = new Rectangle(0, 0, width, height);
            Area componentClipArea = new Area(componentClip);
            if (originalClip != null) {
                componentClipArea.intersect(new Area(originalClip));
            }
            g2d.setClip(componentClipArea);
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // Modern rounded background
            paintBackground(g2d, width, height);
            
            // Use smooth progress if available, otherwise use standard progress bar percentage
            double progressPercent = (smoothProgress != null) ? smoothProgress : progressBar.getPercentComplete();
            
            // Add a small initial prefill (equivalent to ~2% or minimum 4 pixels) to prevent overflow
            // This ensures the progress bar always has a small fill from the start
            double minProgress = Math.max(0.02, 4.0 / width);
            double adjustedProgress = Math.max(progressPercent, minProgress);
            
            // Calculate progress width and ensure it never exceeds width
            int progressWidth = Math.min((int) (width * adjustedProgress), width);
            
            // Draw progress fill with glow effect
            paintProgressFill(g2d, progressWidth, width, height);
            
            // Subtle border (very light)
            paintBorder(g2d, width, height);
            
            // Restore original clip
            g2d.setClip(originalClip);
        } finally {
            g2d.dispose();
        }
    }
    
    /**
     * Paints the rounded background of the progress bar.
     * Uses the theme's background color to match the panel appearance.
     * 
     * @param g2d The graphics context for 2D rendering
     * @param width The width of the progress bar
     * @param height The height of the progress bar
     */
    private void paintBackground(Graphics2D g2d, int width, int height) {
        // Modern rounded background - match the panel background
        g2d.setColor(Colors.BACKGROUND);
        RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
            0, 0, width, height,
            Dimensions.PROGRESS_CORNER_RADIUS,
            Dimensions.PROGRESS_CORNER_RADIUS
        );
        g2d.fill(roundedRect);
    }
    
    /**
     * Paints the progress fill with rounded corners, gradient colors, glow effects, and highlights.
     * This method renders the actual progress indicator that shows how much of the task is complete.
     * It applies clipping to ensure the fill stays within bounds and adds visual effects for depth.
     * 
     * @param g2d The graphics context for 2D rendering
     * @param progressWidth The width of the filled portion (based on progress percentage)
     * @param totalWidth The total width of the progress bar
     * @param height The height of the progress bar
     */
    private void paintProgressFill(Graphics2D g2d, int progressWidth, int totalWidth, int height) {
        // Skip if dimensions are invalid
        if (progressWidth <= 0 || totalWidth <= 0 || height <= 0) {
            return;
        }
        
        // Double-check: ensure progress width never exceeds total width
        int actualProgressWidth = Math.min(progressWidth, totalWidth);
        int cornerRadius = Dimensions.PROGRESS_CORNER_RADIUS;
        
        // Save current clip
        Shape currentClip = g2d.getClip();
        
        // Create a rounded rectangle clip for the progress fill
        RoundRectangle2D progressShape = new RoundRectangle2D.Float(
            0, 0, actualProgressWidth, height,
            cornerRadius, cornerRadius
        );
        Area progressClipArea = new Area(progressShape);
        if (currentClip != null) {
            progressClipArea.intersect(new Area(currentClip));
        }
        g2d.setClip(progressClipArea);
        
        // Draw glow effect (subtle inner glow)
        if (actualProgressWidth > cornerRadius) {
            paintGlowEffect(g2d, actualProgressWidth, height);
        }
        
        // Vibrant purple-to-blue gradient
        int gradientEnd = Math.max(actualProgressWidth, 1);
        GradientPaint mainGradient = new GradientPaint(
            0, 0, Colors.PROGRESS_START, // Vibrant purple
            gradientEnd, 0, Colors.PROGRESS_END  // Vibrant blue
        );
        g2d.setPaint(mainGradient);
        
        // Fill the rounded rectangle
        RoundRectangle2D fillRect = new RoundRectangle2D.Float(
            0, 0, actualProgressWidth, height,
            cornerRadius, cornerRadius
        );
        g2d.fill(fillRect);
        
        // Add subtle highlight on top edge for depth
        if (actualProgressWidth > 4) {
            g2d.setPaint(new GradientPaint(
                0, 0, new Color(255, 255, 255, 40),
                0, height / 3, new Color(255, 255, 255, 0)
            ));
            g2d.fill(fillRect);
        }
        
        // Restore clip
        g2d.setClip(currentClip);
    }
    
    /**
     * Paints a subtle glow effect around the progress fill.
     * Uses multiple semi-transparent layers with decreasing opacity to create a soft,
     * layered glow effect that extends beyond the progress fill boundaries.
     * 
     * @param g2d The graphics context for 2D rendering
     * @param width The width of the progress fill area
     * @param height The height of the progress fill area
     */
    private void paintGlowEffect(Graphics2D g2d, int width, int height) {
        int cornerRadius = Dimensions.PROGRESS_CORNER_RADIUS;
        Color glowColor = Colors.PROGRESS_GLOW;
        
        // Draw multiple layers with decreasing opacity for soft glow
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        
        for (int i = 1; i <= 3; i++) {
            float offset = i * 1.5f;
            float alpha = 0.4f / i; // Decreasing opacity
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 
                (int)(255 * alpha)));
            
            RoundRectangle2D glowShape = new RoundRectangle2D.Float(
                -offset, -offset,
                width + offset * 2, height + offset * 2,
                cornerRadius + offset,
                cornerRadius + offset
            );
            g2d.fill(glowShape);
        }
        
        g2d.setComposite(originalComposite);
    }
    
    /**
     * Paints a subtle border around the entire progress bar with rounded corners.
     * Uses the theme's border color and a 1-pixel stroke for definition.
     * 
     * @param g2d The graphics context for 2D rendering
     * @param width The width of the progress bar
     * @param height The height of the progress bar
     */
    private void paintBorder(Graphics2D g2d, int width, int height) {
        // More visible border for better definition
        g2d.setColor(Colors.PROGRESS_BORDER);
        g2d.setStroke(new BasicStroke(1.0f));
        RoundRectangle2D borderRect = new RoundRectangle2D.Float(
            0.5f, 0.5f, width - 1.0f, height - 1.0f,
            Dimensions.PROGRESS_CORNER_RADIUS,
            Dimensions.PROGRESS_CORNER_RADIUS
        );
        g2d.draw(borderRect);
    }
}

