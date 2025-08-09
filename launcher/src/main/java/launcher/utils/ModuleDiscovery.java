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
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return validModules; // empty if nothing to scan
            }
            
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                
                // Filter out infra/hidden directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    continue;
                }
                
                // Collect only those passing structural checks
                if (isValidModuleStructure(subdir)) {
                    validModules.add(subdir);
                }
            }
        } catch (Exception e) {
            Logging.error("Error getting valid module directories: " + e.getMessage(), e);
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
            String content = Files.readString(mainJavaFile.toPath());
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
            String content = Files.readString(metadataJavaFile.toPath());
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
} 