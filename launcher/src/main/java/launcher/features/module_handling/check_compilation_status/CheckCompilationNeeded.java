package launcher.features.module_handling.check_compilation_status;

import gdk.internal.Logging;

import java.io.File;

/**
 * Checks if a module needs to be compiled.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class CheckCompilationNeeded {
    
    private CheckCompilationNeeded() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if a module needs to be compiled by checking for compiled classes.
     * 
     * <p>This method checks if:
     * <ul>
     *   <li>Compiled classes directory exists</li>
     *   <li>Main.class file exists</li>
     *   <li>Source files are newer than compiled classes</li>
     * </ul>
     * 
     * @param moduleDir The module directory to check
     * @return true if the module needs compile_modules, false otherwise
     */
    public static boolean needsCompilation(File moduleDir) {
        try {
            File targetClassesDir = new File(moduleDir, "target/classes");
            File mainClassFile = new File(targetClassesDir, "Main.class");
            
            if (!targetClassesDir.exists()) {
                Logging.info("Module " + moduleDir.getName() + " missing target/classes directory");
                return true;
            }
            
            if (!mainClassFile.exists()) {
                Logging.info("Module " + moduleDir.getName() + " missing Main.class");
                return true;
            }
            
            // Check if source files are newer than compiled classes
            File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
            
            if (mainJavaFile.exists() && mainClassFile.exists()) {
                if (mainJavaFile.lastModified() > mainClassFile.lastModified()) {
                    Logging.info("Module " + moduleDir.getName() + " source files are newer than compiled classes");
                    return true;
                }
            }
            
            Logging.info("Module " + moduleDir.getName() + " is compiled and up to date");
            return false;
            
        } catch (Exception e) {
            Logging.error("Error checking compile_modules status for " + moduleDir.getName() + ": " + e.getMessage());
            return true; // Assume compile_modules is needed if we can't check
        }
    }
}

