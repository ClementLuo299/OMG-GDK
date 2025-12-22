package launcher.lifecycle.start;

import gdk.api.GameModule;
import gdk.internal.Logging;
import javafx.application.Platform;
import javafx.stage.Stage;
import launcher.gui.GDKGameLobbyController;
import launcher.utils.module.ModuleCompiler;
import launcher.utils.module.ModuleDiscovery;
import launcher.utils.path.PathUtil;
import launcher.lifecycle.start.startup_window.StartupWindowManager;
import launcher.utils.StartupDelayUtil;
import launcher.utils.StartupTransitionUtil;

import javax.swing.SwingUtilities;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * Handles the background thread that loads game modules during startup.
 * 
 * The thread performs these steps:
 * 1. Loads all game modules (discovery, compilation, loading)
 * 2. Updates the UI with loaded games (on JavaFX thread)
 * 3. Checks for compilation issues
 * 4. Marks startup as complete
 * 5. Shows the main stage and hides the startup window
 * 
 * @author Clement Luo
 * @date December 21, 2025
 * @edited December 21, 2025
 * @since Beta 1.0
 */
public final class ModuleLoadingThread {

    private ModuleLoadingThread() {}

    /**
     * Creates and configures the background thread that loads modules.
     * 
     * @param primaryApplicationStage The primary stage to show when ready
     * @param lobbyController The controller to update with loaded games
     * @param windowManager The progress window manager
     * @param totalSteps Total number of progress steps
     * @return The configured but not-yet-started thread
     */
    public static Thread create(Stage primaryApplicationStage,
                                GDKGameLobbyController lobbyController, 
                                StartupWindowManager windowManager, 
                                int totalSteps) {
        return new Thread(() -> {
            try {
                Logging.info("Starting module loading on background thread");
                
                // PHASE 1: Load all game modules
                loadModulesWithProgress(windowManager, totalSteps);
                Logging.info("Module loading completed");
                StartupDelayUtil.addDevelopmentDelay("After module loading process completed");
                
                // PHASE 2: Update UI with loaded games (must run on JavaFX thread)
                updateUIWithLoadedGames(lobbyController);
                StartupDelayUtil.addDevelopmentDelay("After refreshing game modules");
                
                // PHASE 3: Check for compilation issues
                checkForCompilationIssues(lobbyController, windowManager, totalSteps);
                StartupDelayUtil.addDevelopmentDelay("After checking for compilation issues");
                
                // PHASE 4: Mark startup as complete
                markStartupComplete(windowManager, totalSteps);
                StartupDelayUtil.addDevelopmentDelay("After startup complete and ready");
                
                // PHASE 5: Show main stage and hide startup window
                Logging.info("All startup tasks complete - showing main stage");
                StartupTransitionUtil.showMainStage(primaryApplicationStage, windowManager);
                
                Logging.info("Module loading thread completed successfully");
                
            } catch (Exception e) {
                handleModuleLoadingError(e, primaryApplicationStage, windowManager);
            }
        });
    }
    
    /**
     * Handles errors that occur during module loading.
     * Attempts to show the main stage even if errors occurred.
     * 
     * @param error The exception that occurred
     * @param primaryApplicationStage The primary stage to show
     * @param windowManager The startup window manager
     */
    private static void handleModuleLoadingError(Exception error, 
                                                 Stage primaryApplicationStage, 
                                                 StartupWindowManager windowManager) {
        Logging.error("💥 Critical error in module loading thread: " + error.getMessage(), error);
        
        // Even on error, try to show the main stage
        try {
            StartupTransitionUtil.showMainStage(primaryApplicationStage, windowManager);
        } catch (Exception showError) {
            Logging.error("❌ Failed to show main stage after error: " + showError.getMessage());
        }
    }
    
