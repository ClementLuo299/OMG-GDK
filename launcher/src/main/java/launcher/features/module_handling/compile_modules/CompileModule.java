package launcher.features.module_handling.compile_modules;

import launcher.features.module_handling.compile_modules.helpers.MavenCompiler;

import java.io.File;

/**
 * Compiles modules when needed.
 * 
 * <p>This class provides a single method to compile a module.
 * All other compile_modules logic is delegated to helper classes.
 * 
 * <p>All other classes in this package are internal implementation details.
 * External code should only use this class for module compile_modules operations.
 * 
 * @author Clement Luo
 * @date January 4, 2026
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public final class CompileModule {
    
    private CompileModule() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Compiles a module using Maven.
     * 
     * <p>This method:
     * <ul>
     *   <li>Finds the Maven command in the system PATH</li>
     *   <li>Runs "mvn clean compile" in the module directory</li>
     *   <li>Returns true if compile_modules succeeded, false otherwise</li>
     * </ul>
     * 
     * @param moduleDir The module directory to compile
     * @return true if compile_modules was successful, false otherwise
     */
    public static boolean compile(File moduleDir) {
        String modulePath = moduleDir.getAbsolutePath();
        return MavenCompiler.compileModule(modulePath);
    }
}

