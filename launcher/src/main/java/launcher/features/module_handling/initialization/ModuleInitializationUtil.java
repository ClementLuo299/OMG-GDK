package launcher.features.module_handling.initialization;

import gdk.api.GameModule;
import gdk.internal.Logging;
import launcher.features.module_handling.discovery.ModuleDiscovery;
import launcher.features.module_handling.compilation.ModuleCompiler;
import launcher.features.file_paths.PathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for module initialization operations.
 * 
 * <p>This class provides static methods for discovering, loading, and checking
 * compilation status of game modules. These operations are separated from the
 * ViewModel to keep initialization logic separate from state management.
 * 
 * @author Clement Luo
 * @date January 1, 2026
 * @since Beta 1.0
 */
public final class ModuleInitializationUtil {
    
    private ModuleInitializationUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Discovers and loads all available game modules from the modules directory.
     * 
     * <p>This method scans the modules directory, validates module directories,
     * and compiles/loads them into GameModule instances.
     * 
     * @return List of discovered game modules, or empty list if error occurs
     */
    public static List<GameModule> discoverAndLoadModules() {
        try {
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            Logging.info("📂 Scanning for modules in: " + modulesDirectoryPath);
            
            File modulesDir = new File(modulesDirectoryPath);
            if (!modulesDir.exists()) {
                Logging.error("❌ Modules directory does not exist: " + modulesDirectoryPath);
                return new ArrayList<>();
            }
            
            List<File> validModuleDirectories = ModuleDiscovery.getValidModuleDirectories(modulesDir);
            List<GameModule> discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
            
            // Filter out null modules
            List<GameModule> validModules = new ArrayList<>();
            for (GameModule module : discoveredModules) {
                if (module != null) {
                    validModules.add(module);
                }
            }
            
            Logging.info("✅ Found " + validModules.size() + " game module(s)");
            return validModules;
            
        } catch (Exception moduleDiscoveryError) {
            Logging.error("❌ Error discovering modules: " + moduleDiscoveryError.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Checks for compilation failures in modules.
     * 
     * <p>This method checks both the ModuleCompiler's failure list and
     * scans the modules directory for modules that have source files but
     * no compiled classes.
     * 
     * @return List of module names that failed to compile
     */
    public static List<String> checkForCompilationFailures() {
        List<String> failures = new ArrayList<>();
        try {
            // Get compilation failures from ModuleCompiler
            List<String> compilerFailures = ModuleCompiler.getLastCompilationFailures();
            failures.addAll(compilerFailures);
            
            // Check for additional compilation issues
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            File modulesDir = new File(modulesDirectoryPath);
            File[] subdirs = modulesDir.listFiles(File::isDirectory);
            
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    if (subdir.getName().equals("target") || subdir.getName().equals(".git")) {
                        continue;
                    }
                    
                    File pomFile = new File(subdir, "pom.xml");
                    if (pomFile.exists()) {
                        File mainJava = new File(subdir, "src/main/java/Main.java");
                        File metadataJava = new File(subdir, "src/main/java/Metadata.java");
                        
                        if (mainJava.exists() && metadataJava.exists()) {
                            // Check if compiled classes exist
                            File targetClassesDir = new File(subdir, "target/classes");
                            if (!targetClassesDir.exists() || targetClassesDir.listFiles() == null || targetClassesDir.listFiles().length == 0) {
                                if (!failures.contains(subdir.getName())) {
                                    failures.add(subdir.getName());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error checking compilation failures: " + e.getMessage(), e);
        }
        return failures;
    }
}

