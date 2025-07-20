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
        
        // First, try to load modules from the current classpath
        modules.addAll(discoverModulesFromClasspath());
        
        // Then try to load from the modules directory (only if not already found in classpath)
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
            // Only load if not already found in classpath
            if (!isModuleAlreadyLoaded(modules, moduleDir.getName())) {
                GameModule module = loadModule(moduleDir);
                if (module != null) {
                    modules.add(module);
                }
            }
        }
        
        // Also try to load from outer modules directory (only if not already found)
        List<GameModule> outerModules = discoverModulesFromOuterDirectory();
        for (GameModule outerModule : outerModules) {
            if (!isModuleAlreadyLoaded(modules, outerModule.getGameId())) {
                modules.add(outerModule);
            }
        }
        
        return modules;
    }
    
    /**
     * Discovers game modules from the current classpath.
     * 
     * @return List of discovered GameModule instances
     */
    private static List<GameModule> discoverModulesFromClasspath() {
        List<GameModule> modules = new ArrayList<>();
        
        // Known module names to look for with their actual class names
        String[][] knownModules = {
            {"tictactoe", "Main"},
            {"example", "Main"}
        };
        
        for (String[] moduleInfo : knownModules) {
            String moduleName = moduleInfo[0];
            String className = moduleInfo[1];
            
            try {
                // Try to load the module class directly
                String fullClassName = "com.games.modules." + moduleName + "." + className;
                Class<?> moduleClass = Class.forName(fullClassName);
                
                if (GameModule.class.isAssignableFrom(moduleClass)) {
                    GameModule module = instantiateGameModule(moduleClass);
                    if (module != null) {
                        modules.add(module);
                        Logging.info("✅ Loaded module from classpath: " + module.getGameName());
                    }
                }
            } catch (ClassNotFoundException e) {
                // Module not found, this is expected
                Logging.info("ℹ️ Module not found in classpath: " + moduleName);
            } catch (Exception e) {
                Logging.error("❌ Error loading module from classpath: " + moduleName + " - " + e.getMessage());
            }
        }
        
        return modules;
    }
    
    /**
     * Discovers game modules from the outer modules directory.
     * 
     * @return List of discovered GameModule instances
     */
    private static List<GameModule> discoverModulesFromOuterDirectory() {
        List<GameModule> modules = new ArrayList<>();
        
        // Check the outer modules directory
        File outerModulesDir = new File("modules");
        if (!outerModulesDir.exists() || !outerModulesDir.isDirectory()) {
            return modules;
        }
        
        File[] moduleDirs = outerModulesDir.listFiles(File::isDirectory);
        if (moduleDirs == null) {
            return modules;
        }
        
        for (File moduleDir : moduleDirs) {
            try {
                String moduleName = moduleDir.getName();
                Logging.info("🔍 Checking outer module: " + moduleName);
                
                // Try to load the Main class from the outer module
                String className = "com.games.modules." + moduleName + ".Main";
                Class<?> moduleClass = Class.forName(className);
                
                if (GameModule.class.isAssignableFrom(moduleClass)) {
                    GameModule module = instantiateGameModule(moduleClass);
                    if (module != null) {
                        modules.add(module);
                        Logging.info("✅ Loaded outer module: " + module.getGameName());
                    }
                }
            } catch (ClassNotFoundException e) {
                Logging.info("ℹ️ Outer module not found: " + moduleDir.getName());
            } catch (Exception e) {
                Logging.error("❌ Error loading outer module " + moduleDir.getName() + ": " + e.getMessage());
            }
        }
        
        return modules;
    }
    
    /**
     * Checks if a module is already loaded in the list.
     * 
     * @param modules The list of already loaded modules
     * @param moduleId The module ID to check
     * @return true if the module is already loaded, false otherwise
     */
    private static boolean isModuleAlreadyLoaded(List<GameModule> modules, String moduleId) {
        for (GameModule module : modules) {
            if (module.getGameId().equals(moduleId)) {
                return true;
            }
        }
        return false;
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
        // Try different possible class names for the game module
        String[] possibleClassNames = {
            moduleName + "Module",  // e.g., "TicTacToeModule"
            "Main",
            moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1) + "Module", // Capitalized
            moduleName.toLowerCase() + "Module" // Lowercase
        };
        
        for (String className : possibleClassNames) {
            Class<?> foundClass = tryLoadClass(moduleDir, moduleName, className);
            if (foundClass != null) {
                return foundClass;
            }
        }
        
        return null;
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
            // Try to load from current classpath first (since modules are part of main project)
            String fullClassName = "com.games.modules." + moduleName.toLowerCase() + "." + className;
            try {
                return Class.forName(fullClassName);
            } catch (ClassNotFoundException e) {
                // Try with capitalized module name
                fullClassName = "com.games.modules." + moduleName + "." + className;
                try {
                    return Class.forName(fullClassName);
                } catch (ClassNotFoundException e2) {
                    // Continue to try other methods
                }
            }
            
            // Try to load from compiled classes in module directory
            Class<?> gameClass = tryLoadFromCompiledClasses(moduleDir, moduleName, className);
            if (gameClass != null) {
                return gameClass;
            }
            
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