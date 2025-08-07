package launcher.lifecycle.start;

import gdk.Logging;
import gdk.GameModule;
import launcher.utils.ModuleLoader;
import launcher.gui.GDKGameLobbyController;
import launcher.lifecycle.start.StartupProgressWindow;
import launcher.lifecycle.start.PreStartupProgressWindow;
import launcher.gui.GDKViewModel;
import launcher.GDKApplication;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;




/**
 * Handles the complete startup process for the GDK application.
 * 
 * This class orchestrates the initialization of all major components
 * required for the GDK to function properly. It follows a step-by-step
 * approach to ensure each component is properly initialized before
 * proceeding to the next.
 * 
 * Key responsibilities:
 * - Load and initialize the main UI (FXML and CSS)
 * - Create and configure the ViewModel
 * - Set up the primary application stage
 * - Wire up all components for proper communication
 * - Handle any startup errors gracefully
 *
 * @authors Clement Luo
 * @date July 25, 2025
 * @edited August 6, 2025
 * @since 1.0
 */
public class Startup {

    // ==================== INSTANCE VARIABLES ====================
    
    /**
     * The primary JavaFX stage that hosts the main application window
     */
    private final Stage primaryApplicationStage;
    
    /**
     * Utility for discovering and loading game modules from the filesystem
     */
    private final ModuleLoader gameModuleLoader;
    
    /**
     * Controller for the main GDK lobby interface
     */
    private GDKGameLobbyController lobbyController;
    
    /**
     * Startup progress window for showing initialization progress
     */
    private StartupProgressWindow progressWindow;
    
    /**
     * Pre-startup progress window (Swing-based)
     */
    private PreStartupProgressWindow preProgressWindow;

    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a new Startup instance with the provided primary stage.
     * 
     * @param primaryApplicationStage The primary stage for the application
     */
    public Startup(Stage primaryApplicationStage) {
        this.primaryApplicationStage = primaryApplicationStage;
        this.gameModuleLoader = new ModuleLoader();
    }
    
    /**
     * Create a new Startup instance with the provided primary stage and progress window.
     * 
     * @param primaryApplicationStage The primary stage for the application
     * @param progressWindow The progress window to use for startup updates
     */
    public Startup(Stage primaryApplicationStage, StartupProgressWindow progressWindow) {
        this.primaryApplicationStage = primaryApplicationStage;
        this.gameModuleLoader = new ModuleLoader();
        this.progressWindow = progressWindow;
    }
    
    /**
     * Create a new Startup instance with the provided primary stage and pre-startup progress window.
     * 
     * @param primaryApplicationStage The primary stage for the application
     * @param preProgressWindow The pre-startup progress window to use for startup updates
     */
    public Startup(Stage primaryApplicationStage, PreStartupProgressWindow preProgressWindow) {
        this.primaryApplicationStage = primaryApplicationStage;
        this.gameModuleLoader = new ModuleLoader();
        this.preProgressWindow = preProgressWindow;
    }

    // ==================== PUBLIC STARTUP ORCHESTRATION ====================
    
