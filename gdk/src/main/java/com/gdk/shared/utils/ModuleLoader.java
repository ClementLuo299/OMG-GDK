package com.gdk.shared.utils;

import com.gdk.shared.game.GameModule;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Utility class for discovering and loading game modules.
 * Provides functionality to scan directories for game modules and load them dynamically.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @since 1.0
 */
public class ModuleLoader {
    
    /**
     * Discovers game modules in the specified directory.
     * Scans for JAR files and attempts to load GameModule implementations.
     * 
     * @param modulesDir The directory to scan for modules
     * @return List of discovered game modules
     * @throws Exception if there's an error during discovery
     */
    public static List<GameModule> discoverModules(String modulesDir) throws Exception {
        List<GameModule> modules = new ArrayList<>();
        File dir = new File(modulesDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Modules directory does not exist: " + modulesDir);
        }
        
        // Scan for JAR files in the modules directory
        File[] jarFiles = dir.listFiles((file, name) -> name.endsWith(".jar"));
        
        if (jarFiles != null) {
            for (File jarFile : jarFiles) {
                try {
                    GameModule module = loadModule(jarFile);
                    if (module != null) {
                        modules.add(module);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load module from " + jarFile.getName() + ": " + e.getMessage());
                }
            }
        }
        
        return modules;
    }
    
    /**
     * Loads a game module from a JAR file.
     * 
     * @param jarFile The JAR file to load
     * @return The loaded game module, or null if loading fails
     * @throws Exception if there's an error during loading
     */
    public static GameModule loadModule(File jarFile) throws Exception {
        try (JarFile jar = new JarFile(jarFile)) {
            // Check if the JAR has a manifest with a main class
            Manifest manifest = jar.getManifest();
            String mainClass = null;
            
            if (manifest != null) {
                mainClass = manifest.getMainAttributes().getValue("Main-Class");
            }
            
            // If no main class in manifest, try to find GameModule implementations
            if (mainClass == null) {
                mainClass = findGameModuleClass(jar);
            }
            
            if (mainClass != null) {
                // Load the class
                URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{jarFile.toURI().toURL()},
                    ModuleLoader.class.getClassLoader()
                );
                
                Class<?> moduleClass = classLoader.loadClass(mainClass);
                
                // Check if it implements GameModule
                if (GameModule.class.isAssignableFrom(moduleClass)) {
                    // Create an instance
                    Object instance = moduleClass.getDeclaredConstructor().newInstance();
                    return (GameModule) instance;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Finds a GameModule implementation class in a JAR file.
     * This is a simplified implementation - in a real system, you might want
     * to scan all classes in the JAR.
     * 
     * @param jarFile The JAR file to scan
     * @return The name of a GameModule class, or null if not found
     */
    private static String findGameModuleClass(JarFile jarFile) {
        // This is a simplified implementation
        // In a real system, you would scan all entries in the JAR
        // and look for classes that implement GameModule
        
        // For now, we'll look for common patterns
        String[] commonPatterns = {
            "Main",
            "GameModule",
            "Game",
            "Module"
        };
        
        for (String pattern : commonPatterns) {
            // Check if there's a class with this name
            if (jarFile.getEntry(pattern + ".class") != null) {
                return pattern;
            }
        }
        
        return null;
    }
    
    /**
     * Validates that a module implements the required interface.
     * 
     * @param module The module to validate
     * @return true if the module is valid
     */
    public static boolean validateModule(GameModule module) {
        if (module == null) {
            return false;
        }
        
        // Check required methods
        try {
            String gameId = module.getGameId();
            String gameName = module.getGameName();
            
            if (gameId == null || gameId.trim().isEmpty()) {
                return false;
            }
            
            if (gameName == null || gameName.trim().isEmpty()) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 