import gdk.Logging;
import gdk.GameModule;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import launcher.StartupProgressWindow;
import launcher.PreStartupProgressWindow;

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
 * @edited July 25, 2025
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
            preProgressWindow.setTotalSteps(3); // 3 main startup steps (simplified)
            preProgressWindow.updateProgress(0, "Starting GDK application...");
        } else {
            // Create a new JavaFX progress window for clean startup
            progressWindow = new StartupProgressWindow();
            progressWindow.setTotalSteps(3); // 3 main startup steps (simplified)
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
            
            // Now show the main application window when everything is ready
            updateProgress(3, "Starting application...");
            
            // Use Platform.runLater to ensure proper JavaFX thread timing
            javafx.application.Platform.runLater(() -> {
                // Show the window but keep it invisible
                Logging.info("🎬 Showing main application window (invisible)...");
                primaryApplicationStage.show();
                
                // Use another runLater to ensure the window is fully rendered
                javafx.application.Platform.runLater(() -> {
                    // Wait a bit more to ensure all UI components are fully rendered
                    updateProgress(3, "Rendering application...");
                    
                    // Use a timer to delay the fade-in
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(3000)); // DEVELOPMENT: Slowed down
                    pause.setOnFinished(event -> {
                        // Now fade in the main window
                        Logging.info("✨ Fading in main application window...");
                        primaryApplicationStage.setOpacity(1.0);
                        
                        // Hide the progress window after the main window is visible
                        Logging.info("🏁 Hiding progress window...");
                        hideProgressWindow();
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
            
            // Wait for the controller to finish its initialization
            updateProgress(2, "Initializing game modules...");
            Thread.sleep(3000); // DEVELOPMENT: Slowed down to see messages
            
            // Build modules if needed (moved from shell script to JavaFX app)
            updateProgress(2, "Building modules (checking for changes)...");
            Thread.sleep(2000); // DEVELOPMENT: Slowed down to see messages
            
            if (needToBuildModules()) {
                updateProgress(2, "Building modules (incremental)...");
                Thread.sleep(2000); // DEVELOPMENT: Slowed down to see messages
                
                // Build GDK first (if needed)
                if (!new File("../gdk/target/classes").exists()) {
                    updateProgress(2, "Building GDK...");
                    Thread.sleep(1500); // DEVELOPMENT: Slowed down to see messages
                    buildModule("../gdk");
                }
                
                // Build launcher
                updateProgress(2, "Building launcher...");
                Thread.sleep(1500); // DEVELOPMENT: Slowed down to see messages
                buildModule(".");
            } else {
                updateProgress(2, "Using existing builds (recent compilation detected)");
                Thread.sleep(2000); // DEVELOPMENT: Slowed down to see messages
            }
            
            // Now trigger module loading with progress window ready
            if (lobbyController != null) {
                updateProgress(2, "Loading available game modules...");
                Thread.sleep(2500); // DEVELOPMENT: Slowed down to see messages
                
                // Additional delay to ensure pre-startup window is fully visible
                Thread.sleep(2000); // DEVELOPMENT: Ensure window is visible before module loading
                
                // Trigger module discovery and loading
                lobbyController.refreshAvailableGameModulesFast();
            }
            
            // Get and display discovered modules
            List<GameModule> discoveredModules = ModuleLoader.getDiscoveredModules();
            if (!discoveredModules.isEmpty()) {
                StringBuilder moduleList = new StringBuilder();
                for (int i = 0; i < discoveredModules.size(); i++) {
                    if (i > 0) moduleList.append(", ");
                    moduleList.append(discoveredModules.get(i).getGameName());
                }
                updateProgress(2, "Loaded modules: " + moduleList.toString());
                Thread.sleep(3000); // DEVELOPMENT: Slowed down to see module list
            } else {
                updateProgress(2, "No game modules found");
                Thread.sleep(1500); // DEVELOPMENT: Slowed down to see message
            }
            
            // Now check for compilation failures after modules are loaded
            if (lobbyController != null) {
                updateProgress(2, "Checking module compilation...");
                Thread.sleep(1000); // DEVELOPMENT: Slowed down to see message
                
                // Check for compilation failures
                lobbyController.checkStartupCompilationFailures();
                
                // Clear startup progress window reference after startup is complete
                lobbyController.clearStartupProgressWindow();
            }
            
            // Additional wait for any background processes
            updateProgress(2, "Finalizing UI components...");
            Thread.sleep(2000); // DEVELOPMENT: Slowed down to see messages
            
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
            // Check if launcher classes exist (minimal check)
            if (!new File("target/classes").exists()) {
                return true;
            }
            
            // Simple check: if launcher target directory is recent, skip build
            long currentTime = System.currentTimeMillis();
            long launcherAge = currentTime - new File("target/classes").lastModified();
            
            // If launcher is less than 5 minutes old, assume no changes
            return launcherAge >= 300000; // 5 minutes = 300000ms
        } catch (Exception e) {
            Logging.error("❌ Error checking if modules need to be built: " + e.getMessage(), e);
            return true; // Build on error to be safe
        }
    }
    
    /**
     * Build a module using Maven
     * @param modulePath The path to the module directory
     */
    private void buildModule(String modulePath) {
        try {
            Logging.info("🔨 Building module: " + modulePath);
            
            // Create process builder for Maven command
            ProcessBuilder pb = new ProcessBuilder("mvn", "compile", "-DskipTests", "-q");
            pb.directory(new File(modulePath));
            
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
} 