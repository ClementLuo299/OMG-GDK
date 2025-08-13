package launcher.lifecycle.start;

import gdk.GameModule;
import gdk.Logging;
import javafx.application.Platform;
import javafx.stage.Stage;
import launcher.GDKApplication;
import launcher.gui.GDKGameLobbyController;
import launcher.utils.ModuleCompiler;
import launcher.utils.ModuleDiscovery;
import launcher.lifecycle.start.startup_window.StartupWindowManager;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * Encapsulates operational startup tasks (module loading, readiness checks, and stage display).
 * 
 * @author Clement Luo
 * @date August 9, 2025
 * @edited August 12, 2025
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

            String modulesDirectoryPath = GDKApplication.MODULES_DIRECTORY_PATH;
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
        
        // Run module loading on background thread to prevent UI blocking
        Thread moduleLoadingThread = new Thread(() -> {
            try {
                Logging.info("🔄 Starting module loading on background thread");
                loadModulesWithProgress(windowManager, totalSteps);
                
                // Update UI on JavaFX thread after module loading completes
                Platform.runLater(() -> {
                    try {
                        if (lobbyController != null) {
                            Logging.info("🔄 Refreshing available game modules...");
                            lobbyController.refreshAvailableGameModulesFast();
                        }
                    } catch (Exception e) {
                        Logging.error("❌ Error refreshing game modules: " + e.getMessage(), e);
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
                        Logging.error("❌ Error checking compilation issues: " + e.getMessage(), e);
                    }
                });
                
                Platform.runLater(() -> {
                    windowManager.updateProgress(totalSteps - 1, "Startup complete");
                    windowManager.updateProgress(totalSteps, "Ready!");
                });
                
                Logging.info("✅ Module loading thread completed successfully");
                
            } catch (Exception e) {
                Logging.error("💥 Critical error in module loading thread: " + e.getMessage(), e);
                Platform.runLater(() -> {
                    windowManager.updateProgress(totalSteps - 2, "Error during startup - continuing...");
                    windowManager.updateProgress(totalSteps - 1, "Startup complete");
                    windowManager.updateProgress(totalSteps, "Ready!");
                });
            }
        });
        
        // Set thread name for debugging
        moduleLoadingThread.setName("ModuleLoadingThread");
        moduleLoadingThread.setDaemon(true);
        
        // Register the thread with the shutdown system for proper cleanup
        launcher.lifecycle.stop.Shutdown.registerCleanupTask(() -> {
            Logging.info("🧹 Cleaning up module loading thread...");
            if (moduleLoadingThread.isAlive()) {
                try {
                    moduleLoadingThread.interrupt();
                    moduleLoadingThread.join(2000); // Wait up to 2 seconds
                    if (moduleLoadingThread.isAlive()) {
                        Logging.warning("⚠️ Module loading thread did not terminate gracefully");
                    }
                } catch (InterruptedException e) {
                    Logging.info("🧹 Module loading thread cleanup interrupted");
                }
            }
        });
        
        // Start the background thread
        Logging.info("🚀 Starting module loading background thread");
        moduleLoadingThread.start();
        
        // Register StartupWindowManager cleanup
        launcher.lifecycle.stop.Shutdown.registerCleanupTask(() -> {
            Logging.info("🧹 Cleaning up StartupWindowManager...");
            try {
                if (windowManager != null) {
                    windowManager.hide();
                }
            } catch (Exception e) {
                Logging.error("❌ Error cleaning up StartupWindowManager: " + e.getMessage(), e);
            }
        });
    }

    public static void showMainStageWithFade(Stage primaryApplicationStage, StartupWindowManager windowManager) {
        windowManager.hide();
        Platform.runLater(() -> {
            primaryApplicationStage.setOpacity(1.0);
            primaryApplicationStage.show();
        });
    }
} 