    /**
     * Updates the UI with the loaded game modules.
     * This must run on the JavaFX Application Thread.
     * 
     * @param lobbyController The controller to refresh with loaded games
     */
    private static void updateUIWithLoadedGames(GDKGameLobbyController lobbyController) {
        Logging.info("🔄 Scheduling UI refresh on JavaFX thread...");
        Platform.runLater(() -> {
            try {
                Logging.info("🔄 Refreshing available game modules in UI...");
                if (lobbyController != null) {
                    lobbyController.refreshAvailableGameModulesFast();
                    Logging.info("✅ UI refreshed with loaded games");
                } else {
                    Logging.error("❌ Lobby controller is null - cannot refresh UI!");
                }
            } catch (Exception e) {
                Logging.error("❌ Error refreshing game modules: " + e.getMessage(), e);
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Checks for compilation failures and displays warnings if needed.
     * The progress update must run on the Swing EDT, but the controller check
     * must run on the JavaFX thread.
     * 
     * @param lobbyController The controller to check for issues
     * @param windowManager The progress window to update (Swing component)
     * @param totalSteps Total number of progress steps
     */
    private static void checkForCompilationIssues(GDKGameLobbyController lobbyController, 
                                                  StartupWindowManager windowManager, 
                                                  int totalSteps) {
        // Update progress window (Swing component - needs EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                windowManager.updateProgress(totalSteps - 2, "Checking for compilation issues...");
            } catch (Exception e) {
                Logging.error("❌ Error updating progress for compilation check: " + e.getMessage());
            }
        });
        StartupDelayUtil.addDevelopmentDelay("After 'Checking for compilation issues...' message");
        
        // Check compilation issues (JavaFX component - needs JavaFX thread)
        Platform.runLater(() -> {
            try {
                if (lobbyController != null) {
                    lobbyController.checkStartupCompilationFailures();
                }
            } catch (Exception e) {
                Logging.error("❌ Error checking compilation issues: " + e.getMessage());
            }
        });
    }
    
    /**
     * Marks the startup process as complete in the progress window.
     * This must run on the Swing Event Dispatch Thread (EDT) since
     * StartupWindowManager is a Swing component.
     * 
     * @param windowManager The progress window to update (Swing component)
     * @param totalSteps Total number of progress steps
     */
    private static void markStartupComplete(StartupWindowManager windowManager, int totalSteps) {
        SwingUtilities.invokeLater(() -> {
            windowManager.updateProgress(totalSteps - 1, "Startup complete");
        });
        StartupDelayUtil.addDevelopmentDelay("After 'Startup complete' message");
        
        SwingUtilities.invokeLater(() -> {
            windowManager.updateProgress(totalSteps, "Ready!");
        });
        StartupDelayUtil.addDevelopmentDelay("After 'Ready!' message");
    }

    /**
     * Loads game modules with progress updates.
     * Called from the module loading thread.
     * 
     * @param windowManager The startup window manager for progress updates
     * @param totalSteps The total number of steps in the startup process
     */
    private static void loadModulesWithProgress(StartupWindowManager windowManager, int totalSteps) {
        int currentStep = 1; // Start from step 1 since we removed the fake progress updates
        
        try {
            Logging.info("🚀 Starting module loading process with " + totalSteps + " total steps");
            final int initStep = currentStep++;
            SwingUtilities.invokeLater(() -> {
                windowManager.updateProgress(initStep, "Initializing game modules...");
            });
            StartupDelayUtil.addDevelopmentDelay("After initializing game modules");
            
            if (ModuleCompiler.needToBuildModules()) {
                Logging.info("🔨 Modules need to be built");
                final int step = currentStep++;
                SwingUtilities.invokeLater(() -> {
                    windowManager.updateProgress(step, "Building modules...");
                });
                StartupDelayUtil.addDevelopmentDelay("After checking if modules need to be built");
            } else {
                Logging.info("✅ Using existing builds (recent compilation detected)");
                final int step = currentStep++;
                SwingUtilities.invokeLater(() -> {
                    windowManager.updateProgress(step, "Using existing builds (recent compilation detected)");
                });
                StartupDelayUtil.addDevelopmentDelay("After checking if modules need to be built");
            }
            
            Logging.info("📁 Preparing module discovery...");
            final int step1 = currentStep++;
            SwingUtilities.invokeLater(() -> {
                windowManager.updateProgress(step1, "Preparing module discovery...");
            });
            StartupDelayUtil.addDevelopmentDelay("After preparing module discovery");

            String modulesDirectoryPath = PathUtil.getModulesDirectoryPath();
            Logging.info("🔍 Modules directory path: " + modulesDirectoryPath);
            File modulesDirectory = new File(modulesDirectoryPath);

            if (!modulesDirectory.exists()) {
                Logging.warning("❌ Modules directory does not exist: " + modulesDirectoryPath);
                final int step = currentStep++;
                SwingUtilities.invokeLater(() -> {
                    windowManager.updateProgress(step, "Modules directory not found: " + modulesDirectoryPath);
                });
                StartupDelayUtil.addDevelopmentDelay("After 'Modules directory not found' message");
                
                // Run diagnostics to help identify the issue
                Logging.info("🔍 Running module detection diagnostics...");
                ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
                
            } else {
                Logging.info("✅ Modules directory exists: " + modulesDirectory.getAbsolutePath());
                
                // Quick test of directory access before attempting discovery
                Logging.info("🧪 Testing modules directory access...");
                if (!ModuleDiscovery.testModulesDirectoryAccess(modulesDirectoryPath)) {
                    Logging.error("❌ Modules directory access test failed - skipping module discovery");
                    final int step = currentStep++;
                    SwingUtilities.invokeLater(() -> {
                        windowManager.updateProgress(step, "Modules directory access failed - continuing without modules");
                    });
                    StartupDelayUtil.addDevelopmentDelay("After 'Modules directory access failed' message");
                    
                    // Run diagnostics to help identify the issue
                    Logging.info("🔍 Running module detection diagnostics...");
                    ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
                    
                } else {
                    Logging.info("✅ Modules directory access test passed - proceeding with discovery");
                    final int step = currentStep++;
                    SwingUtilities.invokeLater(() -> {
                        windowManager.updateProgress(step, "Discovering modules...");
                    });
                    StartupDelayUtil.addDevelopmentDelay("After 'Discovering modules...' message");
                    
                    // Add timeout protection for module discovery
                    List<File> validModuleDirectories = new ArrayList<>();
                    try {
                        Logging.info("🔍 Starting module discovery...");
                        validModuleDirectories = ModuleDiscovery.getValidModuleDirectories(modulesDirectory);
                        Logging.info("✅ Module discovery completed. Found " + validModuleDirectories.size() + " valid modules");
                        StartupDelayUtil.addDevelopmentDelay("After module discovery completed - found " + validModuleDirectories.size() + " modules");
                        
                        // If no modules found, run diagnostics
                        if (validModuleDirectories.isEmpty()) {
                            Logging.warning("⚠️ No valid modules found - running diagnostics...");
                            ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
                            
                            // Also check compilation status
                            Logging.info("🔍 Checking module compilation status...");
                            ModuleDiscovery.reportModuleCompilationStatus(modulesDirectory);
                        }
                        
                    } catch (Exception e) {
                        Logging.error("❌ Module discovery failed: " + e.getMessage(), e);
                        e.printStackTrace(); // Print full stack trace for debugging
                        final int discoveryErrorStep = currentStep++;
                        SwingUtilities.invokeLater(() -> {
                            windowManager.updateProgress(discoveryErrorStep, "Module discovery failed - continuing with empty list");
                        });
                        
                        // Run diagnostics to help identify the issue
                        Logging.info("🔍 Running module detection diagnostics...");
                        ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
                        StartupDelayUtil.addDevelopmentDelay("After module discovery failure");
                    }
                    
                    // Process each discovered module
                    for (File moduleDir : validModuleDirectories) {
                        if (currentStep >= totalSteps - 2) break; // Reserve last 2 steps for finalization
                        String moduleName = moduleDir.getName();
                        Logging.info("⚙️ Processing module: " + moduleName);
                        final int processingStep = currentStep++;
                        final String finalModuleName = moduleName; // Need final for lambda
                        SwingUtilities.invokeLater(() -> {
                            windowManager.updateProgress(processingStep, "Processing module: " + finalModuleName);
                        });
                        StartupDelayUtil.addDevelopmentDelay("After processing module: " + moduleName);
                    }
                    
                    // Load the discovered modules
                    List<GameModule> discoveredModules = new ArrayList<>();
                    try {
                        Logging.info("📦 Starting module loading...");
                        final int loadingStep = currentStep++;
                        SwingUtilities.invokeLater(() -> {
                            windowManager.updateProgress(loadingStep, "Loading compiled modules...");
                        });
                        discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
                        Logging.info("✅ Module loading completed. Successfully loaded " + discoveredModules.size() + " modules");
                        if (discoveredModules.isEmpty()) {
                            Logging.warning("⚠️ No modules were loaded! Check module compilation status.");
                        }
                        StartupDelayUtil.addDevelopmentDelay("After loading compiled modules - loaded " + discoveredModules.size() + " modules");
                    } catch (Exception e) {
                        Logging.error("❌ Module loading failed: " + e.getMessage(), e);
                        e.printStackTrace(); // Print full stack trace for debugging
                        final int loadingErrorStep = currentStep++;
                        SwingUtilities.invokeLater(() -> {
                            windowManager.updateProgress(loadingErrorStep, "Module loading failed - continuing with empty list");
                        });
                        StartupDelayUtil.addDevelopmentDelay("After module loading failure");
                    }
                    
                    final int finalStep = currentStep++;
                    final int moduleCount = discoveredModules.size();
                    SwingUtilities.invokeLater(() -> {
                        windowManager.updateProgress(finalStep, "Found " + moduleCount + " modules");
                    });
                    StartupDelayUtil.addDevelopmentDelay("After reporting module count");
                }
            }
            
            Logging.info("🏁 Finalizing module loading...");
            final int finalStep = currentStep++;
            SwingUtilities.invokeLater(() -> {
                windowManager.updateProgress(finalStep, "Finalizing module loading...");
            });
            StartupDelayUtil.addDevelopmentDelay("After finalizing module loading");
            
        } catch (Exception e) {
            Logging.error("💥 Critical error during module loading: " + e.getMessage(), e);
            e.printStackTrace(); // Print full stack trace for debugging
            final int errorStep = currentStep;
            SwingUtilities.invokeLater(() -> {
                windowManager.updateProgress(errorStep, "Error during module loading - continuing...");
            });
            StartupDelayUtil.addDevelopmentDelay("After 'Error during module loading' message");
        }
    }
}