    /**
     * Execute the complete GDK startup process.
     * 
     * This method orchestrates the entire startup sequence, ensuring
     * that all components are properly initialized in the correct order.
     * If any step fails, the entire startup process is aborted with
     * appropriate error handling.
     */
    public void start() {
        Logging.info("🔄 Starting GDK application startup process");
        
        // Use pre-startup window if provided, otherwise create JavaFX progress window
        if (preProgressWindow != null) {
            // Use the pre-startup window for all progress updates
            preProgressWindow.setTotalSteps(15); // 15 granular steps for better progress tracking
            preProgressWindow.updateProgress(0, "Starting GDK application...");
        } else {
            // Create a new JavaFX progress window for clean startup
            progressWindow = new StartupProgressWindow();
            progressWindow.setTotalSteps(15); // 15 granular steps for better progress tracking
            progressWindow.show();
        }
        
        try {
            // Step 1: Initialize the main user interface (this includes FXML loading and controller init)
            updateProgress(1, "Loading user interface...");
            Scene mainLobbyScene = initializeMainUserInterface();
            
            // Step 2: Create and configure the ViewModel (quick step, no progress update)
            GDKViewModel applicationViewModel = initializeApplicationViewModel();
            
            // Step 3: Configure the primary application stage (quick step, no progress update)
            configurePrimaryApplicationStage(mainLobbyScene);
            
            // Step 4: Wire up the controller with the ViewModel (quick step, no progress update)
            wireUpControllerWithViewModel(mainLobbyScene, applicationViewModel);
            
            // Pass the progress window to the controller for module loading updates
            if (lobbyController != null && progressWindow != null) {
                lobbyController.setStartupProgressWindow(progressWindow);
            }
            
            // Step 2: Ensure UI is ready and show application (this takes time)
            updateProgress(2, "Preparing application...");
            
            // Ensure the UI is fully ready before showing the main window
            ensureUIReady();
            
            // Hide the progress window FIRST
            Logging.info("🏁 Hiding progress window...");
            hideProgressWindow();
            
            // Wait a moment for the progress window to fully close
            Thread.sleep(1000); // DEVELOPMENT: Wait for progress window to close
            
            // Now show the main application window when everything is ready
            // Use Platform.runLater to ensure proper JavaFX thread timing
            javafx.application.Platform.runLater(() -> {
                // Show the window but keep it invisible
                Logging.info("🎬 Showing main application window (invisible)...");
                primaryApplicationStage.show();
                
                // Use another runLater to ensure the window is fully rendered
                javafx.application.Platform.runLater(() -> {
                    // Use a timer to delay the fade-in
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(3000)); // DEVELOPMENT: Slowed down
                    pause.setOnFinished(event -> {
                        // Then fade in the main window
                        Logging.info("✨ Fading in main application window...");
                        primaryApplicationStage.setOpacity(1.0);
                    });
                    pause.play();
                });
            });
            
            Logging.info("✅ GDK application startup completed successfully");
        } catch (Exception startupError) {
            Logging.error("❌ GDK application startup failed: " + startupError.getMessage(), startupError);
            
            // Show error in progress window before hiding
            if (progressWindow != null) {
                progressWindow.addMessage("❌ ERROR: " + startupError.getMessage());
                progressWindow.updateProgress(5, "Startup failed - check error messages");
                
                // Keep progress window visible for a moment to show error
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                progressWindow.hide();
            }
            
            throw new RuntimeException("Failed to start GDK application", startupError);
        }
    }

    // ==================== USER INTERFACE INITIALIZATION ====================
    
    /**
     * Initialize the main user interface components.
     * 
     * This method loads the FXML layout, applies CSS styling, and
     * sets up the lobby controller with necessary references.
     * 
     * @return The initialized Scene object
     */
    private Scene initializeMainUserInterface() {
        Logging.info("🎨 Initializing main user interface components");
        
        try {
            // Load FXML scene from resources
            URL fxmlResourceUrl = GDKApplication.class.getResource("/gdk-lobby/GDKGameLobby.fxml");
            if (fxmlResourceUrl == null) {
                throw new RuntimeException("FXML resource not found: /gdk-lobby/GDKGameLobby.fxml");
            }
            
            Logging.info("📂 Loading FXML from: " + fxmlResourceUrl);
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlResourceUrl);
            
            // Register custom classes for FXML loading
            fxmlLoader.setClassLoader(GDKApplication.class.getClassLoader());
            
            Scene mainLobbyScene = new Scene(fxmlLoader.load());
            
            // Store the controller reference for later use
            this.lobbyController = fxmlLoader.getController();
            
            // Apply CSS styling to the scene
            URL cssResourceUrl = GDKApplication.class.getResource("/gdk-lobby/gdk-lobby.css");
            if (cssResourceUrl != null) {
                mainLobbyScene.getStylesheets().add(cssResourceUrl.toExternalForm());
                Logging.info("📂 CSS styling loaded from: " + cssResourceUrl);
            }
            
            // Set up the lobby controller with necessary references
            if (this.lobbyController != null) {
                Logging.info("✅ Lobby controller initialized successfully");
            } else {
                throw new RuntimeException("Lobby controller is null - FXML loading may have failed");
            }
            
            Logging.info("✅ Main user interface loaded successfully");
            return mainLobbyScene;
        } catch (Exception uiInitializationError) {
            Logging.error("❌ Failed to load main user interface: " + uiInitializationError.getMessage(), uiInitializationError);
            throw new RuntimeException("Failed to initialize main user interface", uiInitializationError);
        }
    }
    
    // ==================== VIEWMODEL INITIALIZATION ====================
    
    /**
     * Initialize the application ViewModel.
     * 
     * This method creates the ViewModel instance and configures it
     * with the necessary dependencies for proper operation.
     * 
     * @return The configured ViewModel instance
     */
    private GDKViewModel initializeApplicationViewModel() {
        Logging.info("🧠 Initializing application ViewModel");
        
        try {
            // Create a new ViewModel instance with the module loader
            GDKViewModel applicationViewModel = new GDKViewModel(gameModuleLoader);
            
            // Configure the ViewModel with the primary application stage
            applicationViewModel.setPrimaryStage(primaryApplicationStage);
            
            Logging.info("✅ Application ViewModel initialized successfully");
            return applicationViewModel;
        } catch (Exception viewModelInitializationError) {
            Logging.error("❌ Failed to initialize ViewModel: " + viewModelInitializationError.getMessage(), viewModelInitializationError);
            throw new RuntimeException("Failed to initialize ViewModel", viewModelInitializationError);
        }
    }
    
    // ==================== STAGE CONFIGURATION ====================
    
    /**
     * Configure the primary application stage with proper settings.
     * 
     * This method sets up the stage with appropriate title, size,
     * and minimum dimensions for optimal user experience.
     * 
     * @param mainLobbyScene The scene to set on the stage
     */
    private void configurePrimaryApplicationStage(Scene mainLobbyScene) {
        Logging.info("⚙️ Configuring primary application stage");
        
        // Set the basic properties of the primary stage
        primaryApplicationStage.setTitle("OMG Game Development Kit");
        primaryApplicationStage.setWidth(1200);
        primaryApplicationStage.setHeight(900);
        primaryApplicationStage.setMinWidth(800);
        primaryApplicationStage.setMinHeight(600);
        
        // Set the main lobby scene on the primary stage
        primaryApplicationStage.setScene(mainLobbyScene);
        
        // Make the window invisible initially to prevent flash
        primaryApplicationStage.setOpacity(0.0);
        
        // Don't show the window yet - we'll show it at the very end
        // primaryApplicationStage.show(); // This will be called later
        
        Logging.info("✅ Primary application stage configured successfully");
    }
    
    // ==================== COMPONENT WIRING ====================
    
    /**
     * Wire up the controller with the ViewModel for proper communication.
     * 
     * This method establishes the connection between the UI controller
     * and the ViewModel, enabling proper data binding and event handling.
     * 
     * @param mainLobbyScene The main lobby scene (unused but kept for consistency)
     * @param applicationViewModel The ViewModel to wire up
     */
    private void wireUpControllerWithViewModel(Scene mainLobbyScene, GDKViewModel applicationViewModel) {
        try {
            if (this.lobbyController != null) {
                this.lobbyController.setViewModel(applicationViewModel);
                Logging.info("✅ Controller wired up with ViewModel successfully");
            } else {
                Logging.warning("⚠️ Lobby controller not found for wiring");
            }
        } catch (Exception wiringError) {
            Logging.error("❌ Error wiring up controller: " + wiringError.getMessage(), wiringError);
        }
    }
    
    // ==================== PROGRESS WINDOW HELPERS ====================
    
    /**
     * Update progress on the appropriate progress window
     * @param step The current step
     * @param status The status message
     */
    private void updateProgress(int step, String status) {
        if (preProgressWindow != null) {
            preProgressWindow.updateProgress(step, status);
        } else if (progressWindow != null) {
            progressWindow.updateProgress(step, status);
        }
    }
    
    /**
     * Update progress with individual module loading message
     * @param moduleName The name of the module being loaded
     */
    public void updateProgressWithModule(String moduleName) {
        String status = "Loading module: " + moduleName + "...";
        // This method is called by ModuleLoader, but we handle individual module progress
        // in loadModulesWithProgress() method, so this is just a fallback
        updateProgress(6, status);
    }
    
    /**
     * Load modules with individual progress messages for each module
     */
    private void loadModulesWithProgress() {
        try {
            String modulesDirectoryPath = GDKApplication.MODULES_DIRECTORY_PATH;
            File modulesDir = new File(modulesDirectoryPath);
            
            if (!modulesDir.exists() || !modulesDir.isDirectory()) {
                updateProgress(6, "Modules directory not found");
                Thread.sleep(3000); // DEVELOPMENT: Slowed down to see message
                return;
            }
            
            File[] subdirs = modulesDir.listFiles(File::isDirectory);
            if (subdirs == null || subdirs.length == 0) {
                updateProgress(6, "No modules found");
                Thread.sleep(3000); // DEVELOPMENT: Slowed down to see message
                return;
            }
            
            // Filter out non-module directories
            List<File> moduleDirs = new ArrayList<>();
            for (File subdir : subdirs) {
                if (!subdir.getName().equals("target") && !subdir.getName().equals(".git")) {
                    moduleDirs.add(subdir);
                }
            }
            
            // Load each module with individual progress message (steps 6-11)
            int moduleStep = 6;
            for (File moduleDir : moduleDirs) {
                String moduleName = moduleDir.getName();
                updateProgress(moduleStep, "Loading module: " + moduleName + "...");
                Thread.sleep(3000); // DEVELOPMENT: Slowed down to see each module message
                moduleStep++;
            }
            
            // Now trigger the actual module loading (this will populate the UI)
            lobbyController.refreshAvailableGameModulesFast();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logging.error("❌ Module loading interrupted: " + e.getMessage(), e);
            updateProgress(11, "Module loading interrupted");
        } catch (Exception e) {
            Logging.error("❌ Error loading modules with progress: " + e.getMessage(), e);
            updateProgress(11, "Error loading modules");
            try {
                Thread.sleep(3000); // DEVELOPMENT: Slowed down to see error message
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
    

    
    /**
     * Hide the appropriate progress window
     */
    private void hideProgressWindow() {
        if (preProgressWindow != null) {
            preProgressWindow.hide();
        } else if (progressWindow != null) {
            progressWindow.hide();
        }
    }
    
    /**
     * Ensure the UI is fully ready before showing the main window
     */
    private void ensureUIReady() {
        Logging.info("🔧 Ensuring UI is fully ready...");
        
        try {
            // Verify the lobby controller is ready
            if (lobbyController == null) {
                throw new RuntimeException("Lobby controller is not ready");
            }
            
            // Step 1: Initialize game modules
            updateProgress(2, "Initializing game modules...");
            Thread.sleep(3000); // DEVELOPMENT: Slowed down to see messages
            
            // Step 2: Build modules if needed (moved from shell script to JavaFX app)
            if (needToBuildModules()) {
                
                // Build GDK first (if needed)
                if (!new File("../gdk/target/classes").exists()) {
                    updateProgress(3, "Building GDK...");
                    Thread.sleep(3000); // DEVELOPMENT: Slowed down to see messages
                    buildModule("../gdk");
                }
                
                // Build launcher
                updateProgress(4, "Building launcher...");
                Thread.sleep(3000); // DEVELOPMENT: Slowed down to see messages
                buildModule(".");
            } else {
                updateProgress(3, "Using existing builds (recent compilation detected)");
                Thread.sleep(3000); // DEVELOPMENT: Slowed down to see messages
            }
            
            // Step 3: Prepare module discovery
            updateProgress(5, "Preparing module discovery...");
            Thread.sleep(3000); // DEVELOPMENT: Slowed down to see messages
            
            // Register this startup instance with ModuleLoader for individual module messages
            ModuleLoader.setStartupProgressWindow(this);
            
            // Step 4: Load modules with individual progress messages
            if (lobbyController != null) {
                loadModulesWithProgress();
            }
            
            // Step 5: Finalize module loading
            updateProgress(12, "Finalizing module loading...");
            Thread.sleep(3000); // DEVELOPMENT: Slowed down to see message
            
            // Step 6: Check for compilation failures after modules are loaded
            if (lobbyController != null) {
                updateProgress(13, "Checking for compilation issues...");
                Thread.sleep(3000); // DEVELOPMENT: Slowed down to see message
                
                // Check for compilation failures
                lobbyController.checkStartupCompilationFailures();
                
                // Clear startup progress window reference after startup is complete
                lobbyController.clearStartupProgressWindow();
            }
            
            // Step 7: Startup complete
            updateProgress(14, "Startup complete");
            Thread.sleep(3000); // DEVELOPMENT: Slowed down to see message
            
            // Step 8: Final step to reach 100%
            updateProgress(15, "Ready!");
            Thread.sleep(3000); // DEVELOPMENT: Slowed down to see message
            
            // Clear the startup progress window reference to prevent further updates
            ModuleLoader.clearStartupProgressWindow();
            
            // Force JavaFX to process all pending events
            javafx.application.Platform.runLater(() -> {
                Logging.info("✅ UI components fully initialized");
            });
            
            // Small additional delay to ensure everything is ready
            Thread.sleep(200);
            
            Logging.info("✅ UI is fully ready for user interaction");
        } catch (Exception e) {
            Logging.error("❌ Error ensuring UI readiness: " + e.getMessage(), e);
            throw new RuntimeException("Failed to ensure UI readiness", e);
        }
    }
    
    // ==================== MODULE BUILDING METHODS ====================
    
    /**
     * Check if modules need to be built based on file timestamps
     * @return true if modules need to be built
     */
    private boolean needToBuildModules() {
        try {
            // For now, skip building modules to avoid Maven execution issues
            // The modules can be built manually if needed
            Logging.info("⏭️ Skipping module build step to avoid Maven execution issues");
            return false;
            
            // Original logic (commented out):
            // Check if launcher classes exist (minimal check)
            // if (!new File("target/classes").exists()) {
            //     return true;
            // }
            // 
            // // Simple check: if launcher target directory is recent, skip build
            // long currentTime = System.currentTimeMillis();
            // long launcherAge = currentTime - new File("target/classes").lastModified();
            // 
            // // If launcher is less than 5 minutes old, assume no changes
            // return launcherAge >= 300000; // 5 minutes = 300000ms
        } catch (Exception e) {
            Logging.error("❌ Error checking if modules need to be built: " + e.getMessage(), e);
            return false; // Skip build on error to allow app to start
        }
    }
    
    /**
     * Build a module using Maven
     * @param modulePath The path to the module directory
     */
    private void buildModule(String modulePath) {
        try {
            Logging.info("🔨 Building module: " + modulePath);
            
            // Try to find Maven in common locations
            String mvnCommand = findMavenCommand();
            
            // Create process builder for Maven command
            ProcessBuilder pb = new ProcessBuilder(mvnCommand, "compile", "-DskipTests", "-q");
            pb.directory(new File(modulePath));
            
            // Set environment variables to ensure Maven can be found
            Map<String, String> env = pb.environment();
            String path = env.get("PATH");
            if (path != null && !path.contains("/usr/bin")) {
                env.put("PATH", path + ":/usr/bin:/usr/local/bin");
            }
            
            // Redirect stderr to suppress warnings
            pb.redirectErrorStream(true);
            
            // Start the process
            Process process = pb.start();
            
            // Wait for completion
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new RuntimeException("Maven build failed with exit code: " + exitCode);
            }
            
            Logging.info("✅ Successfully built module: " + modulePath);
        } catch (IOException | InterruptedException e) {
            Logging.error("❌ Error building module " + modulePath + ": " + e.getMessage(), e);
            throw new RuntimeException("Failed to build module: " + modulePath, e);
        }
    }
    
    /**
     * Find the Maven command to use
     * @return The Maven command path
     */
    private String findMavenCommand() {
        // Try common Maven locations
        String[] possiblePaths = {
            "/usr/bin/mvn",
            "/usr/local/bin/mvn",
            "mvn"
        };
        
        for (String path : possiblePaths) {
            File mvnFile = new File(path);
            if (mvnFile.exists() && mvnFile.canExecute()) {
                return path;
            }
        }
        
        // Fallback to just "mvn" and let the system handle it
        return "mvn";
    }
} 