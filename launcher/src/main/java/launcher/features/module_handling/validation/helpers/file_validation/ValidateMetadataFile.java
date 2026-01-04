package launcher.features.module_handling.validation.helpers.file_validation;

import gdk.internal.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Helper class for validating Metadata.java file content.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ValidateMetadataFile {
    
    private ValidateMetadataFile() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Validates that Metadata.java exposes minimal game metadata contract.
     * 
     * <p>This method checks that Metadata.java:
     * <ul>
     *   <li>Extends GameMetadata</li>
     *   <li>Contains getGameName() method</li>
     *   <li>Contains getGameVersion() method</li>
     *   <li>Contains getGameDescription() method</li>
     * </ul>
     * 
     * <p>Includes timeout protection for file reading operations.
     * 
     * @param metadataJavaFile The Metadata.java file to validate
     * @return true if the file contains required methods, false otherwise
     */
    public static boolean isValid(File metadataJavaFile) {
        try {
            // Add timeout protection for file reading
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // 5 second timeout for file operations
            
            String content = Files.readString(metadataJavaFile.toPath());
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("File reading timeout for Metadata.java");
                return false;
            }

            // Check code content
            boolean extendsGameMetadata = content.contains("extends GameMetadata");
            boolean hasGetGameName = content.contains("getGameName()");
            boolean hasGetGameVersion = content.contains("getGameVersion()");
            boolean hasGetGameDescription = content.contains("getGameDescription()");
            return extendsGameMetadata && hasGetGameName && hasGetGameVersion && hasGetGameDescription;
        } catch (IOException e) {
            Logging.error("Error reading Metadata.java file: " + e.getMessage(), e);
            return false;
        }
    }
}

