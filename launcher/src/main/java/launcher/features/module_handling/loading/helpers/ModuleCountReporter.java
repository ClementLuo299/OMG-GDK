package launcher.features.module_handling.loading.helpers;

import launcher.features.development_features.StartupDelayUtil;

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
        StartupDelayUtil.addDevelopmentDelay("After reporting module count");
    }
}

