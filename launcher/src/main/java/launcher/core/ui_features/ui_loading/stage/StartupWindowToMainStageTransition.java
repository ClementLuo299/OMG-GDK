package launcher.core.ui_features.ui_loading.stage;

import javafx.application.Platform;
import javafx.stage.Stage;
import launcher.ui_areas.startup_window.StartupWindowManager;

/**
 * Utility class for transitioning from startup window to main application stage.
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public final class StartupWindowToMainStageTransition {

    private StartupWindowToMainStageTransition() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Shows the main application stage and hides the startup window.
     * Called after module ui_loading completes.
     * 
     * @param primaryApplicationStage The primary application stage
     * @param windowManager The startup window manager to hide
     */
    public static void showMainStage(Stage primaryApplicationStage, StartupWindowManager windowManager) {
        windowManager.hide();
        Platform.runLater(() -> {
            primaryApplicationStage.setOpacity(1.0);
            primaryApplicationStage.show();
        });
    }
}

