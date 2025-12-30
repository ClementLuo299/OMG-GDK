package launcher.gui.lobby.ui_logic.managers.game;

import gdk.internal.Logging;
import launcher.gui.lobby.GDKViewModel;
import launcher.utils.module.ModuleCompiler;
import javafx.application.Platform;

import java.util.List;

/**
 * Handles UI reporting for module compilation checking.
 * Delegates business logic (checking for failures) to ViewModel.
 * 
 * @authors Clement Luo
 * @date December 27, 2025
 * @edited January 2025
 * @since 1.0
 */
public class ModuleCompilationChecker {
    
    /**
     * Interface for reporting messages to the UI.
     */
    public interface MessageReporter {
        void addMessage(String message);
    }
    
    private final GDKViewModel viewModel;
    private final MessageReporter messageReporter;
    
    /**
     * Create a new ModuleCompilationChecker.
     * 
     * @param viewModel The ViewModel for business logic (may be null initially)
     * @param messageReporter Callback to report messages to the UI
     */
    public ModuleCompilationChecker(GDKViewModel viewModel, MessageReporter messageReporter) {
        this.viewModel = viewModel;
        this.messageReporter = messageReporter;
    }
    
    /**
     * Check for compilation failures on startup.
     * Delegates business logic to ViewModel, handles UI reporting.
     */
    public void checkStartupCompilationFailures() {
        try {
            Logging.info("🚀 Checking for compilation failures on startup...");
            
            // Delegate compilation checking to ViewModel (business logic)
            final List<String> compilationFailures;
            if (viewModel != null) {
                compilationFailures = viewModel.checkForCompilationFailures();
            } else {
                // Fallback: get from ModuleCompiler directly
                compilationFailures = ModuleCompiler.getLastCompilationFailures();
                ModuleCompiler.clearCompilationFailures();
            }
            
            // Report failures to UI (UI logic)
            if (!compilationFailures.isEmpty()) {
                final List<String> failuresForUI = new java.util.ArrayList<>(compilationFailures);
                Platform.runLater(() -> {
                    for (String moduleName : failuresForUI) {
                        String message = "⚠️ Module '" + moduleName + "' failed to compile - check source code for errors";
                        messageReporter.addMessage(message);
                    }
                    String summaryMessage = "📋 Compilation failures detected in: " + String.join(", ", failuresForUI);
                    messageReporter.addMessage(summaryMessage);
                });
            }
            
            Logging.info("✅ Startup compilation failure check completed");
            
        } catch (Exception e) {
            Logging.error("❌ Error during startup compilation failure check: " + e.getMessage(), e);
        }
    }
    
}

