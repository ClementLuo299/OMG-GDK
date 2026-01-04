package launcher.features.module_handling.compilation;

import launcher.features.module_handling.compilation.helpers.CompilationFailureTracker;

import java.util.List;

/**
 * Public API for checking module compilation failures.
 * 
 * <p>This class provides methods for checking compilation failures in modules.
 * All implementation details are delegated to helper classes.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class CompilationFailures {
    
    private CompilationFailures() {
        throw new AssertionError("Utility class should not be instantiated");
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
    public static List<String> check() {
        return CompilationFailureTracker.checkForFailures();
    }
}

