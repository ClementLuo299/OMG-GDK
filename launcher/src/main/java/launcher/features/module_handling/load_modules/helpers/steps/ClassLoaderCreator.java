package launcher.features.module_handling.load_modules.helpers.steps;

import gdk.internal.Logging;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for loading classes from modules.
 * 
 * @author Clement Luo
 * @date January 3, 2026
 * @edited January 3, 2026
 * @since Beta 1.0
 */
public final class ClassLoaderCreator {
    
    private ClassLoaderCreator() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Creates a class loader for a module with necessary dependencies.
     * 
     * <p>This method creates a URLClassLoader that includes:
     * <ul>
     *   <li>The module's compiled classes (target/classes)</li>
     *   <li>GDK classes (gdk/target/classes)</li>
     *   <li>Launcher classes (launcher/target/classes)</li>
     * </ul>
     * 
     * <p>It tries multiple possible locations for dependencies to handle
     * different project structures and launch scenarios.
     * 
     * @param moduleDir The module directory
     * @return URLClassLoader configured for the module
     * @throws Exception if class loader creation fails (e.g., no valid classpath URLs found)
     */
    public static URLClassLoader create(File moduleDir) throws Exception {
        List<URL> classpathUrls = new ArrayList<>();
        
        // Add module's target/classes directory
        File targetClassesDir = new File(moduleDir, "target/classes");
        if (targetClassesDir.exists()) {
            classpathUrls.add(targetClassesDir.toURI().toURL());
            Logging.info("Added module classes to classpath: " + targetClassesDir.getAbsolutePath());
        } else {
            Logging.warning("Module target/classes directory does not exist: " + targetClassesDir.getAbsolutePath());
        }
        
        // Resolve GDK and launcher paths relative to the module directory
        // Modules are in modules/ subdirectory, so we need to go up to project root
        File moduleParent = moduleDir.getParentFile(); // modules/
        File projectRoot = moduleParent != null ? moduleParent.getParentFile() : null; // project root
        
        if (projectRoot != null) {
            // Add GDK classes - try multiple possible locations
            File[] gdkCandidates = {
                new File(projectRoot, "gdk/target/classes"),
                new File(moduleDir, "../../gdk/target/classes"),
                new File("../gdk/target/classes"),
                new File("gdk/target/classes")
            };
            
            boolean gdkAdded = false;
            for (File gdkCandidate : gdkCandidates) {
                File gdkClassesDir = gdkCandidate.getAbsoluteFile();
                if (gdkClassesDir.exists() && gdkClassesDir.isDirectory()) {
                    classpathUrls.add(gdkClassesDir.toURI().toURL());
                    Logging.info("Added GDK classes to classpath: " + gdkClassesDir.getAbsolutePath());
                    gdkAdded = true;
                    break;
                }
            }
            
            if (!gdkAdded) {
                StringBuilder triedPaths = new StringBuilder();
                for (File candidate : gdkCandidates) {
                    if (triedPaths.length() > 0) triedPaths.append(", ");
                    triedPaths.append(candidate.getAbsolutePath());
                }
                Logging.warning("GDK classes directory not found. Tried: " + triedPaths.toString());
            }
            
            // Add launcher classes - try multiple possible locations
            File[] launcherCandidates = {
                new File(projectRoot, "launcher/target/classes"),
                new File(moduleDir, "../../launcher/target/classes"),
                new File("../launcher/target/classes"),
                new File("launcher/target/classes")
            };
            
            boolean launcherAdded = false;
            for (File launcherCandidate : launcherCandidates) {
                File launcherClassesDir = launcherCandidate.getAbsoluteFile();
                if (launcherClassesDir.exists() && launcherClassesDir.isDirectory()) {
                    classpathUrls.add(launcherClassesDir.toURI().toURL());
                    Logging.info("Added launcher classes to classpath: " + launcherClassesDir.getAbsolutePath());
                    launcherAdded = true;
                    break;
                }
            }
            
            if (!launcherAdded) {
                StringBuilder triedPaths = new StringBuilder();
                for (File candidate : launcherCandidates) {
                    if (triedPaths.length() > 0) triedPaths.append(", ");
                    triedPaths.append(candidate.getAbsolutePath());
                }
                Logging.warning("Launcher classes directory not found. Tried: " + triedPaths.toString());
            }
        } else {
            Logging.warning("Could not determine project root from module directory: " + moduleDir.getAbsolutePath());
            // Fallback to relative paths
            File gdkClassesDir = new File("../gdk/target/classes").getAbsoluteFile();
            if (gdkClassesDir.exists()) {
                classpathUrls.add(gdkClassesDir.toURI().toURL());
                Logging.info("Added GDK classes to classpath (fallback): " + gdkClassesDir.getAbsolutePath());
            }
            
            File launcherClassesDir = new File("../launcher/target/classes").getAbsoluteFile();
            if (launcherClassesDir.exists()) {
                classpathUrls.add(launcherClassesDir.toURI().toURL());
                Logging.info("Added launcher classes to classpath (fallback): " + launcherClassesDir.getAbsolutePath());
            }
        }
        
        if (classpathUrls.isEmpty()) {
            throw new Exception("No valid classpath URLs found for module: " + moduleDir.getAbsolutePath());
        }
        
        Logging.info("Created classloader with " + classpathUrls.size() + " classpath entries for module: " + moduleDir.getName());
        return new URLClassLoader(classpathUrls.toArray(new URL[0]), ClassLoaderCreator.class.getClassLoader());
    }
}

