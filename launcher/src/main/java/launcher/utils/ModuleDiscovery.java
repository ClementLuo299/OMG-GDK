package launcher.utils;

import gdk.Logging;
import gdk.GameModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles module discovery and validation.
 * This class is responsible for finding modules in the modules directory
 * and validating their structure and required components.
 * 
 * @authors Clement Luo
 * @date August 12, 2025
 * @edited August 12, 2025
 * @since 1.0
 */
public class ModuleDiscovery {
    
    /**
     * Discover all valid modules in the modules directory.
     * Currently only identifies candidates and logs validation; actual loading
     * is delegated elsewhere (kept intentionally side-effect free here).
     */
    public static List<GameModule> discoverModules(String modulesDirectoryPath) {
        Logging.info("Starting module discovery in: " + modulesDirectoryPath);
        
        List<GameModule> discoveredModules = new ArrayList<>();
        File modulesDirectory = new File(modulesDirectoryPath);
        
        if (!modulesDirectory.exists() || !modulesDirectory.isDirectory()) {
            Logging.info("Modules directory does not exist: " + modulesDirectoryPath);
            return discoveredModules; // return empty by design
        }
        
        File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
        if (subdirs == null) {
            Logging.info("No subdirectories found in modules directory");
            return discoveredModules;
        }
        
        Logging.info("Found " + subdirs.length + " subdirectories in modules directory");
        
        for (File subdir : subdirs) {
            String moduleName = subdir.getName();
            Logging.info("Checking subdirectory: " + moduleName);
            
            // Only log validity here; loading is performed by ModuleCompiler
            if (isValidModuleStructure(subdir)) {
                Logging.info("Valid module structure found: " + moduleName);
            } else {
                Logging.info("Invalid module structure: " + moduleName);
            }
        }
        
        Logging.info("Total modules discovered: " + discoveredModules.size());
        return discoveredModules;
    }
    
    /**
     * Count the number of valid modules in the modules directory.
     */
    public static int countValidModules(File modulesDirectory) {
        int validCount = 0;
        
        try {
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return 0; // nothing to scan
            }
            
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                
                // Skip infra/hidden directories that are not game modules
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    continue;
                }
                
