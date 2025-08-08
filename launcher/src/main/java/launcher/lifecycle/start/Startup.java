package launcher.lifecycle.start;

import gdk.Logging;
import gdk.GameModule;

import launcher.utils.ModuleLoader;
import launcher.gui.GDKGameLobbyController;
import launcher.gui.GDKViewModel;
import launcher.GDKApplication;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform; 

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

/**
 * Handles the main application startup logic, including UI initialization and module loading.
 * This class orchestrates the entire startup sequence, ensuring all components are properly
 * initialized in the correct order.
 *
 * @authors Clement Luo
 * @date July 20, 2025
 * @edited August 8, 2025
 * @since 1.0
 */
public class Startup {

    // ==================== STATIC STARTUP METHOD ====================
    
    /**
     * Execute the complete GDK startup process.
     * 
     * This method orchestrates the entire startup sequence, ensuring
     * that all components are properly initialized in the correct order.
     * If any step fails, the entire startup process is aborted with
     * appropriate error handling.
     * 
     * @param primaryApplicationStage The primary stage for the application
     */
    public static void start(Stage primaryApplicationStage) {
        Logging.info("Starting GDK application startup process");
        
        // Create and manage the pre-startup progress window
        PreStartupProgressWindow progressWindow = new PreStartupProgressWindow();
        StartupWindowManager windowManager = new StartupWindowManager(progressWindow);
        
        // Quick module verification to determine number of steps
        int totalSteps = calculateTotalSteps();
        windowManager.setTotalSteps(totalSteps);
        Logging.info("📊 Determined " + totalSteps + " total steps based on module count");
        
        // Show the window and start with initial progress
        windowManager.show();
        windowManager.updateProgress(0, "Starting GDK application...");
        
        try {
            // Step 1: Initialize the main user interface (this includes FXML loading and controller init)
            updateProgress(1, "Loading user interface...", windowManager);
            GDKGameLobbyController[] controllerHolder = new GDKGameLobbyController[1];
            Scene mainLobbyScene = initializeMainUserInterface(primaryApplicationStage, controllerHolder);
            GDKGameLobbyController lobbyController = controllerHolder[0];
            
            // Step 2: Create and configure the ViewModel (quick step, no progress update)
            GDKViewModel applicationViewModel = initializeApplicationViewModel(primaryApplicationStage);
            
            // Step 3: Configure the primary application stage (quick step, no progress update)
            configurePrimaryApplicationStage(primaryApplicationStage, mainLobbyScene);
            
            // Step 4: Wire up the controller with the ViewModel (quick step, no progress update)
            wireUpControllerWithViewModel(mainLobbyScene, applicationViewModel, lobbyController);
            
            // Step 2: Ensure UI is ready and show application (this takes time)
            updateProgress(2, "Preparing application...", windowManager);
            
            // Ensure the UI is fully ready before showing the main window
            ensureUIReady(primaryApplicationStage, lobbyController, windowManager, totalSteps);
            
            // Hide the progress window FIRST
            Logging.info("🏁 Hiding progress window...");
            hideProgressWindow(windowManager);
            
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
            
            // Show error in pre-startup progress window before hiding
            if (windowManager != null) {
                windowManager.updateProgress(5, "Startup failed - check error messages");
                
                // Keep progress window visible for a moment to show error
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                windowManager.hide();
            }
            
            throw new RuntimeException("Failed to start GDK application", startupError);
        }
        }
    
    // ==================== MODULE VERIFICATION ====================
    
