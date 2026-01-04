package launcher.features.module_handling.module_target_validation.helpers;

import gdk.internal.Logging;

import java.io.File;

/**
 * Helper class for checking if a module has compiled classes ready for loading.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class CheckForCompiledClasses {
    
    private CheckForCompiledClasses() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if a module has compiled classes ready for loading.
     * 
     * <p>This method verifies:
     * <ul>
     *   <li>The target/classes directory exists</li>
     *   <li>Main.class exists in target/classes</li>
     * </ul>
     * 
     * @param moduleDir The module directory to check
     * @return true if compiled classes are ready, false otherwise
     */
    public static boolean hasCompiledClasses(File moduleDir) {
        String moduleName = moduleDir.getName();
        
        // Check if compiled classes directory exists
        File targetClassesDir = new File(moduleDir, "target/classes");
        if (!targetClassesDir.exists()) {
            Logging.info("Module " + moduleName + " missing compiled classes - recompilation needed");
            return false;
        }
        
        // Verify Main.class exists
        File mainClassFile = new File(targetClassesDir, "Main.class");
        if (!mainClassFile.exists()) {
            Logging.info("Main.class missing in target for module: " + moduleName);
            return false;
        }
        
        return true;
    }
}

