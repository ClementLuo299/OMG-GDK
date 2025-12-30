package launcher.gui.lobby.ui_logic.managers;

import gdk.internal.Logging;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.HashSet;
import java.util.Set;

/**
 * Manages UI state updates for the game lobby controller.
 * 
 * Handles updating status labels, button states, and reporting module changes.
 * 
 * @authors Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since 1.0
 */
public class UIStateManager {
    
    /**
     * Interface for reporting messages to the UI.
     */
    public interface MessageReporter {
        void addMessage(String message);
    }
    
    private final Label statusLabel;
    private final Button launchGameButton;
    private final MessageReporter messageReporter;
    
    /**
     * Create a new UIStateManager.
     * 
     * @param statusLabel The status label to update
     * @param launchGameButton The launch button to update
     * @param messageReporter Callback to report messages to the UI
     */
    public UIStateManager(Label statusLabel, Button launchGameButton, MessageReporter messageReporter) {
        this.statusLabel = statusLabel;
        this.launchGameButton = launchGameButton;
        this.messageReporter = messageReporter;
    }
    
    /**
     * Update the game count status label.
     * 
     * @param gameCount The number of available games
     */
    public void updateGameCountStatus(int gameCount) {
        if (statusLabel != null) {
            statusLabel.setText("Available Games: " + gameCount);
            Logging.info("📊 UI Status updated: " + gameCount + " games available");
        }
    }
    
    /**
     * Update the launch button state based on whether a game is selected.
     * 
     * @param hasSelectedGame Whether a game is currently selected
     */
    public void updateLaunchButtonState(boolean hasSelectedGame) {
        if (launchGameButton != null) {
            launchGameButton.setDisable(!hasSelectedGame);
            
            if (hasSelectedGame) {
                launchGameButton.setStyle("-fx-alignment: center; -fx-content-display: text-only; -fx-min-height: 45px; -fx-pref-height: 45px; -fx-max-height: 45px; -fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: transparent; -fx-cursor: hand; -fx-padding: 10 20; -fx-min-width: 120;");
            } else {
                launchGameButton.setStyle("-fx-alignment: center; -fx-content-display: text-only; -fx-min-height: 45px; -fx-pref-height: 45px; -fx-max-height: 45px; -fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-border-color: transparent; -fx-cursor: default; -fx-padding: 10 20; -fx-min-width: 120; -fx-opacity: 0.6;");
            }
        }
    }
    
    /**
     * Report changes in available game modules (only for subsequent reloads).
     * 
     * @param previousModuleNames Set of module names that were available before refresh
     * @param currentModuleNames Set of module names that are available after refresh
     */
    public void reportModuleChanges(Set<String> previousModuleNames, Set<String> currentModuleNames) {
        // Find added modules (in current but not in previous)
        Set<String> addedModules = new HashSet<>(currentModuleNames);
        addedModules.removeAll(previousModuleNames);
        
        // Find removed modules (in previous but not in current)
        Set<String> removedModules = new HashSet<>(previousModuleNames);
        removedModules.removeAll(currentModuleNames);
        
        // Log the changes for debugging
        Logging.info("📊 Module change analysis:");
        Logging.info("   Previous modules: " + (previousModuleNames.isEmpty() ? "none" : String.join(", ", previousModuleNames)));
        Logging.info("   Current modules: " + (currentModuleNames.isEmpty() ? "none" : String.join(", ", currentModuleNames)));
        Logging.info("   Added modules: " + (addedModules.isEmpty() ? "none" : String.join(", ", addedModules)));
        Logging.info("   Removed modules: " + (removedModules.isEmpty() ? "none" : String.join(", ", removedModules)));
        
        // Only report changes for subsequent reloads (not first time loading)
        if (!previousModuleNames.isEmpty()) {
            // Report each added module individually
            for (String moduleName : addedModules) {
                messageReporter.addMessage("🆕 Added game module: " + moduleName);
                Logging.info("🆕 Added game module: " + moduleName);
            }
            
            // Report each removed module individually
            for (String moduleName : removedModules) {
                messageReporter.addMessage("🗑️ Removed game module: " + moduleName);
                Logging.info("🗑️ Removed game module: " + moduleName);
            }
            
            // Report no changes if nothing changed
            if (addedModules.isEmpty() && removedModules.isEmpty()) {
                String message = "✅ No changes detected - " + currentModuleNames.size() + " game module(s) available";
                messageReporter.addMessage(message);
                Logging.info(message);
            }
        }
    }
}

