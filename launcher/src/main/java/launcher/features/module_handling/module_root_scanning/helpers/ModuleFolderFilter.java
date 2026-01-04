package launcher.features.module_handling.module_root_scanning.helpers;

import launcher.features.module_handling.module_root_scanning.ScanForModuleFolders;

import java.io.File;

/**
 * Internal filter for the main modules' directory.
 * 
 * <p>This filter is used when scanning the modules directory to skip
 * infrastructure directories (like "target") and hidden directories (any directory
 * starting with ".") that are not actual game modules.
 * 
 * <p>This class is internal to the module_root_scanning package. External code
 * should use {@link ScanForModuleFolders}
 * as the public API for directory management operations.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
final class ModuleFolderFilter {
    
    private ModuleFolderFilter() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if a directory should be skipped during module module_finding.
     * 
     * <p>Directories are skipped if they are:
     * <ul>
     *   <li>Infrastructure directories: "target"</li>
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
     * Checks if a directory name indicates it should be skipped during module module_finding.
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

