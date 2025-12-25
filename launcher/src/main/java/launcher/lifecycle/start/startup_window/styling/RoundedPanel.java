package launcher.lifecycle.start.startup_window.styling;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A JPanel with rounded corners and optional shadow effect.
 * Used for modern UI styling.
 */
public class RoundedPanel extends JPanel {
    
    private final int cornerRadius;
    private final boolean drawShadow;
    
    public RoundedPanel(int cornerRadius, boolean drawShadow) {
        this.cornerRadius = cornerRadius;
        this.drawShadow = drawShadow;
        setOpaque(false); // Make transparent to show rounded corners
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            int width = getWidth();
            int height = getHeight();
            
            // Draw shadow if enabled
            if (drawShadow) {
                paintShadow(g2d, width, height);
            }
            
            // Draw rounded background
            RoundRectangle2D roundedRect = new RoundRectangle2D.Float(
                drawShadow ? StartupWindowTheme.SHADOW_OFFSET : 0,
                drawShadow ? StartupWindowTheme.SHADOW_OFFSET : 0,
                width - (drawShadow ? StartupWindowTheme.SHADOW_OFFSET * 2 : 0),
                height - (drawShadow ? StartupWindowTheme.SHADOW_OFFSET * 2 : 0),
                cornerRadius, cornerRadius
            );
            
            g2d.setColor(StartupWindowTheme.BACKGROUND);
            g2d.fill(roundedRect);
            
            // Draw subtle border
            g2d.setColor(StartupWindowTheme.BORDER);
            g2d.setStroke(new BasicStroke(0.5f));
            g2d.draw(roundedRect);
        } finally {
            g2d.dispose();
        }
    }
    
    private void paintShadow(Graphics2D g2d, int width, int height) {
        int shadowBlur = StartupWindowTheme.SHADOW_BLUR;
        int shadowOffset = StartupWindowTheme.SHADOW_OFFSET;
        Color shadowColor = StartupWindowTheme.SHADOW_COLOR;
        
        // Draw multiple shadow layers for soft shadow effect
        for (int i = 0; i < shadowBlur; i++) {
            float alpha = (float) (shadowColor.getAlpha() * (1.0 - (double) i / shadowBlur)) / 255f;
            g2d.setColor(new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), (int) (alpha * 255)));
            
            RoundRectangle2D shadowRect = new RoundRectangle2D.Float(
                shadowOffset + i * 0.3f,
                shadowOffset + i * 0.3f,
                width - shadowOffset * 2 - i * 0.6f,
                height - shadowOffset * 2 - i * 0.6f,
                cornerRadius + i * 0.2f,
                cornerRadius + i * 0.2f
            );
            g2d.fill(shadowRect);
        }
    }
}

