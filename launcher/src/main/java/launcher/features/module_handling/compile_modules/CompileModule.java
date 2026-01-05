package launcher.features.module_handling.compile_modules;

import launcher.features.module_handling.compile_modules.compilers.MavenModuleCompiler;

import java.io.File;

/**
 * Compiles modules when needed.
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
     * Compiles a module given its folder.
     * 
     * @param moduleDir The module directory to compile
     * @return true if compile_modules was successful, false otherwise
     */
    public static boolean compile(File moduleDir) {
        String modulePath = moduleDir.getAbsolutePath();
        return MavenModuleCompiler.compile(modulePath);
    }
}

