package launcher.lifecycle.start;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom flat and minimal progress bar UI with modern styling.
 * This class provides a clean, contemporary appearance for progress bars
 * with subtle animations and effects.
 *
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 8, 2025
 * @since 1.0
 */
public class ProgressBarStyling extends BasicProgressBarUI {
    
    private float shimmerOffset = 0.0f;
    
    /**
     * Set the shimmer offset for animation effects.
     * 
     * @param shimmerOffset The shimmer offset value (0.0 to 1.0)
     */
    public void setShimmerOffset(float shimmerOffset) {
        this.shimmerOffset = shimmerOffset;
    }
    
    /**
     * Get the current shimmer offset value.
     * 
     * @return The current shimmer offset
     */
    public float getShimmerOffset() {
        return shimmerOffset;
    }
    
    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        
        int width = c.getWidth();
        int height = c.getHeight();
        
        // Enhanced outer shadow with multiple layers
        paintMultiLayeredShadow(g2d, width, height);
        
        // Flat background with subtle gradient
        paintGlassBackground(g2d, width, height);
        
        // Add subtle inner shadow
        paintInnerShadow(g2d, width, height);
        
        // Calculate progress
        int progressWidth = (int) (width * progressBar.getPercentComplete());
        
        // Draw progress with modern effects
        if (progressWidth > 0) {
            paintProgressFill(g2d, progressWidth, height);
            paintShimmerEffect(g2d, progressWidth, height);
            paintElectricGlow(g2d, progressWidth, height);
            paintGlassHighlight(g2d, progressWidth, height);
            paintDepthShadow(g2d, progressWidth, height);
            paintAnimatedParticles(g2d, progressWidth, height);
        }
        
        // Enhanced border with minimal effects
        paintBorderSystem(g2d, width, height);
        
        g2d.dispose();
    }
    
    @Override
    protected void paintIndeterminate(Graphics g, JComponent c) {
        paintDeterminate(g, c);
    }
    
    /**
     * Paint multi-layered shadow for depth effect.
     */
    private void paintMultiLayeredShadow(Graphics2D g2d, int width, int height) {
        for (int i = 3; i >= 1; i--) {
            g2d.setColor(new Color(0, 0, 0, 15 - i * 3));
            g2d.fillRoundRect(i, i, width - i * 2, height - i * 2, 25, 25);
        }
    }
    
    /**
     * Paint flat background with subtle gradient.
     */
    private void paintGlassBackground(Graphics2D g2d, int width, int height) {
        GradientPaint bgGradient = new GradientPaint(
            0, 0, new Color(245, 245, 245),
            0, height, new Color(235, 235, 235)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRoundRect(0, 0, width, height, 25, 25);
    }
    
    /**
     * Paint subtle inner shadow for minimal depth.
     */
    private void paintInnerShadow(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(0, 0, 0, 15));
        g2d.fillRoundRect(1, 1, width - 2, height / 2, 24, 24);
    }
    
    /**
     * Paint the main progress fill with gradient matching the main app.
     */
    private void paintProgressFill(Graphics2D g2d, int progressWidth, int height) {
        GradientPaint mainGradient = new GradientPaint(
            0, 0, new Color(0, 123, 255),  // #007bff - bright blue
            progressWidth, 0, new Color(0, 86, 179)   // #0056b3 - darker blue
        );
        g2d.setPaint(mainGradient);
        g2d.fillRoundRect(2, 2, progressWidth - 4, height - 4, 23, 23);
    }
    
    /**
     * Paint animated shimmer effect.
     */
    private void paintShimmerEffect(Graphics2D g2d, int progressWidth, int height) {
        int shimmerWidth = progressWidth / 2;
        int shimmerX = (int) (shimmerOffset * progressWidth);
        GradientPaint shimmerGradient = new GradientPaint(
            shimmerX, 0, new Color(255, 255, 255, 0),
            shimmerX + shimmerWidth / 2, 0, new Color(255, 255, 255, 80)
        );
        g2d.setPaint(shimmerGradient);
        g2d.fillRoundRect(2, 2, progressWidth - 4, height - 4, 23, 23);
    }
    
    /**
     * Paint subtle glow effect around progress.
     */
    private void paintElectricGlow(Graphics2D g2d, int progressWidth, int height) {
        g2d.setColor(new Color(0, 123, 255, 15));  // #007bff with minimal transparency
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(1, 1, progressWidth - 2, height - 2, 24, 24);
    }
    
    /**
     * Paint subtle highlight on top of progress.
     */
    private void paintGlassHighlight(Graphics2D g2d, int progressWidth, int height) {
        GradientPaint highlightGradient = new GradientPaint(
            0, 2, new Color(255, 255, 255, 30),
            0, height / 3, new Color(255, 255, 255, 5)
        );
        g2d.setPaint(highlightGradient);
        g2d.fillRoundRect(2, 2, progressWidth - 4, height / 3, 23, 23);
    }
    
    /**
     * Paint subtle shadow on bottom of progress.
     */
    private void paintDepthShadow(Graphics2D g2d, int progressWidth, int height) {
        g2d.setColor(new Color(0, 0, 0, 10));
        g2d.fillRoundRect(2, height - height / 3, progressWidth - 4, height / 3, 23, 23);
    }
    
    /**
     * Paint subtle animated particle effects.
     */
    private void paintAnimatedParticles(Graphics2D g2d, int progressWidth, int height) {
        if (progressWidth > 20) {
            for (int i = 0; i < 2; i++) {  // Reduced from 3 to 2 particles
                int particleX = (int) (shimmerOffset * progressWidth) + (i * 20);
                if (particleX < progressWidth - 10) {
                    g2d.setColor(new Color(255, 255, 255, 40));  // Much more subtle
                    g2d.fillOval(particleX, height / 2 - 1, 2, 2);  // Smaller particles
                }
            }
        }
    }
    
    /**
     * Paint the complete border system with minimal effects.
     */
    private void paintBorderSystem(Graphics2D g2d, int width, int height) {
        // Main border with subtle gradient
        GradientPaint borderGradient = new GradientPaint(
            0, 0, new Color(200, 200, 200),
            0, height, new Color(180, 180, 180)
        );
        g2d.setPaint(borderGradient);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(0, 0, width - 1, height - 1, 25, 25);
        
        // Subtle outer glow effect
        g2d.setColor(new Color(0, 123, 255, 10));  // #007bff with minimal transparency
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(-1, -1, width + 1, height + 1, 26, 26);
        
        // Very subtle inner border
        g2d.setColor(new Color(255, 255, 255, 15));
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawRoundRect(1, 1, width - 3, height - 3, 23, 23);
    }
} 