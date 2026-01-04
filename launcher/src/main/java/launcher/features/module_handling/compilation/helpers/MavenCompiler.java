package launcher.features.module_handling.compilation.helpers;

import gdk.internal.Logging;

import java.io.File;

/**
 * Helper class for compiling modules using Maven.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class MavenCompiler {
    
    private MavenCompiler() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Compiles a specific module using Maven.
     * 
     * @param modulePath The path to the module to compile
     * @return true if compilation was successful, false otherwise
     */
    public static boolean compileModule(String modulePath) {
        Logging.info("Building module: " + modulePath);
        
        try {
            // Find Maven command
            String mavenCommand = findMavenCommand();
            
            // Build the module
            ProcessBuilder processBuilder = new ProcessBuilder(mavenCommand, "clean", "compile");
            processBuilder.directory(new File(modulePath));
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                Logging.info("Module built successfully: " + modulePath);
                return true;
            } else {
                Logging.info("Module build completed with warnings: " + modulePath);
                return false;
            }
        } catch (Exception buildError) {
            Logging.error("Failed to build module " + modulePath + ": " + buildError.getMessage(), buildError);
            return false;
        }
    }
    
    /**
     * Finds the Maven command to use.
     * 
     * <p>This method tries common Maven command names (mvn, mvn.cmd, mvn.bat)
     * and tests if they work by running "mvn --version". Returns the first
     * working command, or "mvn" as a default.
     * 
     * @return The Maven command path
     */
    private static String findMavenCommand() {
        // Try to find Maven in the system PATH
        String[] possibleCommands = {"mvn", "mvn.cmd", "mvn.bat"};
        
        for (String command : possibleCommands) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(command, "--version");
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    return command;
                }
            } catch (Exception e) {
                // Continue to next command
            }
        }
        
        // Default to mvn if not found
        return "mvn";
    }
}

