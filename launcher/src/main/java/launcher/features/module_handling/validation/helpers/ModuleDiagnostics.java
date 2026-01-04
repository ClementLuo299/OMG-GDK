package launcher.features.module_handling.validation.helpers;

import gdk.internal.Logging;
import launcher.features.module_handling.validation.ModuleValidator;

import java.io.File;

/**
 * Helper class for module diagnostics.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleDiagnostics {
    
    private ModuleDiagnostics() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Comprehensive diagnostic method to check why no modules are being detected.
     * 
     * <p>This method performs extensive diagnostics including:
     * <ul>
     *   <li>Checking current working directory</li>
     *   <li>Verifying modules directory existence and accessibility</li>
     *   <li>Listing all contents</li>
     *   <li>Validating each potential module's structure</li>
     *   <li>Checking compilation status</li>
     * </ul>
     * 
     * <p>This will help identify file system, path, or validation issues.
     * 
     * @param modulesDirectoryPath The path to the modules directory
     */
    public static void diagnoseModuleDetectionIssues(String modulesDirectoryPath) {
        Logging.info("🔍 === MODULE DETECTION DIAGNOSTICS ===");
        
        try {
            // Check current working directory
            String currentWorkingDir = System.getProperty("user.dir");
            Logging.info("📁 Current working directory: " + currentWorkingDir);
            
            // Check the modules directory path
            Logging.info("🔍 Modules directory path: " + modulesDirectoryPath);
            
            // Try to resolve the path
            File modulesDirectory = new File(modulesDirectoryPath);
            Logging.info("📁 Resolved modules directory: " + modulesDirectory.getAbsolutePath());
            
            // Check if it exists
            if (!modulesDirectory.exists()) {
                Logging.error("❌ Modules directory does not exist!");
                
                // Try to find modules directory in common locations
                String[] commonPaths = {
                    "modules",
                    "../modules", 
                    "./modules",
                    "../../modules",
                    "target/modules"
                };
                
                Logging.info("🔍 Checking common module directory locations...");
                for (String path : commonPaths) {
                    File testPath = new File(path);
                    if (testPath.exists()) {
                        Logging.info("✅ Found potential modules directory: " + testPath.getAbsolutePath());
                        if (testPath.isDirectory()) {
                            Logging.info("✅ It's a directory");
                            File[] contents = testPath.listFiles();
                            if (contents != null) {
                                Logging.info("📁 Contains " + contents.length + " items");
                                for (File item : contents) {
                                    Logging.info("   - " + item.getName() + " (dir: " + item.isDirectory() + ")");
                                }
                            }
                        }
                    }
                }
                
                return;
            }
            
            // Check if it's a directory
            if (!modulesDirectory.isDirectory()) {
                Logging.error("❌ Path exists but is not a directory!");
                return;
            }
            
            // Check if it's readable
            if (!modulesDirectory.canRead()) {
                Logging.error("❌ Directory exists but is not readable!");
                return;
            }
            
            Logging.info("✅ Modules directory exists, is a directory, and is readable");
            
            // List all contents
            File[] allContents = modulesDirectory.listFiles();
            if (allContents == null) {
                Logging.error("❌ Cannot list directory contents (null returned)");
                return;
            }
            
            Logging.info("📁 Directory contains " + allContents.length + " items:");
            for (File item : allContents) {
                Logging.info("   - " + item.getName() + " (dir: " + item.isDirectory() + ", readable: " + item.canRead() + ")");
            }
            
            // Check for subdirectories that might be modules
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                Logging.error("❌ Cannot list subdirectories (null returned)");
                return;
            }
            
            Logging.info("📁 Found " + subdirs.length + " subdirectories:");
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                Logging.info("🔍 Checking subdirectory: " + moduleName);
                
                // Skip infrastructure and hidden directories
                String dirName = subdir.getName();
                if (dirName.equals("target") || dirName.startsWith(".")) {
                    Logging.info("⏭️ Skipping internal directory: " + moduleName);
                    continue;
                }
                
                // Check module structure
                Logging.info("✅ Validating module structure for: " + moduleName);
                
                // Check for required files
                File mainJavaFile = new File(subdir, "src/main/java/Main.java");
                File metadataJavaFile = new File(subdir, "src/main/java/Metadata.java");
                File targetClassesDir = new File(subdir, "target/classes");
                File mainClassFile = new File(targetClassesDir, "Main.class");
                
                Logging.info("   📄 Main.java exists: " + mainJavaFile.exists());
                Logging.info("   📄 Metadata.java exists: " + metadataJavaFile.exists());
                Logging.info("   📁 target/classes exists: " + targetClassesDir.exists());
                Logging.info("   📄 Main.class exists: " + mainClassFile.exists());
                
                // Try to validate the structure
                try {
                    boolean isValid = ModuleValidator.isValidModule(subdir);
                    Logging.info("   ✅ Module structure validation result: " + isValid);
                    
                    if (isValid) {
                        Logging.info("   🎉 Module " + moduleName + " is VALID!");
                        
                        // Check compilation status
                        boolean needsCompilation = ModuleCompilationChecker.needsCompilation(subdir);
                        Logging.info("   📦 Compilation needed: " + needsCompilation);
                        
                        if (needsCompilation) {
                            Logging.info("   💡 Run 'mvn compile' in modules/" + moduleName + " to compile");
                        }
                    } else {
                        Logging.info("   ❌ Module " + moduleName + " is INVALID");
                    }
                } catch (Exception e) {
                    Logging.error("   💥 Error validating module " + moduleName + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            Logging.error("💥 Error during diagnostics: " + e.getMessage(), e);
        }
        
        Logging.info("🔍 === END DIAGNOSTICS ===");
    }
}

