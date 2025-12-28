package launcher.gui.lobby.managers;

import gdk.internal.Logging;
import launcher.utils.module.ModuleCompiler;
import launcher.utils.path.PathUtil;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles module compilation checking and validation.
 * 
 * Provides functionality to check for compilation failures, broken modules,
 * and attempts to compile modules when needed.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class ModuleCompilationChecker {
    
    /**
     * Interface for reporting messages to the UI.
     */
    public interface MessageReporter {
        void addMessage(String message);
    }
    
    private final MessageReporter messageReporter;
    
    /**
     * Create a new ModuleCompilationChecker.
     * 
     * @param messageReporter Callback to report messages to the UI
     */
    public ModuleCompilationChecker(MessageReporter messageReporter) {
        this.messageReporter = messageReporter;
    }
    
    /**
     * Check for compilation failures on startup.
     */
    public void checkStartupCompilationFailures() {
        try {
            Logging.info("🚀 Checking for compilation failures on startup...");
            
            // Get the modules directory path
            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            
            // Check for compilation failures detected by ModuleCompiler
            checkModuleCompilerCompilationFailures();
            
            // Also check for any existing compilation issues
            checkForCompilationFailures(modulesDirectoryPath);
            
            // Force a compilation check for all modules to detect issues
            forceCompilationCheck(modulesDirectoryPath);
            
            Logging.info("✅ Startup compilation failure check completed");
            
        } catch (Exception e) {
            Logging.error("❌ Error during startup compilation failure check: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check for compilation failures detected by ModuleCompiler.
     */
    private void checkModuleCompilerCompilationFailures() {
        try {
            Logging.info("🔍 Starting check for ModuleCompiler compilation failures...");
            
            // Get compilation failures from ModuleCompiler
            List<String> compilationFailures = ModuleCompiler.getLastCompilationFailures();
            
            Logging.info("📊 Found " + compilationFailures.size() + " compilation failures to report");
            
            if (!compilationFailures.isEmpty()) {
                // Use Platform.runLater to ensure UI updates happen on the JavaFX thread
                Platform.runLater(() -> {
                    Logging.info("🎯 Adding compilation failure messages to UI...");
                    for (String moduleName : compilationFailures) {
                        String message = "⚠️ Module '" + moduleName + "' failed to compile - check source code for errors";
                        messageReporter.addMessage(message);
                        Logging.info("📝 Added message: " + message);
                    }
                    String summaryMessage = "📋 Compilation failures detected in: " + String.join(", ", compilationFailures);
                    messageReporter.addMessage(summaryMessage);
                    Logging.info("📝 Added summary message: " + summaryMessage);
                });
                
                Logging.info("📢 Queued compilation failure notifications for UI: " + String.join(", ", compilationFailures));
            } else {
                Logging.info("✅ No compilation failures to report");
            }
            
            // Clear the stored failures after reporting them
            ModuleCompiler.clearCompilationFailures();
            
        } catch (Exception e) {
            Logging.error("❌ Error checking ModuleCompiler compilation failures: " + e.getMessage(), e);
        }
    }
    
    /**
     * Force compilation check for all modules to detect issues.
     * 
     * @param modulesDirectoryPath Path to the modules directory
     */
    private void forceCompilationCheck(String modulesDirectoryPath) {
        try {
            File modulesDir = new File(modulesDirectoryPath);
            File[] subdirs = modulesDir.listFiles(File::isDirectory);
            
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    if (subdir.getName().equals("target") || subdir.getName().equals(".git")) {
                        continue; // Skip non-module directories
                    }
                    
                    File pomFile = new File(subdir, "pom.xml");
                    if (pomFile.exists()) {
                        // Check if this module has compilation issues by attempting compilation
                        File mainJava = new File(subdir, "src/main/java/Main.java");
                        File metadataJava = new File(subdir, "src/main/java/Metadata.java");
                        
                        if (mainJava.exists() && metadataJava.exists()) {
                            // Try to compile the module
                            boolean compilationSuccess = attemptModuleCompilation(subdir);
                            if (!compilationSuccess) {
                                messageReporter.addMessage("⚠️ Module '" + subdir.getName() + "' has compilation errors - check the console for details");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error during forced compilation check: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check for compilation failures and notify the user.
     * 
     * @param modulesDirectoryPath Path to the modules directory
     */
    public void checkForCompilationFailures(String modulesDirectoryPath) {
        try {
            File modulesDir = new File(modulesDirectoryPath);
            File[] subdirs = modulesDir.listFiles(File::isDirectory);
            
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    if (subdir.getName().equals("target") || subdir.getName().equals(".git")) {
                        continue; // Skip non-module directories
                    }
                    
                    File pomFile = new File(subdir, "pom.xml");
                    if (pomFile.exists()) {
                        // Check if this module has compilation issues
                        File classesDir = new File(subdir, "target/classes");
                        File mainClass = new File(classesDir, "Main.class");
                        File metadataClass = new File(classesDir, "Metadata.class");
                        
                        // Check if source files exist
                        File mainJava = new File(subdir, "src/main/java/Main.java");
                        File metadataJava = new File(subdir, "src/main/java/Metadata.java");
                        
                        if (mainJava.exists() && metadataJava.exists()) {
                            // Source files exist, check if compilation succeeded
                            if (!classesDir.exists() || !mainClass.exists() || !metadataClass.exists()) {
                                messageReporter.addMessage("⚠️ Module '" + subdir.getName() + "' failed to compile - check for syntax errors");
                            }
                        } else {
                            // Missing source files
                            if (!mainJava.exists()) {
                                messageReporter.addMessage("⚠️ Module '" + subdir.getName() + "' missing Main.java file");
                            }
                            if (!metadataJava.exists()) {
                                messageReporter.addMessage("⚠️ Module '" + subdir.getName() + "' missing Metadata.java file");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error checking for compilation failures: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check for broken modules (modules that load but have Java file issues).
     * 
     * @param modulesDirectoryPath Path to the modules directory
     */
    public void checkForBrokenModules(String modulesDirectoryPath) {
        try {
            File modulesDir = new File(modulesDirectoryPath);
            File[] subdirs = modulesDir.listFiles(File::isDirectory);
            
            if (subdirs != null) {
                for (File subdir : subdirs) {
                    if (subdir.getName().equals("target") || subdir.getName().equals(".git")) {
                        continue; // Skip non-module directories
                    }
                    
                    File pomFile = new File(subdir, "pom.xml");
                    if (pomFile.exists()) {
                        // Check if this module has Java file issues but still loads
                        File mainJava = new File(subdir, "src/main/java/Main.java");
                        File metadataJava = new File(subdir, "src/main/java/Metadata.java");
                        File classesDir = new File(subdir, "target/classes");
                        File mainClass = new File(classesDir, "Main.class");
                        File metadataClass = new File(classesDir, "Metadata.class");
                        
                        // If core files exist and compile, but there might be other issues
                        if (mainJava.exists() && metadataJava.exists() && 
                            classesDir.exists() && mainClass.exists() && metadataClass.exists()) {
                            
                            // Check if there are other Java files that might have issues
                            List<File> allJavaFiles = findAllJavaFilesInModule(subdir);
                            if (allJavaFiles.size() > 2) { // More than just Main.java and Metadata.java
                                // Check for syntax issues in other files
                                List<String> problematicFiles = new ArrayList<>();
                                for (File javaFile : allJavaFiles) {
                                    if (!javaFile.getName().equals("Main.java") && 
                                        !javaFile.getName().equals("Metadata.java")) {
                                        if (!isJavaFileValid(javaFile)) {
                                            String relativePath = javaFile.getPath().substring(subdir.getPath().length() + 1);
                                            problematicFiles.add(relativePath);
                                        }
                                    }
                                }
                                
                                if (!problematicFiles.isEmpty()) {
                                    messageReporter.addMessage("⚠️ Module '" + subdir.getName() + "' has issues in: " + String.join(", ", problematicFiles));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error checking for broken modules: " + e.getMessage(), e);
        }
    }
    
    /**
     * Attempt to compile a module and return success status.
     * 
     * @param moduleDir The module directory
     * @return true if compilation succeeds
     */
    private boolean attemptModuleCompilation(File moduleDir) {
        try {
            ProcessBuilder pb = new ProcessBuilder("mvn", "compile");
            pb.directory(moduleDir);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // Read output for logging
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Wait for compilation to complete
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                Logging.info("✅ Forced compilation successful for " + moduleDir.getName());
                return true;
            } else {
                Logging.warning("⚠️ Forced compilation failed for " + moduleDir.getName() + " (exit code: " + exitCode + ")");
                // Log compilation errors for debugging
                String[] lines = output.toString().split("\n");
                for (String line : lines) {
                    if (line.contains("ERROR") || line.contains("FAILURE") || line.contains("BUILD FAILURE")) {
                        Logging.warning("  " + line);
                    }
                }
                return false;
            }
            
        } catch (Exception e) {
            Logging.warning("⚠️ Exception during forced compilation of " + moduleDir.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Find all Java files in a module directory.
     * 
     * @param moduleDir The module directory
     * @return List of Java files
     */
    private List<File> findAllJavaFilesInModule(File moduleDir) {
        List<File> javaFiles = new ArrayList<>();
        findJavaFilesRecursivelyInModule(moduleDir, javaFiles);
        return javaFiles;
    }
    
    /**
     * Recursively find Java files in a module directory.
     * 
     * @param dir The directory to search
     * @param javaFiles List to add Java files to
     */
    private void findJavaFilesRecursivelyInModule(File dir, List<File> javaFiles) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Skip target and .git directories
                    if (!file.getName().equals("target") && !file.getName().equals(".git")) {
                        findJavaFilesRecursivelyInModule(file, javaFiles);
                    }
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
    }
    
    /**
     * Check if a Java file has valid syntax.
     * 
     * @param javaFile The Java file to check
     * @return true if the file appears to have valid syntax
     */
    private boolean isJavaFileValid(File javaFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            boolean isCommented = false;
            
            while ((line = reader.readLine()) != null) {
                // Skip commented lines
                if (line.trim().startsWith("//")) {
                    continue;
                }
                
                // Handle block comments
                if (line.contains("/*")) {
                    isCommented = true;
                }
                if (line.contains("*/")) {
                    isCommented = false;
                    continue;
                }
                if (isCommented) {
                    continue;
                }
                
                content.append(line).append("\n");
            }
            
            String sourceCode = content.toString();
            
            // Basic syntax checks
            boolean hasClassDeclaration = sourceCode.contains("class ") || sourceCode.contains("public class ");
            boolean hasBalancedBraces = countChar(sourceCode, '{') == countChar(sourceCode, '}');
            boolean hasBalancedParens = countChar(sourceCode, '(') == countChar(sourceCode, ')');
            boolean hasBalancedBrackets = countChar(sourceCode, '[') == countChar(sourceCode, ']');
            
            return hasClassDeclaration && hasBalancedBraces && hasBalancedParens && hasBalancedBrackets;
            
        } catch (Exception e) {
            Logging.warning("⚠️ Error reading Java file " + javaFile.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Counts occurrences of a character in a string.
     * 
     * @param str The string to search
     * @param ch The character to count
     * @return The count
     */
    private int countChar(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }
}

