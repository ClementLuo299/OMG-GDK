package launcher.features.module_handling.compilation.helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.compilation.CheckCompilationNeeded;

import java.io.File;

/**
 * Helper class for reporting compilation status.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @since Beta 1.0
 */
public final class CompilationStatusReporter {
    
    private CompilationStatusReporter() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Reports compilation status for all modules in the directory.
     * 
     * <p>This method scans all modules and reports which ones need compilation.
     * Useful for debugging and user feedback.
     * 
     * @param modulesDirectory The modules directory to check
     */
    public static void report(File modulesDirectory) {
        try {
            Logging.info("=== MODULE COMPILATION STATUS ===");
            
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                Logging.info("No subdirectories found");
                return;
            }
            
            for (File subdir : subdirs) {
                // Skip infrastructure and hidden directories
                String dirName = subdir.getName();
                if (dirName.equals("target") || dirName.startsWith(".")) {
                    continue;
                }
                
                String moduleName = subdir.getName();
                
                Logging.info("Module: " + moduleName);
                boolean needsCompilation = CheckCompilationNeeded.needsCompilation(subdir);
                Logging.info("Compilation needed: " + needsCompilation);
                
                if (needsCompilation) {
                    Logging.info("Run 'mvn compile' in modules/" + moduleName + " to compile");
                }
            }
            
            Logging.info("=== END COMPILATION STATUS ===");
            
        } catch (Exception e) {
            Logging.error("Error reporting compilation status: " + e.getMessage(), e);
        }
    }
}