    /**
     * Calculate the total number of steps needed for startup based on module count.
     * 
     * This method does a quick verification of modules to determine how many
     * valid modules exist, which affects the total number of progress steps.
     * 
     * @return The total number of steps needed
     */
    private static int calculateTotalSteps() {
        Logging.info("🔍 Calculating total steps based on module verification...");
        
        try {
            // Base steps: UI initialization, preparation, finalization
            int baseSteps = 5; // Starting, UI loading, preparation, finalization, ready
            
            // Check if modules directory exists
            String modulesDirectoryPath = GDKApplication.MODULES_DIRECTORY_PATH;
            File modulesDirectory = new File(modulesDirectoryPath);
            
            if (!modulesDirectory.exists()) {
                Logging.info("📁 Modules directory not found, using base steps only");
                return baseSteps;
            }
            
            // Count valid modules
            int validModuleCount = countValidModules(modulesDirectory);
            Logging.info("📦 Found " + validModuleCount + " valid modules");
            
            // Each valid module adds 1 step for processing
            int totalSteps = baseSteps + validModuleCount;
            
            // Ensure minimum of 5 steps and maximum of 50 steps
            totalSteps = Math.max(5, Math.min(50, totalSteps));
            
            return totalSteps;
        } catch (Exception e) {
            Logging.error("❌ Error calculating total steps: " + e.getMessage(), e);
            return 10; // Default fallback
        }
    }
    
    /**
     * Count the number of valid modules in the modules directory.
     * 
     * A module is considered valid if it contains both Main and Metadata classes
     * that implement the required methods.
     * 
     * @param modulesDirectory The modules directory to scan
     * @return The number of valid modules found
     */
    private static int countValidModules(File modulesDirectory) {
        int validCount = 0;
        
        try {
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return 0;
            }
            
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                
                // Skip non-module directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    continue;
                }
                
                // Check if this is a valid module
                if (isValidModule(subdir)) {
                    validCount++;
                    Logging.info("✅ Valid module found: " + moduleName);
                } else {
                    Logging.info("⚠️ Invalid module found: " + moduleName);
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error counting valid modules: " + e.getMessage(), e);
        }
        
