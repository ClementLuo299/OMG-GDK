package com.utils;

import com.game.GameModule;
import com.utils.error_handling.Logging;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * Loads game modules from JAR files.
 * This allows each module to have its own dependencies and be self-contained.
 * 
 * @authors Clement Luo
 * @date July 20, 2025
 * @since 1.0
 */
public class JarModuleLoader {
    
    private static final String MODULES_DIR = "modules";
    private static final String MAIN_CLASS_NAME = "Main";
    
    /**
     * Discovers and loads all game modules from JAR files in the modules directory.
     * 
     * @return List of discovered GameModule instances
     */
    public static List<GameModule> discoverModules() {
        List<GameModule> modules = new ArrayList<>();
        File modulesDir = new File(MODULES_DIR);
        
        if (!modulesDir.exists() || !modulesDir.isDirectory()) {
            Logging.warning("⚠️ Modules directory not found: " + MODULES_DIR);
            return modules;
        }
        
        File[] jarFiles = modulesDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null) {
            Logging.warning("⚠️ Could not list modules directory: " + MODULES_DIR);
            return modules;
        }
        
        for (File jarFile : jarFiles) {
            GameModule module = loadModuleFromJar(jarFile);
            if (module != null) {
                modules.add(module);
            }
        }
        
        return modules;
    }
    
    /**
     * Loads a game module from a JAR file.
     * 
     * @param jarFile The JAR file containing the module
     * @return The loaded GameModule, or null if loading failed
     */
    public static GameModule loadModuleFromJar(File jarFile) {
        try {
            Logging.info("🔍 Loading module from JAR: " + jarFile.getName());
            
            // Create a class loader for the JAR file
            URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                JarModuleLoader.class.getClassLoader()
            );
            
            // Find the Main class in the JAR
            String mainClassName = findMainClass(jarFile);
            if (mainClassName == null) {
                Logging.warning("⚠️ Could not find Main class in JAR: " + jarFile.getName());
                return null;
            }
            
            // Load the Main class
            Class<?> mainClass = classLoader.loadClass(mainClassName);
            
            // Check if it implements GameModule
            if (!GameModule.class.isAssignableFrom(mainClass)) {
                Logging.warning("⚠️ Main class does not implement GameModule: " + mainClassName);
                return null;
            }
            
            // Instantiate the module
            Constructor<?> constructor = mainClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            GameModule module = (GameModule) constructor.newInstance();
            
            Logging.info("✅ Successfully loaded module from JAR: " + module.getGameName());
            return module;
            
        } catch (Exception e) {
            Logging.error("❌ Error loading module from JAR " + jarFile.getName() + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Finds the Main class in a JAR file.
     * 
     * @param jarFile The JAR file to search
     * @return The fully qualified class name of the Main class, or null if not found
     */
    private static String findMainClass(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            // Look for Main.class files
            java.util.Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith("/Main.class")) {
                    // Convert path to class name
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    Logging.info("📁 Found Main class: " + className);
                    return className;
                }
            }
            
            // If no Main.class found, try to infer from JAR name
            String jarName = jarFile.getName().replace(".jar", "");
            String inferredClassName = "com.games.modules." + jarName + ".Main";
            Logging.info("🔍 Trying inferred class name: " + inferredClassName);
            return inferredClassName;
            
        } catch (Exception e) {
            Logging.error("❌ Error searching JAR file: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Builds a module JAR file from a module directory.
     * This is a utility method for building modules.
     * 
     * @param moduleDir The module directory
     * @return The built JAR file, or null if building failed
     */
    public static File buildModuleJar(File moduleDir) {
        try {
            String moduleName = moduleDir.getName();
            Logging.info("🔨 Building JAR for module: " + moduleName);
            
            // Run Maven to build the module
            ProcessBuilder pb = new ProcessBuilder(
                "mvn", "clean", "package", "-DskipTests"
            );
            pb.directory(moduleDir);
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // Find the built JAR file
                File targetDir = new File(moduleDir, "target");
                File[] jarFiles = targetDir.listFiles((dir, name) -> 
                    name.endsWith(".jar") && !name.endsWith("-sources.jar")
                );
                
                if (jarFiles != null && jarFiles.length > 0) {
                    File jarFile = jarFiles[0];
                    Logging.info("✅ Successfully built JAR: " + jarFile.getName());
                    return jarFile;
                }
            } else {
                Logging.error("❌ Failed to build module JAR: " + moduleName);
            }
            
        } catch (Exception e) {
            Logging.error("❌ Error building module JAR: " + e.getMessage(), e);
        }
        
        return null;
    }
} 