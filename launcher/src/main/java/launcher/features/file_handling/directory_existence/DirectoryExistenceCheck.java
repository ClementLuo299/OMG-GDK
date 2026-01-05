package launcher.features.file_handling.directory_existence;

import java.io.File;

/**
 * Validates basic directory properties.
 * 
 * <p>This class provides methods for checking if a directory exists,
 * is actually a directory type, and is readable. It performs lightweight
 * validation without attempting to list directory contents.
 * 
 * @author Clement Luo
 * @date January 4, 2026
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public final class DirectoryExistenceCheck {
    
    private DirectoryExistenceCheck() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Checks if a directory exists and is accessible.
     * 
     * <p>This is a lightweight check that verifies the directory exists,
     * is actually a directory, and is readable. It does not attempt to list
     * the directory contents.
     * 
     * @param directoryPath The path to the directory to check
     * @return true if the directory exists, is a directory, and is readable
     */
    public static boolean exists(String directoryPath) {
        File directory = new File(directoryPath);
        return directory.exists() && directory.isDirectory() && directory.canRead();
    }
    
    /**
     * Checks if a directory exists.
     * 
     * @param directory The directory to check
     * @return true if the directory exists, false otherwise
     */
    public static boolean exists(File directory) {
        return directory.exists();
    }
    
    /**
     * Checks if a path is a directory.
     * 
     * @param directory The path to check
     * @return true if the path is a directory, false otherwise
     */
    public static boolean isDirectory(File directory) {
        return directory.isDirectory();
    }
    
    /**
     * Checks if a directory is readable.
     * 
     * @param directory The directory to check
     * @return true if the directory is readable, false otherwise
     */
    public static boolean isReadable(File directory) {
        return directory.canRead();
    }
    
    /**
     * Validates all basic directory properties.
     * 
     * @param directory The directory to validate
     * @return true if the directory exists, is a directory, and is readable
     */
    public static boolean validate(File directory) {
        return exists(directory) && isDirectory(directory) && isReadable(directory);
    }
}

