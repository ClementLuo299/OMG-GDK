package launcher.features.module_handling.compilation.helpers;

import gdk.api.GameModule;
import gdk.internal.Logging;

/**
 * Helper class for validating loaded classes.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @since Beta 1.0
 */
public final class ClassValidator {
    
    private ClassValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Validates that a Main class implements the GameModule interface.
     * 
     * @param mainClass The Main class to validate
     * @return true if the class implements GameModule, false otherwise
     */
    public static boolean isValidMainClass(Class<?> mainClass) {
        try {
            boolean implementsGameModule = GameModule.class.isAssignableFrom(mainClass);
            if (!implementsGameModule) {
                Logging.info("Main class does not implement GameModule interface");
            }
            return implementsGameModule;
        } catch (Exception e) {
            Logging.error("Error validating main class: " + e.getMessage(), e);
            return false;
        }
    }
}

