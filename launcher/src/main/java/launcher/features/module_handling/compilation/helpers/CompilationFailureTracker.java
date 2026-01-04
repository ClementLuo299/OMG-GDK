package launcher.features.module_handling.compilation.helpers;

import gdk.internal.Logging;
import launcher.features.file_paths.PathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for tracking compilation failures.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @since Beta 1.0
 */
public final class CompilationFailureTracker {
    
    /** Tracks compilation failures for UI notification. */
    private static final List<String> lastCompilationFailures = new ArrayList<>();
    
    private CompilationFailureTracker() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Stores compilation failures for UI notification.
     * 
     * <p>This method replaces the current list of compilation failures with
     * the provided list. Used to track which modules failed to compile.
     * 
     * @param failures List of module names that failed compilation
     */
    public static void store(List<String> failures) {
        lastCompilationFailures.clear();
        lastCompilationFailures.addAll(failures);
        Logging.info("💾 Stored " + failures.size() + " compilation failures: " + String.join(", ", failures));
    }
    
    /**
     * Gets the last compilation failures for UI notification.
     * 
     * <p>This method returns a copy of the stored compilation failures list.
     * 
     * @return List of module names that failed compilation
     */
    public static List<String> getLastFailures() {
        Logging.info("📤 Retrieved " + lastCompilationFailures.size() + " compilation failures: " + String.join(", ", lastCompilationFailures));
        return new ArrayList<>(lastCompilationFailures);
    }
    
    /**
     * Clears the stored compilation failures.
     * 
     * <p>This method clears all stored compilation failure information.
     */
    public static void clear() {
        lastCompilationFailures.clear();
    }
    
    /**
     * Adds a compilation failure for a specific module.
     * 
     * <p>This method adds a module name to the compilation failures list
     * if it's not already present.
     * 
     * @param moduleName The name of the module that failed compilation
     */
    public static void addFailure(String moduleName) {
        if (!lastCompilationFailures.contains(moduleName)) {
            lastCompilationFailures.add(moduleName);
            Logging.info("💾 Added compilation failure: " + moduleName);
        }
    }
    
    /**
     * Checks for compilation failures in modules.
     * 
     * <p>This method checks both the stored failure list and
     * scans the modules directory for modules that have source files but
     * no compiled classes.
     * 
     * @return List of module names that failed to compile
     */
    public static List<String> checkForFailures() {
        List<String> failures = new ArrayList<>();
        try {
            // Get compilation failures from tracker
            List<String> trackedFailures = getLastFailures();
            failures.addAll(trackedFailures);
            
            // Check for additional compilation issues
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            File modulesDir = new File(modulesDirectoryPath);
            File[] subdirs = modulesDir.listFiles(File::isDirectory);
            
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    String dirName = subdir.getName();
                    if (dirName.equals("target") || dirName.startsWith(".")) {
                        continue;
                    }
                    
                    File pomFile = new File(subdir, "pom.xml");
                    if (pomFile.exists()) {
                        File mainJava = new File(subdir, "src/main/java/Main.java");
                        File metadataJava = new File(subdir, "src/main/java/Metadata.java");
                        
                        if (mainJava.exists() && metadataJava.exists()) {
                            // Check if compiled classes exist
                            File targetClassesDir = new File(subdir, "target/classes");
                            if (!targetClassesDir.exists() || targetClassesDir.listFiles() == null || targetClassesDir.listFiles().length == 0) {
                                if (!failures.contains(subdir.getName())) {
                                    failures.add(subdir.getName());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error checking compilation failures: " + e.getMessage(), e);
        }
        return failures;
    }
}