                // Only structural validity is checked here (not compilation)
                if (isValidModuleStructure(subdir)) {
                    validCount++;
                    Logging.info("Valid module found: " + moduleName);
                } else {
                    Logging.info("Invalid module found: " + moduleName);
                }
            }
        } catch (Exception e) {
            Logging.error("Error counting valid modules: " + e.getMessage(), e);
        }
        
        return validCount;
    }
    
    /**
     * Get list of valid module directories for processing.
     */
    public static List<File> getValidModuleDirectories(File modulesDirectory) {
        List<File> validModules = new ArrayList<>();
        
        try {
            Logging.info("🔍 Starting module discovery in: " + modulesDirectory.getAbsolutePath());
            
            // Add timeout protection for file operations
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // Reduced to 5 second timeout for faster failure detection
            
            // First, check if we can even list the directory
            Logging.info("🔍 Attempting to list directory contents...");
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            
            if (System.currentTimeMillis() - startTime > timeout) {
                Logging.warning("⚠️ Directory listing timeout reached");
                return validModules;
            }
            
            if (subdirs == null) {
                Logging.info("📁 No subdirectories found in modules directory");
                return validModules; // empty if nothing to scan
            }
            
            Logging.info("📁 Found " + subdirs.length + " subdirectories to check");
            
            for (File subdir : subdirs) {
                // Check timeout
                if (System.currentTimeMillis() - startTime > timeout) {
                    Logging.warning("⚠️ Module discovery timeout reached, stopping discovery");
                    break;
                }
                
                String moduleName = subdir.getName();
                Logging.info("🔍 Checking module: " + moduleName);
                
                // Filter out infra/hidden directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    Logging.info("⏭️ Skipping infrastructure directory: " + moduleName);
                    continue;
                }
                
                try {
                    Logging.info("✅ Validating module structure for: " + moduleName);
                    // Collect only those passing structural checks
                    if (isValidModuleStructure(subdir)) {
                        validModules.add(subdir);
                        Logging.info("✅ Valid module found: " + moduleName);
                    } else {
                        Logging.info("❌ Invalid module structure: " + moduleName);
                    }
                } catch (Exception e) {
                    Logging.error("💥 Error validating module " + moduleName + ": " + e.getMessage());
                    // Continue with other modules instead of failing completely
                }
            }
            
            Logging.info("🏁 Module discovery completed. Found " + validModules.size() + " valid modules");
            
        } catch (Exception e) {
            Logging.error("💥 Error getting valid module directories: " + e.getMessage(), e);
        }
        
        return validModules;
    }
    
    /**
     * Check if a directory has a valid module structure.
     * Validity = presence of required files + minimal API signatures.
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
    
    /**
     * Validate that Main.java contains required methods.
     * Currently ensures a runnable entrypoint via a main method signature.
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
     * Validate that Metadata.java exposes minimal game metadata contract.
     * Accepts instance or static getters for name, version, description.
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
    
    /**
     * Check if a module directory exists and is accessible.
     */
    public static boolean moduleDirectoryExists(String modulePath) {
        File moduleDir = new File(modulePath);
        return moduleDir.exists() && moduleDir.isDirectory() && moduleDir.canRead();
    }
    
    /**
     * Calculate the total number of steps needed for startup progress tracking.
     * Base steps + one per valid module; clamped for UX.
     */
    public static int calculateTotalSteps() {
        Logging.info("Calculating total steps based on module verification...");
        
        try {
            // Base steps: starting, UI loading, preparation, finalization, ready
            int baseSteps = 5;
            
            // Relative to launcher module root
            String modulesDirectoryPath = "../modules";
            File modulesDirectory = new File(modulesDirectoryPath);
            
            if (!modulesDirectory.exists()) {
                Logging.info("Modules directory not found, using base steps only");
                return baseSteps;
            }
            
            int validModuleCount = countValidModules(modulesDirectory);
            Logging.info("Found " + validModuleCount + " valid modules");
            
            int totalSteps = baseSteps + validModuleCount;
            // Maintain a reasonable range for the progress bar
            totalSteps = Math.max(5, Math.min(50, totalSteps));
            return totalSteps;
        } catch (Exception e) {
            Logging.error("Error calculating total steps: " + e.getMessage(), e);
            return 10; // safe fallback
        }
    }
    
    /**
     * Get the list of all module directories (valid or invalid).
     * Useful for diagnostics or bulk operations before validation.
     */
    public static List<File> getAllModuleDirectories(File modulesDirectory) {
        List<File> allModules = new ArrayList<>();
        
        try {
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return allModules;
            }
            
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                
                // Skip infra/hidden directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    continue;
                }
                
                allModules.add(subdir);
            }
        } catch (Exception e) {
            Logging.error("Error getting all module directories: " + e.getMessage(), e);
        }
        
        return allModules;
    }

    /**
     * Quick test to check if modules directory is accessible.
     * This can help identify if the issue is with file system access.
     */
    public static boolean testModulesDirectoryAccess(String modulesDirectoryPath) {
        try {
            Logging.info("🧪 Testing modules directory access: " + modulesDirectoryPath);
            
            File modulesDirectory = new File(modulesDirectoryPath);
            
            if (!modulesDirectory.exists()) {
                Logging.warning("❌ Modules directory does not exist: " + modulesDirectoryPath);
                return false;
            }
            
            if (!modulesDirectory.isDirectory()) {
                Logging.warning("❌ Path exists but is not a directory: " + modulesDirectoryPath);
                return false;
            }
            
            if (!modulesDirectory.canRead()) {
                Logging.warning("❌ Modules directory is not readable: " + modulesDirectoryPath);
                return false;
            }
            
            Logging.info("✅ Modules directory is accessible and readable");
            
            // Try to list contents
            long startTime = System.currentTimeMillis();
            File[] contents = modulesDirectory.listFiles();
            long listTime = System.currentTimeMillis() - startTime;
            
            if (contents == null) {
                Logging.warning("❌ Cannot list directory contents (null returned)");
                return false;
            }
            
            Logging.info("✅ Directory listing successful in " + listTime + "ms. Found " + contents.length + " items");
            
            // List first few items for debugging
            for (int i = 0; i < Math.min(5, contents.length); i++) {
                File item = contents[i];
                Logging.info("📁 Item " + i + ": " + item.getName() + " (dir: " + item.isDirectory() + ")");
            }
            
            return true;
            
        } catch (Exception e) {
            Logging.error("💥 Error testing modules directory access: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Comprehensive diagnostic method to check why no modules are being detected.
     * This will help identify file system, path, or validation issues.
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
                
                // Skip infrastructure directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    Logging.info("⏭️ Skipping infrastructure directory: " + moduleName);
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

    /**
     * Check if a module needs to be compiled by checking for compiled classes.
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
            File metadataJavaFile = new File(moduleDir, "src/main/java/Metadata.java");
            
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
    
    /**
     * Get compilation status for all modules in the directory.
     */
    public static void reportModuleCompilationStatus(File modulesDirectory) {
        try {
            Logging.info("🔍 === MODULE COMPILATION STATUS ===");
            
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                Logging.info("📁 No subdirectories found");
                return;
            }
            
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                
                // Skip infrastructure directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    continue;
                }
                
                Logging.info("🔍 Module: " + moduleName);
                boolean needsCompilation = moduleNeedsCompilation(subdir);
                Logging.info("   📦 Compilation needed: " + needsCompilation);
                
                if (needsCompilation) {
                    Logging.info("   💡 Run 'mvn compile' in modules/" + moduleName + " to compile");
                }
            }
            
            Logging.info("🔍 === END COMPILATION STATUS ===");
            
        } catch (Exception e) {
            Logging.error("💥 Error reporting compilation status: " + e.getMessage(), e);
        }
    }
} 