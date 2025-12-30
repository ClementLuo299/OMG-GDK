package launcher.gui.lobby.ui_logic.managers;

import gdk.internal.Logging;

import java.util.HashSet;
import java.util.Set;

/**
 * Reports changes in available game modules to the UI.
 * Handles module change analysis and message reporting.
 * 
 * @authors Clement Luo
 * @date December 29, 2025
 * @since Beta 1.0
 */
public class ModuleChangeReporter {
    
    /**
     * Interface for reporting messages to the UI.
     */
    public interface MessageReporter {
        void addMessage(String message);
    }
    
    private final MessageReporter messageReporter;
    
    /**
     * Create a new ModuleChangeReporter.
     * 
     * @param messageReporter Callback to report messages to the UI
     */
    public ModuleChangeReporter(MessageReporter messageReporter) {
        this.messageReporter = messageReporter;
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

