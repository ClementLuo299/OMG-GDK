package launcher.features.lobby_features.managers.game_launching;

import gdk.internal.Logging;

import java.util.HashSet;
import java.util.Set;

/**
 * Reports changes in available game modules to the UI.
 * 
 * <p>This class analyzes differences between previous and current module sets,
 * and reports additions, removals, or no changes to the user via messages.
 * 
 * <p>Changes are only reported for subsequent reloads (not on first load),
 * to avoid cluttering the UI with initial module detection messages.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 30, 2025
 * @since Beta 1.0
 */
public class ModuleChangesReporter {
    
    // ==================== INTERFACES ====================
    
    /**
     * Interface for reporting messages to the UI.
     * Allows this reporter to send messages without direct UI dependencies.
     */
    public interface MessageReporter {
        /**
         * Adds a message to be displayed to the user.
         * 
         * @param message The message to display
         */
        void addMessage(String message);
    }
    
    // ==================== DEPENDENCIES ====================
    
    /** Callback for reporting messages to the UI. */
    private final MessageReporter messageReporter;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Creates a new ModuleChangesReporter.
     * 
     * @param messageReporter Callback to report messages to the UI
     */
    public ModuleChangesReporter(MessageReporter messageReporter) {
        this.messageReporter = messageReporter;
    }
    
    // ==================== PUBLIC METHODS ====================
    
    /**
     * Reports changes in available game modules.
     * 
     * <p>This method:
     * <ol>
     *   <li>Analyzes differences between previous and current module sets</li>
     *   <li>Logs the analysis for debugging</li>
     *   <li>Reports changes to the UI (only for subsequent reloads, not first load)</li>
     * </ol>
     * 
     * <p>If this is the first load (previousModuleNames is empty), no changes
     * are reported to avoid cluttering the UI with initial detection messages.
     * 
     * @param previousModuleNames Set of module names that were available before refresh
     * @param currentModuleNames Set of module names that are available after refresh
     */
    public void reportModuleChanges(Set<String> previousModuleNames, Set<String> currentModuleNames) {
        // Analyze changes: find added and removed modules
        Set<String> addedModules = findAddedModules(previousModuleNames, currentModuleNames);
        Set<String> removedModules = findRemovedModules(previousModuleNames, currentModuleNames);
        
        // Log the analysis for debugging
        logModuleChanges(previousModuleNames, currentModuleNames, addedModules, removedModules);
        
        // Only report changes for subsequent reloads (not first time loading)
        if (!previousModuleNames.isEmpty()) {
            reportChangesToUI(addedModules, removedModules, currentModuleNames.size());
        }
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Finds modules that were added (present in current but not in previous).
     * 
     * @param previousModuleNames Set of previous module names
     * @param currentModuleNames Set of current module names
     * @return Set of added module names
     */
    private Set<String> findAddedModules(Set<String> previousModuleNames, Set<String> currentModuleNames) {
        Set<String> addedModules = new HashSet<>(currentModuleNames);
        addedModules.removeAll(previousModuleNames);
        return addedModules;
    }
    
    /**
     * Finds modules that were removed (present in previous but not in current).
     * 
     * @param previousModuleNames Set of previous module names
     * @param currentModuleNames Set of current module names
     * @return Set of removed module names
     */
    private Set<String> findRemovedModules(Set<String> previousModuleNames, Set<String> currentModuleNames) {
        Set<String> removedModules = new HashSet<>(previousModuleNames);
        removedModules.removeAll(currentModuleNames);
        return removedModules;
    }
    
    /**
     * Logs module change analysis for debugging purposes.
     * 
     * @param previousModuleNames Set of previous module names
     * @param currentModuleNames Set of current module names
     * @param addedModules Set of added module names
     * @param removedModules Set of removed module names
     */
    private void logModuleChanges(Set<String> previousModuleNames,
                                  Set<String> currentModuleNames,
                                  Set<String> addedModules,
                                  Set<String> removedModules) {
        Logging.info("Module change analysis:");
        Logging.info("   Previous modules: " + formatModuleSet(previousModuleNames));
        Logging.info("   Current modules: " + formatModuleSet(currentModuleNames));
        Logging.info("   Added modules: " + formatModuleSet(addedModules));
        Logging.info("   Removed modules: " + formatModuleSet(removedModules));
    }
    
    /**
     * Formats a module set for logging (handles empty sets).
     * 
     * @param modules Set of module names
     * @return Formatted string representation
     */
    private String formatModuleSet(Set<String> modules) {
        return modules.isEmpty() ? "none" : String.join(", ", modules);
    }
    
    /**
     * Reports module changes to the UI.
     * 
     * @param addedModules Set of added module names
     * @param removedModules Set of removed module names
     * @param currentModuleCount Current number of modules
     */
    private void reportChangesToUI(Set<String> addedModules, Set<String> removedModules, int currentModuleCount) {
        // Report each added module individually
        for (String moduleName : addedModules) {
            String message = "Added game module: " + moduleName;
            messageReporter.addMessage(message);
            Logging.info(message);
        }
        
        // Report each removed module individually
        for (String moduleName : removedModules) {
            String message = "Removed game module: " + moduleName;
            messageReporter.addMessage(message);
            Logging.info(message);
        }
        
        // Report no changes if nothing changed
        if (addedModules.isEmpty() && removedModules.isEmpty()) {
            String message = "No changes detected - " + currentModuleCount + " game module(s) available";
            messageReporter.addMessage(message);
            Logging.info(message);
        }
    }
}

