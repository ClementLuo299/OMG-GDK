package launcher.features.module_handling.module_code_validation.helpers.file_validation;

import gdk.internal.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Helper class for validating Main.java file content.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ValidateMainFile {
    
    private ValidateMainFile() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Validates that Main.java contains required methods.
     * 
     * <p>This method checks that Main.java:
     * <ul>
     *   <li>Implements the GameModule interface</li>
     *   <li>Contains a class named "Main"</li>
     * </ul>
     * 
     * <p>Includes timeout protection for file reading operations.
     * 
     * @param mainJavaFile The Main.java file to validate
     * @return true if the file contains required methods, false otherwise
     */
    public static boolean isValid(File mainJavaFile) {
        try {
            // Add timeout protection for file reading
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // 5 second timeout for file operations
            
            String content = Files.readString(mainJavaFile.toPath());
            
            // Check timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("File reading timeout for Main.java");
                return false;
            }

            // Check code content
            boolean implementsGameModule = content.contains("implements GameModule");
            boolean hasClassMain = content.contains("class Main");
            return implementsGameModule && hasClassMain;
        } catch (IOException e) {
            Logging.error("Error reading Main.java file: " + e.getMessage(), e);
            return false;
        }
    }
}

