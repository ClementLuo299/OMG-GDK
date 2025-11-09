package launcher.lifecycle.start;

import gdk.api.GameModule;
import gdk.infrastructure.Logging;
import javafx.application.Platform;
import javafx.stage.Stage;
import launcher.GDKApplication;
import launcher.gui.GDKGameLobbyController;
import launcher.utils.ModuleCompiler;
import launcher.utils.ModuleDiscovery;
import launcher.lifecycle.start.startup_window.StartupWindowManager;
import launcher.lifecycle.stop.Shutdown;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * Encapsulates operational startup tasks (module loading, readiness checks, and stage display).
 * 
 * @author Clement Luo
 * @date August 9, 2025
 * @edited August 13, 2025
 * @since 1.0
 */
public final class StartupOperations {

    private StartupOperations() {}

    public static void loadModulesWithProgress(StartupWindowManager windowManager, int totalSteps) {
        int currentStep = 6; // Move variable declaration to method scope
        
        try {
            Logging.info("🚀 Starting module loading process with " + totalSteps + " total steps");
            windowManager.updateProgress(3, "Initializing game modules...");
            
            if (ModuleCompiler.needToBuildModules()) {
                Logging.info("🔨 Modules need to be built");
                windowManager.updateProgress(4, "Building modules...");
            } else {
                Logging.info("✅ Using existing builds (recent compilation detected)");
                windowManager.updateProgress(4, "Using existing builds (recent compilation detected)");
            }
            
            Logging.info("📁 Preparing module discovery...");
            windowManager.updateProgress(5, "Preparing module discovery...");

            String modulesDirectoryPath = GDKApplication.getModulesDirectoryPath();
            Logging.info("🔍 Modules directory path: " + modulesDirectoryPath);
            File modulesDirectory = new File(modulesDirectoryPath);

            if (!modulesDirectory.exists()) {
                Logging.warning("❌ Modules directory does not exist: " + modulesDirectoryPath);
                windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Modules directory not found: " + modulesDirectoryPath);
                
                // Run diagnostics to help identify the issue
                Logging.info("🔍 Running module detection diagnostics...");
                ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
                
            } else {
                Logging.info("✅ Modules directory exists: " + modulesDirectory.getAbsolutePath());
                
                // Quick test of directory access before attempting discovery
                Logging.info("🧪 Testing modules directory access...");
                if (!ModuleDiscovery.testModulesDirectoryAccess(modulesDirectoryPath)) {
                    Logging.error("❌ Modules directory access test failed - skipping module discovery");
                    windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Modules directory access failed - continuing without modules");
                    
                    // Run diagnostics to help identify the issue
                    Logging.info("🔍 Running module detection diagnostics...");
                    ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
                    
                } else {
                    Logging.info("✅ Modules directory access test passed - proceeding with discovery");
                    windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Discovering modules...");
                    
                    // Add timeout protection for module discovery
                    List<File> validModuleDirectories = new ArrayList<>();
                    try {
                        Logging.info("🔍 Starting module discovery...");
                        validModuleDirectories = ModuleDiscovery.getValidModuleDirectories(modulesDirectory);
                        Logging.info("✅ Module discovery completed. Found " + validModuleDirectories.size() + " valid modules");
                        
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
                        windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Module discovery failed - continuing with empty list");
                        
                        // Run diagnostics to help identify the issue
                        Logging.info("🔍 Running module detection diagnostics...");
                        ModuleDiscovery.diagnoseModuleDetectionIssues(modulesDirectoryPath);
                    }
                    
                    currentStep++;
                    
                    // Process each discovered module
                    for (File moduleDir : validModuleDirectories) {
                        if (currentStep >= totalSteps - 3) break;
                        String moduleName = moduleDir.getName();
                        Logging.info("⚙️ Processing module: " + moduleName);
                        windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Processing module: " + moduleName);
                        currentStep++;
                    }
                    
                    // Load the discovered modules
                    List<GameModule> discoveredModules = new ArrayList<>();
                    try {
                        Logging.info("📦 Starting module loading...");
                        windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Loading compiled modules...");
                        discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
                        Logging.info("✅ Module loading completed. Successfully loaded " + discoveredModules.size() + " modules");
                    } catch (Exception e) {
                        Logging.error("❌ Module loading failed: " + e.getMessage(), e);
                        windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Module loading failed - continuing with empty list");
                    }
                    
                    windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Found " + discoveredModules.size() + " modules");
                }
            }
            
            Logging.info("🏁 Finalizing module loading...");
            windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Finalizing module loading...");
            
        } catch (Exception e) {
            Logging.error("💥 Critical error during module loading: " + e.getMessage(), e);
            windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Error during module loading - continuing...");
        }
    }

