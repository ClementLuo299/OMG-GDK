package com.utils;

import com.game.GameModule;
import com.utils.error_handling.Logging;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for dynamically loading game modules from the modules directory.
 * This class provides core loading functionality used by LocalGameSource.
 * 
 * @authors Clement Luo
 * @date July 18, 2025
 * @edited July 19, 2025
 * @since 1.0
 */
public class ModuleLoader {
    
    /**
     * Discovers and loads all game modules from a modules directory.
     * 
     * @param modulesDirPath The path to the modules directory
     * @return List of discovered GameModule instances
     */
    public static List<GameModule> discoverModules(String modulesDirPath) {
        List<GameModule> modules = new ArrayList<>();
        File modulesDir = new File(modulesDirPath);
        
        if (!modulesDir.exists() || !modulesDir.isDirectory()) {
            Logging.warning("⚠️ Modules directory not found: " + modulesDirPath);
            return modules;
        }
        
        File[] moduleDirs = modulesDir.listFiles(File::isDirectory);
        if (moduleDirs == null) {
            Logging.warning("⚠️ Could not list modules directory: " + modulesDirPath);
            return modules;
        }
        
        for (File moduleDir : moduleDirs) {
            GameModule module = loadModule(moduleDir);
            if (module != null) {
                modules.add(module);
            }
        }
        
        return modules;
    }
    
    /**
     * Loads a single game module from a module directory.
     * This is a utility method for loading compiled modules.
     * 
     * @param moduleDir The module directory
     * @return The loaded GameModule, or null if loading failed
     */
    public static GameModule loadModule(File moduleDir) {
        try {
            String moduleName = moduleDir.getName();
            Logging.info("🔍 Loading module: " + moduleName);
            
            // Try to find the game module class
            Class<?> gameClass = findGameModuleClass(moduleDir, moduleName);
            if (gameClass == null) {
                Logging.warning("⚠️ Could not find game module class in: " + moduleName);
                return null;
            }
            
            // Instantiate the game module
            GameModule game = instantiateGameModule(gameClass);
            if (game != null) {
                Logging.info("✅ Successfully loaded: " + game.getGameName() + " from " + moduleName);
            }
            
            return game;
            
        } catch (Exception e) {
            Logging.error("❌ Error loading module " + moduleDir.getName() + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Finds the game module class in a module directory.
     * 
     * @param moduleDir The module directory
     * @param moduleName The module name
     * @return The found Class, or null if not found
     */
    private static Class<?> findGameModuleClass(File moduleDir, String moduleName) {
        // Use the standardized Main class name
        String className = "Main";
        return tryLoadClass(moduleDir, moduleName, className);
    }
    
    /**
     * Attempts to load a class from a module directory.
     * 
     * @param moduleDir The module directory
     * @param moduleName The module name
     * @param className The class name
     * @return The loaded Class, or null if not found
     */
    private static Class<?> tryLoadClass(File moduleDir, String moduleName, String className) {
        try {
            // Try to load from compiled classes first
            Class<?> gameClass = tryLoadFromCompiledClasses(moduleDir, moduleName, className);
            if (gameClass != null) {
                return gameClass;
            }
            
            // Try to load from current classpath
            String fullClassName = "com.games.modules." + moduleName.toLowerCase() + "." + className;
            return Class.forName(fullClassName);
            
        } catch (ClassNotFoundException e) {
            // Class not found, this is expected
            return null;
        } catch (Exception e) {
            Logging.error("❌ Error loading class " + className + " from " + moduleName + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Attempts to load a class from compiled classes in the module.
     * 
     * @param moduleDir The module directory
     * @param moduleName The module name
     * @param className The class name
     * @return The loaded Class, or null if not found
     */
    private static Class<?> tryLoadFromCompiledClasses(File moduleDir, String moduleName, String className) {
        try {
            File classesDir = new File(moduleDir, "target/classes");
            if (!classesDir.exists()) {
                return null;
            }
            
            URLClassLoader classLoader = new URLClassLoader(new URL[]{classesDir.toURI().toURL()});
            String fullClassName = "com.games.modules." + moduleName.toLowerCase() + "." + className;
            
            return classLoader.loadClass(fullClassName);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Instantiates a GameModule from a Class.
     * 
     * @param gameClass The game module class
     * @return The instantiated GameModule, or null if instantiation failed
     */
    private static GameModule instantiateGameModule(Class<?> gameClass) {
        try {
            // Check if it implements GameModule
            if (!GameModule.class.isAssignableFrom(gameClass)) {
                Logging.warning("⚠️ Class " + gameClass.getName() + " does not implement GameModule");
                return null;
            }
            
            // Try to instantiate the class
            Constructor<?> constructor = gameClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            GameModule game = (GameModule) constructor.newInstance();
            
            return game;
            
        } catch (Exception e) {
            Logging.error("❌ Error instantiating game module " + gameClass.getName() + ": " + e.getMessage(), e);
            return null;
        }
    }
} 