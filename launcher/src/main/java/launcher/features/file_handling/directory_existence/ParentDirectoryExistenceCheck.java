package launcher.features.file_handling.directory_existence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility for ensuring parent directories exist for file operations.
 * 
 * @author Clement Luo
 * @date January 5, 2026
 * @edited January 5, 2026
 * @since Beta 1.0
 */
public final class ParentDirectoryExistenceCheck {
    
    private ParentDirectoryExistenceCheck() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Ensures the parent directory of the given file path exists.
     * Creates the directory structure if it doesn't exist.
     * 
     * @param file The file path whose parent directory should exist
     * @throws IOException If the directory cannot be created
     */
    public static void exists(Path file) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
    }
}

