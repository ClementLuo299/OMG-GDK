package launcher.features.module_handling.on_app_start.helpers.thread_tasks_helpers;


import java.util.List;

/**
 * Reports the count of successfully loaded modules.
 * Responsible for reporting the number of modules found and loaded.
 * 
 * @author Clement Luo
 * @date December 27, 2025
 * @edited December 27, 2025
 * @since Beta 1.0
 */
public final class ModuleCountReporter {
    
    private ModuleCountReporter() {}
    
    /**
     * Reports the number of modules found and loaded.
     * 
     * @param discoveredModules List of discovered modules
     */
    public static void reportModuleCount(List<?> discoveredModules) {
        // Module count reporting (no delay needed)
    }
}

