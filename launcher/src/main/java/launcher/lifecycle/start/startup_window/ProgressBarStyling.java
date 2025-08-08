package launcher.lifecycle.start.startup_window;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom flat and minimal progress bar UI with modern styling.
 */
public class ProgressBarStyling extends BasicProgressBarUI {
    
    private float shimmerOffset = 0.0f;
    
    public void setShimmerOffset(float shimmerOffset) {
        this.shimmerOffset = shimmerOffset;
    }
    
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
        paintMultiLayeredShadow(g2d, width, height);
        paintGlassBackground(g2d, width, height);
        paintInnerShadow(g2d, width, height);
        int progressWidth = (int) (width * progressBar.getPercentComplete());
        if (progressWidth > 0) {
            paintProgressFill(g2d, progressWidth, height);
            paintShimmerEffect(g2d, progressWidth, height);
            paintElectricGlow(g2d, progressWidth, height);
            paintGlassHighlight(g2d, progressWidth, height);
            paintDepthShadow(g2d, progressWidth, height);
            paintAnimatedParticles(g2d, progressWidth, height);
        }
        paintBorderSystem(g2d, width, height);
        g2d.dispose();
    }
    
    @Override
    protected void paintIndeterminate(Graphics g, JComponent c) {
        paintDeterminate(g, c);
    }
    
    private void paintMultiLayeredShadow(Graphics2D g2d, int width, int height) {
        for (int i = 3; i >= 1; i--) {
            g2d.setColor(new Color(0, 0, 0, 15 - i * 3));
            g2d.fillRoundRect(i, i, width - i * 2, height - i * 2, 25, 25);
        }
    }
    
    private void paintGlassBackground(Graphics2D g2d, int width, int height) {
        GradientPaint bgGradient = new GradientPaint(
            0, 0, new Color(245, 245, 245),
            0, height, new Color(235, 235, 235)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRoundRect(0, 0, width, height, 25, 25);
    }
    
    private void paintInnerShadow(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(0, 0, 0, 15));
        g2d.fillRoundRect(1, 1, width - 2, height / 2, 24, 24);
    }
    
    private void paintProgressFill(Graphics2D g2d, int progressWidth, int height) {
        GradientPaint mainGradient = new GradientPaint(
            0, 0, new Color(0, 123, 255),
            progressWidth, 0, new Color(0, 86, 179)
        );
        g2d.setPaint(mainGradient);
        g2d.fillRoundRect(2, 2, progressWidth - 4, height - 4, 23, 23);
    }
    
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
    
    private void paintElectricGlow(Graphics2D g2d, int progressWidth, int height) {
        g2d.setColor(new Color(0, 123, 255, 15));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(1, 1, progressWidth - 2, height - 2, 24, 24);
    }
    
    private void paintGlassHighlight(Graphics2D g2d, int progressWidth, int height) {
        GradientPaint highlightGradient = new GradientPaint(
            0, 2, new Color(255, 255, 255, 30),
            0, height / 3, new Color(255, 255, 255, 5)
        );
        g2d.setPaint(highlightGradient);
        g2d.fillRoundRect(2, 2, progressWidth - 4, height / 3, 23, 23);
    }
    
    private void paintDepthShadow(Graphics2D g2d, int progressWidth, int height) {
        g2d.setColor(new Color(0, 0, 0, 10));
        g2d.fillRoundRect(2, height - height / 3, progressWidth - 4, height / 3, 23, 23);
    }
    
    private void paintAnimatedParticles(Graphics2D g2d, int progressWidth, int height) {
        if (progressWidth > 20) {
            for (int i = 0; i < 2; i++) {
                int particleX = (int) (shimmerOffset * progressWidth) + (i * 20);
                if (particleX < progressWidth - 10) {
                    g2d.setColor(new Color(255, 255, 255, 40));
                    g2d.fillOval(particleX, height / 2 - 1, 2, 2);
                }
            }
        }
    }
    
    private void paintBorderSystem(Graphics2D g2d, int width, int height) {
        GradientPaint borderGradient = new GradientPaint(
            0, 0, new Color(200, 200, 200),
            0, height, new Color(180, 180, 180)
        );
        g2d.setPaint(borderGradient);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(0, 0, width - 1, height - 1, 25, 25);
        g2d.setColor(new Color(0, 123, 255, 10));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(-1, -1, width + 1, height + 1, 26, 26);
        g2d.setColor(new Color(255, 255, 255, 15));
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawRoundRect(1, 1, width - 3, height - 3, 23, 23);
    }
} 