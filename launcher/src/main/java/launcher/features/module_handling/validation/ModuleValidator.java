package launcher.features.module_handling.validation;

import gdk.internal.Logging;
import launcher.features.module_handling.discovery.ModuleFolderFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utility class for validating module structure and compilation status.
 * 
 * <p>This class handles validation of:
 * <ul>
 *   <li>Module directory structure (required files and directories)</li>
 *   <li>Source file content validation (Main.java, Metadata.java)</li>
 *   <li>Compilation status checking</li>
 * </ul>
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ModuleValidator {
    
    private ModuleValidator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    // ==================== PUBLIC METHODS - STRUCTURE VALIDATION ====================
    
    /**
     * Checks if a directory has a valid module structure.
     * 
     * <p>Validity is determined by:
     * <ul>
     *   <li>Presence of required source files (Main.java, Metadata.java)</li>
     *   <li>Minimal API signatures in those files</li>
     * </ul>
     * 
     * @param moduleDir The module directory to validate
     * @return true if the directory has a valid module structure, false otherwise
     */
    public static boolean isValidModuleStructure(File moduleDir) {
        try {
            // Require top-level entry points (minimal contract for modules)
            File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
            File metadataJavaFile = new File(moduleDir, "src/main/java/Metadata.java");
            
            if (!mainJavaFile.exists() || !metadataJavaFile.exists()) {
                Logging.info("Module " + moduleDir.getName() + " missing required source files");
                return false;
            }
            
            // Minimal content checks for Main and Metadata
            if (!validateMainJavaFile(mainJavaFile)) {
                Logging.info("Module " + moduleDir.getName() + " missing required methods in Main.java");
                return false;
            }
            
            if (!validateMetadataJavaFile(metadataJavaFile)) {
                Logging.info("Module " + moduleDir.getName() + " missing required methods in Metadata.java");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            Logging.error("Error validating module " + moduleDir.getName() + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== PRIVATE METHODS - FILE VALIDATION ====================
    
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
    private static boolean validateMainJavaFile(File mainJavaFile) {
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
            
            boolean implementsGameModule = content.contains("implements GameModule");
            boolean hasClassMain = content.contains("class Main");
            return implementsGameModule && hasClassMain;
        } catch (IOException e) {
            Logging.error("Error reading Main.java file: " + e.getMessage(), e);
            return false;
        }
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
    private static boolean validateMetadataJavaFile(File metadataJavaFile) {
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
    
    // ==================== PUBLIC METHODS - COMPILATION STATUS ====================
    
    /**
     * Checks if a module needs to be compiled by checking for compiled classes.
     * 
     * <p>This method checks if:
     * <ul>
     *   <li>Compiled classes directory exists</li>
     *   <li>Main.class file exists</li>
     *   <li>Source files are newer than compiled classes</li>
     * </ul>
     * 
     * @param moduleDir The module directory to check
     * @return true if the module needs compilation, false otherwise
     */
    public static boolean moduleNeedsCompilation(File moduleDir) {
        try {
            File targetClassesDir = new File(moduleDir, "target/classes");
            File mainClassFile = new File(targetClassesDir, "Main.class");
            
            if (!targetClassesDir.exists()) {
                Logging.info("📁 Module " + moduleDir.getName() + " missing target/classes directory");
                return true;
            }
            
            if (!mainClassFile.exists()) {
                Logging.info("📄 Module " + moduleDir.getName() + " missing Main.class");
                return true;
            }
            
            // Check if source files are newer than compiled classes
            File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
            
            if (mainJavaFile.exists() && mainClassFile.exists()) {
                if (mainJavaFile.lastModified() > mainClassFile.lastModified()) {
                    Logging.info("📝 Module " + moduleDir.getName() + " source files are newer than compiled classes");
                    return true;
                }
            }
            
            Logging.info("✅ Module " + moduleDir.getName() + " is compiled and up to date");
            return false;
            
        } catch (Exception e) {
            Logging.error("💥 Error checking compilation status for " + moduleDir.getName() + ": " + e.getMessage());
            return true; // Assume compilation is needed if we can't check
        }
    }
    
    // ==================== PUBLIC METHODS - DIAGNOSTICS ====================
    
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
                if (ModuleFolderFilter.shouldSkip(subdir)) {
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
                    boolean isValid = isValidModuleStructure(subdir);
                    Logging.info("   ✅ Module structure validation result: " + isValid);
                    
                    if (isValid) {
                        Logging.info("   🎉 Module " + moduleName + " is VALID!");
                        
                        // Check compilation status
                        boolean needsCompilation = moduleNeedsCompilation(subdir);
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

