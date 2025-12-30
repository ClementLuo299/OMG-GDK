package launcher.gui.lobby.ui_logic.managers.core.setup;

import gdk.internal.Logging;
import launcher.gui.lobby.ui_logic.GDKGameLobbyController;
import launcher.gui.lobby.persistence.JsonPersistenceManager;
import launcher.gui.lobby.ui_logic.managers.game.GameModuleRefreshManager;
import launcher.gui.lobby.ui_logic.managers.messaging.MessageBridgeManager;
import launcher.gui.lobby.ui_logic.managers.ui.StatusLabelManager;
import launcher.gui.lobby.ui_logic.subcontrollers.GameSelectionController;
import launcher.gui.lobby.ui_logic.subcontrollers.JsonActionButtonsController;
import launcher.gui.lobby.ui_logic.subcontrollers.TopBarController;
import launcher.utils.module.ModuleCompiler;

/**
 * Handles post-initialization setup tasks.
 * Encapsulates setup logic that occurs after all components are created.
 * 
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class PostInitializationSetup {
    
    /**
     * Perform all post-initialization setup tasks.
     * 
     * @param controller The main lobby controller
     * @param messageBridgeManager The message bridge manager
     * @param jsonPersistenceManager The JSON persistence manager
     * @param statusLabelManager The status label manager
     * @param gameSelectionController The game selection controller
     * @param jsonActionButtonsController The JSON action buttons controller
     * @param topBarController The top bar controller
     */
    public static void performSetup(
            GDKGameLobbyController controller,
            MessageBridgeManager messageBridgeManager,
            JsonPersistenceManager jsonPersistenceManager,
            StatusLabelManager statusLabelManager,
            GameSelectionController gameSelectionController,
            JsonActionButtonsController jsonActionButtonsController,
            TopBarController topBarController) {
        
        // Mirror game 'end' messages into JSON output
        messageBridgeManager.subscribeToEndMessageMirror();
        
        // Register controller with ModuleCompiler for progress updates
        ModuleCompiler.setUIController(controller);
        
        // Load saved JSON content and toggle state
        jsonPersistenceManager.loadPersistenceSettings();
        
        // Initialize the status label
        if (statusLabelManager != null && gameSelectionController != null) {
            statusLabelManager.updateGameCountStatus(gameSelectionController.getAvailableGameModules().size());
        }
        
        // Initialize all subcontrollers
        gameSelectionController.initialize();
        jsonActionButtonsController.initialize();
        topBarController.initialize();
        
        Logging.info("GDK Game Picker Controller initialized successfully");
    }
}

