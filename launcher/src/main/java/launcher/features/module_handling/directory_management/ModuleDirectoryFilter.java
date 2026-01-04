package launcher.features.module_handling.directory_management;

import java.io.File;

/**
 * Internal utility class for filtering module directories.
 * 
 * <p>This class determines which directories should be skipped during module discovery.
 * It filters out infrastructure directories (like "target", ".git") and hidden directories
 * that are not actual game modules.
 * 
 * <p>This class is package-private. External code should use {@link ModuleDirectoryValidator}
 * as the public API for directory management operations.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
final class ModuleDirectoryFilter {
    
    private ModuleDirectoryFilter() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if a directory should be skipped during module discovery.
     * 
     * <p>Directories are skipped if they are:
     * <ul>
     *   <li>Infrastructure directories: "target", ".git"</li>
     *   <li>Hidden directories: any directory starting with "."</li>
     * </ul>
     * 
     * @param directory The directory to check
     * @return true if the directory should be skipped, false otherwise
     */
    public static boolean shouldSkip(File directory) {
        String directoryName = directory.getName();
        return shouldSkip(directoryName);
    }
    
    /**
     * Checks if a directory name indicates it should be skipped during module discovery.
     * 
     * <p>Directories are skipped if they are:
     * <ul>
     *   <li>Infrastructure directories: "target", ".git"</li>
     *   <li>Hidden directories: any directory starting with "."</li>
     * </ul>
     * 
     * @param directoryName The name of the directory to check
     * @return true if the directory should be skipped, false otherwise
     */
    public static boolean shouldSkip(String directoryName) {
        return directoryName.equals("target") 
            || directoryName.equals(".git") 
            || directoryName.startsWith(".");
    }
}