        return validCount;
    }
    
    /**
     * Check if a directory contains a valid module.
     * 
     * A valid module must have:
     * 1. A Main.java file with required methods
     * 2. A Metadata.java file with required methods
     * 
     * @param moduleDir The module directory to check
     * @return true if the module is valid, false otherwise
     */
    private static boolean isValidModule(File moduleDir) {
        try {
            // Check for Main.java
            File mainJavaFile = new File(moduleDir, "src/main/java/Main.java");
            if (!mainJavaFile.exists()) {
                return false;
            }
            
            // Check for Metadata.java
            File metadataJavaFile = new File(moduleDir, "src/main/java/Metadata.java");
            if (!metadataJavaFile.exists()) {
                return false;
            }
            
            // Basic validation - check if files contain required method signatures
            // This is a quick check without full compilation
            String mainContent = java.nio.file.Files.readString(mainJavaFile.toPath());
            String metadataContent = java.nio.file.Files.readString(metadataJavaFile.toPath());
            
            // Check Main.java for required methods
            boolean hasMainMethod = mainContent.contains("public static void main(String[] args)");
            boolean hasGetGameName = mainContent.contains("public String getGameName()") || 
                                   mainContent.contains("public static String getGameName()");
            
            // Check Metadata.java for required methods
            boolean hasGetGameNameInMetadata = metadataContent.contains("public String getGameName()") || 
                                             metadataContent.contains("public static String getGameName()");
            boolean hasGetVersion = metadataContent.contains("public String getVersion()") || 
                                  metadataContent.contains("public static String getVersion()");
            boolean hasGetDescription = metadataContent.contains("public String getDescription()") || 
                                      metadataContent.contains("public static String getDescription()");
            
            // Module is valid if it has all required methods
            boolean isValid = hasMainMethod && hasGetGameName && hasGetGameNameInMetadata && hasGetVersion && hasGetDescription;
            
            if (!isValid) {
                Logging.info("⚠️ Module " + moduleDir.getName() + " missing required methods");
            }
            
            return isValid;
        } catch (Exception e) {
            Logging.error("❌ Error validating module " + moduleDir.getName() + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== USER INTERFACE INITIALIZATION ====================
    
    /**
     * Initialize the main user interface components.
     * 
     * This method loads the FXML layout, applies CSS styling, and
     * sets up the lobby controller with necessary references.
     * 
     * @param primaryApplicationStage The primary stage for the application
     * @param controllerHolder Array to hold the controller reference
     * @return The initialized Scene object
     */
    private static Scene initializeMainUserInterface(Stage primaryApplicationStage, GDKGameLobbyController[] controllerHolder) {
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
            GDKGameLobbyController lobbyController = fxmlLoader.getController();
            controllerHolder[0] = lobbyController;
            
            // Apply CSS styling to the scene
            URL cssResourceUrl = GDKApplication.class.getResource("/gdk-lobby/gdk-lobby.css");
            if (cssResourceUrl != null) {
                mainLobbyScene.getStylesheets().add(cssResourceUrl.toExternalForm());
                Logging.info("📂 CSS styling loaded from: " + cssResourceUrl);
            }
            
            // Set up the lobby controller with necessary references
            if (lobbyController != null) {
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
     * @param primaryApplicationStage The primary stage for the application
     * @return The configured ViewModel instance
     */
    private static GDKViewModel initializeApplicationViewModel(Stage primaryApplicationStage) {
        Logging.info("🧠 Initializing application ViewModel");
        
        try {
            // Create a new ViewModel instance with the module loader
            GDKViewModel applicationViewModel = new GDKViewModel(null);
            
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
     * @param primaryApplicationStage The primary stage for the application
     * @param mainLobbyScene The scene to set on the stage
     */
    private static void configurePrimaryApplicationStage(Stage primaryApplicationStage, Scene mainLobbyScene) {
        Logging.info("⚙️ Configuring primary application stage");
        
        try {
            // Set the scene on the stage
            primaryApplicationStage.setScene(mainLobbyScene);
            
            // Configure stage properties
            primaryApplicationStage.setTitle("OMG Game Development Kit (GDK)");
            primaryApplicationStage.setMinWidth(800);
            primaryApplicationStage.setMinHeight(600);
            primaryApplicationStage.setWidth(1200);
            primaryApplicationStage.setHeight(900);
            
            // Start with opacity 0 for fade-in effect
            primaryApplicationStage.setOpacity(0.0);
            
            Logging.info("✅ Primary application stage configured successfully");
        } catch (Exception stageConfigurationError) {
            Logging.error("❌ Failed to configure primary application stage: " + stageConfigurationError.getMessage(), stageConfigurationError);
            throw new RuntimeException("Failed to configure primary application stage", stageConfigurationError);
        }
    }
    
    // ==================== CONTROLLER-VIEWMODEL WIRING ====================
    
    /**
     * Wire up the controller with the ViewModel for proper communication.
     * 
     * This method establishes the connection between the UI controller
     * and the business logic ViewModel.
     * 
     * @param mainLobbyScene The main lobby scene
     * @param applicationViewModel The application ViewModel
     * @param lobbyController The lobby controller
     */
    private static void wireUpControllerWithViewModel(Scene mainLobbyScene, GDKViewModel applicationViewModel, GDKGameLobbyController lobbyController) {
        Logging.info("🔌 Wiring up controller with ViewModel");
        
        try {
            // Set the ViewModel on the controller
            if (lobbyController != null) {
                lobbyController.setViewModel(applicationViewModel);
                Logging.info("✅ Controller wired up with ViewModel successfully");
            } else {
                throw new RuntimeException("Lobby controller is null - cannot wire up ViewModel");
            }
        } catch (Exception wiringError) {
            Logging.error("❌ Failed to wire up controller with ViewModel: " + wiringError.getMessage(), wiringError);
            throw new RuntimeException("Failed to wire up controller with ViewModel", wiringError);
        }
    }
    
    // ==================== PROGRESS MANAGEMENT ====================
    
    /**
     * Update the progress window with current step and status.
     * 
     * @param step The current step number
     * @param status The status message
     * @param preProgressWindow The progress window to update
     */
    private static void updateProgress(int step, String status, StartupWindowManager windowManager) {
        if (windowManager != null) {
            windowManager.updateProgress(step, status);
        }
    }
    
    /**
     * Update progress with module-specific information.
     * 
     * @param moduleName The name of the module being processed
     * @param preProgressWindow The progress window to update
     */

    
    // ==================== MODULE LOADING ====================
    
    /**
     * Load modules with progress updates.
     * 
     * @param preProgressWindow The progress window for updates
     * @param totalSteps The total number of steps for progress tracking
     */
    private static void loadModulesWithProgress(StartupWindowManager windowManager, int totalSteps) {
        Logging.info("📦 Loading game modules with progress tracking");
        
        try {
            // Note: Progress window updates are now handled directly by PreStartupProgressWindow
            
            // Load modules
            updateProgress(3, "Initializing game modules...", windowManager);
            
            // Check if we need to build modules
            if (needToBuildModules()) {
                updateProgress(4, "Building modules...", windowManager);
                // Build modules logic would go here
            } else {
                updateProgress(4, "Using existing builds (recent compilation detected)", windowManager);
            }
            
            updateProgress(5, "Preparing module discovery...", windowManager);
            
            // Discover and process modules individually
            String modulesDirectoryPath = GDKApplication.MODULES_DIRECTORY_PATH;
            File modulesDirectory = new File(modulesDirectoryPath);
            
            int currentStep = 6;
            
            if (!modulesDirectory.exists()) {
                updateProgress(Math.min(currentStep, totalSteps - 3), "Modules directory not found", windowManager);
                Logging.info("⚠️ Modules directory does not exist: " + modulesDirectoryPath);
                currentStep++;
            } else {
                updateProgress(Math.min(currentStep, totalSteps - 3), "Discovering modules...", windowManager);
                currentStep++;
                
                // Get list of valid modules for individual processing
                List<File> validModules = getValidModuleDirectories(modulesDirectory);
                
                // Process each valid module individually (but respect total steps)
                for (File moduleDir : validModules) {
                    if (currentStep >= totalSteps - 3) break; // Stop if we're approaching the end
                    
                    String moduleName = moduleDir.getName();
                    updateProgress(Math.min(currentStep, totalSteps - 3), "Processing module: " + moduleName, windowManager);
                    
                    // Simulate module processing time
                    Thread.sleep(500); // Brief pause to show progress
                    
                    currentStep++;
                }
                
                // Discover modules using ModuleLoader
                List<GameModule> discoveredModules = ModuleLoader.discoverModules(modulesDirectoryPath);
                updateProgress(Math.min(currentStep, totalSteps - 3), "Found " + discoveredModules.size() + " modules", windowManager);
                currentStep++;
            }
            
            // Finalize module loading
            updateProgress(Math.min(currentStep, totalSteps - 3), "Finalizing module loading...", windowManager);
            
            // Clear startup progress window reference
            ModuleLoader.clearStartupProgressWindow();
            
            Logging.info("✅ Module loading completed successfully");
        } catch (Exception moduleLoadingError) {
            Logging.error("❌ Failed to load modules: " + moduleLoadingError.getMessage(), moduleLoadingError);
            throw new RuntimeException("Failed to load modules", moduleLoadingError);
        }
    }
    
    /**
     * Get list of valid module directories for individual processing.
     * 
     * @param modulesDirectory The modules directory to scan
     * @return List of valid module directories
     */
    private static List<File> getValidModuleDirectories(File modulesDirectory) {
        List<File> validModules = new ArrayList<>();
        
        try {
            File[] subdirs = modulesDirectory.listFiles(File::isDirectory);
            if (subdirs == null) {
                return validModules;
            }
            
            for (File subdir : subdirs) {
                String moduleName = subdir.getName();
                
                // Skip non-module directories
                if (moduleName.equals("target") || moduleName.equals(".git") || moduleName.startsWith(".")) {
                    continue;
                }
                
                // Check if this is a valid module
                if (isValidModule(subdir)) {
                    validModules.add(subdir);
                }
            }
        } catch (Exception e) {
            Logging.error("❌ Error getting valid module directories: " + e.getMessage(), e);
        }
        
        return validModules;
    }
    
    // ==================== PROGRESS WINDOW MANAGEMENT ====================
    
    /**
     * Hide the progress window.
     * 
     * @param preProgressWindow The progress window to hide
     */
    private static void hideProgressWindow(StartupWindowManager windowManager) {
        if (windowManager != null) {
            windowManager.hide();
        }
    }
    
    // ==================== UI READINESS ====================
    
    /**
     * Ensure the UI is fully ready for user interaction.
     * 
     * This method performs final initialization steps and ensures
     * all components are properly set up before showing the main window.
     * 
     * @param primaryApplicationStage The primary stage for the application
     * @param lobbyController The lobby controller
     * @param preProgressWindow The progress window for updates
     * @param totalSteps The total number of steps for progress tracking
     */
    private static void ensureUIReady(Stage primaryApplicationStage, GDKGameLobbyController lobbyController, StartupWindowManager windowManager, int totalSteps) {
        Logging.info("🔧 Ensuring UI is fully ready...");
        
        try {
            // Load modules with progress tracking
            loadModulesWithProgress(windowManager, totalSteps);
            
            // Check for compilation failures on startup
            updateProgress(totalSteps - 2, "Checking for compilation issues...", windowManager);
            
            // Use Platform.runLater to ensure we're on the JavaFX thread
            Platform.runLater(() -> {
                try {
                    Logging.info("🚀 Checking for compilation failures on startup...");
                    
                    // Check for compilation failures
                    if (lobbyController != null) {
                        lobbyController.checkStartupCompilationFailures();
                    }
                    
                    Logging.info("✅ Startup compilation failure check completed");
                } catch (Exception compilationCheckError) {
                    Logging.error("❌ Failed to check compilation failures: " + compilationCheckError.getMessage(), compilationCheckError);
                }
            });
            
            // Final progress update
            updateProgress(totalSteps - 1, "Startup complete", windowManager);
            updateProgress(totalSteps, "Ready!", windowManager);
            
            // Wait a moment for final progress update
            Thread.sleep(5000); // DEVELOPMENT: Wait for final progress
            
            Logging.info("✅ UI is fully ready for user interaction");
        } catch (Exception uiReadyError) {
            Logging.error("❌ Failed to ensure UI readiness: " + uiReadyError.getMessage(), uiReadyError);
            throw new RuntimeException("Failed to ensure UI readiness", uiReadyError);
        }
    }
    
    // ==================== MODULE BUILDING ====================
    
    /**
     * Check if modules need to be built.
     * 
     * @return true if modules need to be built, false otherwise
     */
    private static boolean needToBuildModules() {
        // For now, always return false to avoid Maven execution issues
        // This can be enhanced later with proper build detection logic
        return false;
    }
    
    /**
     * Build a specific module.
     * 
     * @param modulePath The path to the module to build
     */
    private static void buildModule(String modulePath) {
        Logging.info("🔨 Building module: " + modulePath);
        
        try {
            // Find Maven command
            String mavenCommand = findMavenCommand();
            
            // Build the module
            ProcessBuilder processBuilder = new ProcessBuilder(mavenCommand, "clean", "compile");
            processBuilder.directory(new File(modulePath));
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                Logging.info("✅ Module built successfully: " + modulePath);
            } else {
                Logging.info("⚠️ Module build completed with warnings: " + modulePath);
            }
        } catch (Exception buildError) {
            Logging.error("❌ Failed to build module " + modulePath + ": " + buildError.getMessage(), buildError);
        }
    }
    
    /**
     * Find the Maven command to use.
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