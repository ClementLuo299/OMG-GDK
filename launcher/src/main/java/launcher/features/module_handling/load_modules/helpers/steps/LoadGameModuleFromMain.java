package launcher.features.module_handling.load_modules.helpers.steps;

import gdk.api.GameModule;
import gdk.internal.Logging;

/**
 * Helper class for instantiating GameModule instances from loaded Main classes.
 * 
 * <p>This class handles the instantiation process including constructor invocation
 * and extract_metadata validation.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 4, 2026
 * @since Beta 1.0
 */
public final class LoadGameModuleFromMain {
    
    private LoadGameModuleFromMain() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Instantiates a GameModule from a loaded Main class.
     * 
     * <p>This method creates an instance of the Main class by calling its no-arg constructor.
     * The class must have been validated to implement GameModule before calling this method.
     * 
     * @param mainClass The loaded Main class (must implement GameModule)
     * @param moduleName The name of the module (for logging)
     * @return The instantiated GameModule, or null if instantiation failed
     */
    public static GameModule load(Class<?> mainClass, String moduleName) {
        // Finally, create an instance of the Main class by calling its no-arg constructor
        // We can safely cast to GameModule because it was validated before calling this method
        // This is where the module's constructor runs, so any initialization code executes here
        Logging.info("🎯 Instantiating Main class for module: " + moduleName);
        try {
            // Use reflection to call the no-argument constructor
            // This will fail if the class doesn't have a no-arg constructor or if
            // the constructor throws an exception
            GameModule module = (GameModule) mainClass.getDeclaredConstructor().newInstance();
            Logging.info("✅ Instance created for module: " + moduleName);
            
            // Verify we can access the module's extract_metadata (sanity check)
            String gameName = module.getMetadata().getGameName();
            Logging.info("✅ Successfully loaded module: " + moduleName + " (Game: " + gameName + ")");
            return module;
            
        } catch (Exception instantiationError) {
            // Constructor failed - could be missing no-arg constructor, constructor threw exception, etc.
            Logging.error("❌ Failed to instantiate Main class for module " + moduleName + ": " + 
                instantiationError.getMessage(), instantiationError);
            instantiationError.printStackTrace();
            return null;
        }
    }
}