    public static void ensureUIReady(Stage primaryApplicationStage, GDKGameLobbyController lobbyController, StartupWindowManager windowManager) {
        int totalSteps = windowManager.getTotalSteps();
        
        // Step 1: Initialize startup window
        Logging.info("🔄 Initializing startup window...");
        windowManager.updateProgress(1, "Initializing startup window...");
        
        // Add delay so user can read the message
        // addDevelopmentDelay("Step 1 completed - waiting for user to read");
        
        // Step 2: Discover available modules
        Logging.info("🔍 Discovering available modules...");
        windowManager.updateProgress(2, "Discovering available modules...");
        
        // Add delay so user can read the message
        // addDevelopmentDelay("Step 2 completed - waiting for user to read");
        
        // Step 3: Load discovered modules
        Logging.info("📦 Loading discovered modules...");
        windowManager.updateProgress(3, "Loading discovered modules...");
        
        // Add delay so user can read the message
        // addDevelopmentDelay("Step 3 completed - waiting for user to read");
        
        // Step 4: Validate module compilation
        Logging.info("✅ Validating module compilation...");
        windowManager.updateProgress(4, "Validating module compilation...");
        
        // Add delay so user can read the message
        // addDevelopmentDelay("Step 4 completed - waiting for user to read");
        
        // Step 5: Check for compilation failures
        Logging.info("🚀 Checking for compilation failures on startup...");
        windowManager.updateProgress(5, "Checking for compilation failures...");
        
        // Add delay so user can read the message
        // addDevelopmentDelay("Step 5 completed - waiting for user to read");
        
        // Step 6: Startup complete
        Logging.info("📊 Startup complete");
        windowManager.updateProgress(6, "Startup complete");
        
        // Add delay so user can read the message
        // addDevelopmentDelay("Step 6 completed - waiting for user to read");
        
        // Step 7: Ready!
        Logging.info("📊 Ready!");
        windowManager.updateProgress(7, "Ready!");
        
        // Add final delay so user can read the "Ready!" message
        // addDevelopmentDelay("Step 7 completed - waiting for user to read 'Ready!' message");
        
        // Add a 3-second delay before opening the GUI
        Logging.info("⏳ Adding 3-second delay before opening GUI for development...");
        // addDevelopmentDelay("Final delay before GUI opens");
        
        // Run module loading on background thread to prevent UI blocking
        Thread moduleLoadingThread = new Thread(() -> {
            try {
                Logging.info("🔄 Starting module loading on background thread");
                Logging.info("   Thread name: " + Thread.currentThread().getName());
                Logging.info("   Is daemon: " + Thread.currentThread().isDaemon());
                
                loadModulesWithProgress(windowManager, totalSteps);
                Logging.info("✅ loadModulesWithProgress completed");
                
                // Update UI on JavaFX thread after module loading completes
                Logging.info("🔄 Scheduling UI refresh on JavaFX thread...");
                Platform.runLater(() -> {
                    try {
                        Logging.info("🔄 Now on JavaFX thread, refreshing available game modules...");
                        if (lobbyController != null) {
                            Logging.info("   Lobby controller is not null, calling refreshAvailableGameModulesFast()");
                            lobbyController.refreshAvailableGameModulesFast();
                            Logging.info("✅ refreshAvailableGameModulesFast() completed");
                        } else {
                            Logging.error("❌ Lobby controller is null!");
                        }
                    } catch (Exception e) {
                        Logging.error("❌ Error refreshing game modules: " + e.getMessage(), e);
                        e.printStackTrace();
                    }
                });
                
                // Continue with remaining startup steps
                Platform.runLater(() -> {
                    try {
                        windowManager.updateProgress(totalSteps - 2, "Checking for compilation issues...");
                        if (lobbyController != null) {
                            lobbyController.checkStartupCompilationFailures();
                        }
                    } catch (Exception e) {
                        Logging.error("❌ Error checking compilation issues: " + e.getMessage());
                    }
                });
                
                Platform.runLater(() -> {
                    windowManager.updateProgress(totalSteps - 1, "Startup complete");
                    windowManager.updateProgress(totalSteps, "Ready!");
                });
                
                Logging.info("✅ Module loading thread completed successfully");
                
            } catch (Exception e) {
                Logging.error("💥 Critical error in module loading thread: " + e.getMessage());
            }
        });
        
        // Register the module loading thread for cleanup
        Shutdown.registerCleanupTask(() -> {
            Logging.info("🧹 Cleaning up module loading thread...");
            if (moduleLoadingThread.isAlive()) {
                moduleLoadingThread.interrupt();
            }
        });
        
        // Start the module loading thread
        moduleLoadingThread.start();
        
        // Register the StartupWindowManager for cleanup
        Shutdown.registerCleanupTask(() -> {
            Logging.info("🧹 Cleaning up StartupWindowManager...");
            if (windowManager != null) {
                windowManager.hide();
            }
        });
    }
    
    /**
     * Add a development delay to slow down the startup process so users can read each message.
     * This method adds a 5-second delay with logging to make it clear what's happening.
     */
    private static void addDevelopmentDelay(String reason) {
        Logging.info("⏳ DEVELOPMENT DELAY: " + reason + " - waiting 5 seconds...");
        try {
            Thread.sleep(5000); // 5-second delay for development
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logging.info("⏳ Development delay interrupted");
        }
        Logging.info("✅ Development delay completed for: " + reason);
    }

    public static void showMainStageWithFade(Stage primaryApplicationStage, StartupWindowManager windowManager) {
        windowManager.hide();
        Platform.runLater(() -> {
            primaryApplicationStage.setOpacity(1.0);
            primaryApplicationStage.show();
        });
    }
} 