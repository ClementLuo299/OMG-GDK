package launcher.lifecycle.start;

import gdk.GameModule;
import javafx.application.Platform;
import javafx.stage.Stage;
import launcher.GDKApplication;
import launcher.gui.GDKGameLobbyController;
import launcher.utils.ModuleCompiler;
import launcher.utils.ModuleDiscovery;
import launcher.lifecycle.start.startup_window.StartupWindowManager;

import java.io.File;
import java.util.List;

/**
 * Encapsulates operational startup tasks (module loading, readiness checks, and stage display).
 * 
 * @author Clement Luo
 * @date August 9, 2025
 * @edited August 9, 2025
 * @since 1.0
 */
public final class StartupOperations {

    private StartupOperations() {}

    public static void loadModulesWithProgress(StartupWindowManager windowManager, int totalSteps) {
        windowManager.updateProgress(3, "Initializing game modules...");
        if (ModuleCompiler.needToBuildModules()) {
            windowManager.updateProgress(4, "Building modules...");
        } else {
            windowManager.updateProgress(4, "Using existing builds (recent compilation detected)");
        }
        windowManager.updateProgress(5, "Preparing module discovery...");

        String modulesDirectoryPath = GDKApplication.MODULES_DIRECTORY_PATH;
        File modulesDirectory = new File(modulesDirectoryPath);
        int currentStep = 6;

        if (!modulesDirectory.exists()) {
            windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Modules directory not found");
        } else {
            windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Discovering modules...");
            List<File> validModuleDirectories = ModuleDiscovery.getValidModuleDirectories(modulesDirectory);
            currentStep++;
            for (File moduleDir : validModuleDirectories) {
                if (currentStep >= totalSteps - 3) break;
                String moduleName = moduleDir.getName();
                windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Processing module: " + moduleName);
                currentStep++;
            }
            List<GameModule> discoveredModules = ModuleCompiler.loadModules(validModuleDirectories);
            windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Found " + discoveredModules.size() + " modules");
        }
        windowManager.updateProgress(Math.min(currentStep, totalSteps - 3), "Finalizing module loading...");
    }

    public static void ensureUIReady(Stage primaryApplicationStage, GDKGameLobbyController lobbyController, StartupWindowManager windowManager) {
        int totalSteps = windowManager.getTotalSteps();
        loadModulesWithProgress(windowManager, totalSteps);
        Platform.runLater(() -> {
            try {
                if (lobbyController != null) {
                    lobbyController.refreshAvailableGameModulesFast();
                }
            } catch (Exception ignored) {
            }
        });
        windowManager.updateProgress(totalSteps - 2, "Checking for compilation issues...");
        Platform.runLater(() -> {
            try {
                if (lobbyController != null) {
                    lobbyController.checkStartupCompilationFailures();
                }
            } catch (Exception ignored) {
            }
        });
        windowManager.updateProgress(totalSteps - 1, "Startup complete");
        windowManager.updateProgress(totalSteps, "Ready!");
    }

    public static void showMainStageWithFade(Stage primaryApplicationStage, StartupWindowManager windowManager) {
        windowManager.hide();
        Platform.runLater(() -> {
            primaryApplicationStage.setOpacity(1.0);
            primaryApplicationStage.show();
        });
    }
} 