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
 * @author Clement Luo
 * @date August 8, 2025
 * @edited August 8, 2025
 * @since 1.0
 */
public class ModuleDiscovery {
    
    /**
     * Discover all valid modules in the modules directory.
     * 
     * @param modulesDirectoryPath The path to the modules directory
     * @return List of discovered GameModule instances
     */
    public static List<GameModule> discoverModules(String modulesDirectoryPath) {
        Logging.info("Starting module discovery in: " + modulesDirectoryPath);
        
        List<GameModule> discoveredModules = new ArrayList<>();
        File modulesDirectory = new File(modulesDirectoryPath);
        
        if (!modulesDirectory.exists() || !modulesDirectory.isDirectory()) {
            Logging.info("Modules directory does not exist: " + modulesDirectoryPath);
            return discoveredModules;
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
            
            if (isValidModuleStructure(subdir)) {
                Logging.info("Valid module structure found: " + moduleName);
                // Note: Actual module loading is handled by ModuleLoader
            } else {
                Logging.info("Invalid module structure: " + moduleName);
            }
        }
        
        Logging.info("Total modules discovered: " + discoveredModules.size());
        return discoveredModules;
    }
    
    /**
     * Count the number of valid modules in the modules directory.
     * 
     * @param modulesDirectory The modules directory to scan
     * @return The number of valid modules found
     */
    public static int countValidModules(File modulesDirectory) {
        int validCount = 0;
        
        try {
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return 0;
            }
            
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                
                // Skip non-module directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    continue;
                }
                
                // Check if this is a valid module
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
     * 
     * @param modulesDirectory The modules directory to scan
     * @return List of valid module directories
     */
    public static List<File> getValidModuleDirectories(File modulesDirectory) {
        List<File> validModules = new ArrayList<>();
        
        try {
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return validModules;
            }
            
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                
                // Skip non-module directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    continue;
                }
                
                // Check if this is a valid module
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
     * 
     * @param moduleDir The module directory to validate
     * @return true if the module has valid structure, false otherwise
     */
    public static boolean isValidModuleStructure(File moduleDir) {
        try {
            // Check for required source files
            File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
            File metadataJavaFile = new File(moduleDir, "src/main/java/Metadata.java");
            
            if (!mainJavaFile.exists() || !metadataJavaFile.exists()) {
                Logging.info("Module " + moduleDir.getName() + " missing required source files");
                return false;
            }
            
            // Validate Main.java content
            if (!validateMainJavaFile(mainJavaFile)) {
                Logging.info("Module " + moduleDir.getName() + " missing required methods in Main.java");
                return false;
            }
            
            // Validate Metadata.java content
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
     * 
     * @param mainJavaFile The Main.java file to validate
     * @return true if the file contains required methods, false otherwise
     */
    private static boolean validateMainJavaFile(File mainJavaFile) {
        try {
            String content = Files.readString(mainJavaFile.toPath());
            
            // Check for required methods
            boolean hasMainMethod = content.contains("public static void main(String[] args)") ||
                                  content.contains("public static void main(String args[])");
            
            return hasMainMethod;
        } catch (IOException e) {
            Logging.error("Error reading Main.java file: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Validate that Metadata.java contains required methods.
     * 
     * @param metadataJavaFile The Metadata.java file to validate
     * @return true if the file contains required methods, false otherwise
     */
    private static boolean validateMetadataJavaFile(File metadataJavaFile) {
        try {
            String content = Files.readString(metadataJavaFile.toPath());
            
            // Check for required methods
            boolean hasGetGameName = content.contains("public String getGameName()") || 
                                   content.contains("public static String getGameName()");
            boolean hasGetVersion = content.contains("public String getVersion()") || 
                                  content.contains("public static String getVersion()");
            boolean hasGetDescription = content.contains("public String getDescription()") || 
                                      content.contains("public static String getDescription()");
            
            return hasGetGameName && hasGetVersion && hasGetDescription;
        } catch (IOException e) {
            Logging.error("Error reading Metadata.java file: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if a module directory exists and is accessible.
     * 
     * @param modulePath The path to the module directory
     * @return true if the module directory exists and is accessible, false otherwise
     */
    public static boolean moduleDirectoryExists(String modulePath) {
        File moduleDir = new File(modulePath);
        return moduleDir.exists() && moduleDir.isDirectory() && moduleDir.canRead();
    }
    
    /**
     * Calculate the total number of steps needed for startup progress tracking.
     * 
     * This method determines the number of progress bar steps based on:
     * - Base steps for UI initialization and preparation
     * - Number of valid modules found (each module adds 1 step)
     * 
     * @return The total number of steps for progress tracking
     */
    public static int calculateTotalSteps() {
        Logging.info("Calculating total steps based on module verification...");
        
        try {
            // Base steps: UI initialization, preparation, finalization
            int baseSteps = 5; // Starting, UI loading, preparation, finalization, ready
            
            // Check if modules directory exists
            String modulesDirectoryPath = "../modules"; // Relative to launcher
            File modulesDirectory = new File(modulesDirectoryPath);
            
            if (!modulesDirectory.exists()) {
                Logging.info("Modules directory not found, using base steps only");
                return baseSteps;
            }
            
            // Count valid modules
            int validModuleCount = countValidModules(modulesDirectory);
            Logging.info("Found " + validModuleCount + " valid modules");
            
            // Each valid module adds 1 step for processing
            int totalSteps = baseSteps + validModuleCount;
            
            // Ensure minimum of 5 steps and maximum of 50 steps
            totalSteps = Math.max(5, Math.min(50, totalSteps));
            
            return totalSteps;
        } catch (Exception e) {
            Logging.error("Error calculating total steps: " + e.getMessage(), e);
            return 10; // Default fallback
        }
    }
    
    /**
     * Get the list of all module directories (valid or invalid).
     * 
     * @param modulesDirectory The modules directory to scan
     * @return List of all module directories
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
                
                // Skip non-module directories
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