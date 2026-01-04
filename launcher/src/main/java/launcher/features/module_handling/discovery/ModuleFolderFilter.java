package launcher.features.module_handling.discovery;

import java.io.File;

/**
 * Filter for module folders when scanning within the modules directory.
 * 
 * <p>This class determines which directories should be skipped when looking
 * for module folders. It filters out infrastructure directories (like "target")
 * and hidden directories (any directory starting with ".") that are not actual game modules.
 * 
 * <p>This is used when scanning the main modules directory to find individual
 * module folders.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleFolderFilter {
    
    private ModuleFolderFilter() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if a directory should be skipped when looking for module folders.
     * 
     * <p>Files (non-directories) are always skipped. Directories are skipped if they are:
     * <ul>
     *   <li>Infrastructure directories: "target"</li>
     *   <li>Hidden directories: any directory starting with "."</li>
     * </ul>
     * 
     * @param directory The file or directory to check
     * @return true if the file/directory should be skipped, false otherwise
     */
    public static boolean shouldSkip(File directory) {
        // Only allow directories - skip all files
        if (!directory.isDirectory()) {
            return true;
        }
        
        String directoryName = directory.getName();
        return shouldSkip(directoryName);
    }
    
    /**
     * Checks if a directory name indicates it should be skipped.
     * 
     * <p>Directories are skipped if they are:
     * <ul>
     *   <li>Infrastructure directories: "target"</li>
     *   <li>Hidden directories: any directory starting with "."</li>
     * </ul>
     * 
     * @param directoryName The name of the directory to check
     * @return true if the directory should be skipped, false otherwise
     */
    public static boolean shouldSkip(String directoryName) {
        return directoryName.equals("target") 
            || directoryName.startsWith(".");
    }
}

