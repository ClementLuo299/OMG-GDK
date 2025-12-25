package launcher.lifecycle.start.startup_window;

/**
 * Interface for startup window implementations.
 * Allows different implementations (Swing, JavaFX, etc.)
 */
public interface IStartupWindow {
    void show();
    void hide();
    void updateProgress(int step, String status);
    void setSmoothProgress(double progress);
    void updateStatusText(String text);
    // Note: getProgressBar() returns null for Swing, JavaFX ProgressBar for JavaFX implementations
    javafx.scene.control.ProgressBar getProgressBar();
}

