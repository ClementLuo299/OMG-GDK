package launcher.features.lobby_features.managers.core.setup;

import gdk.internal.Logging;
import launcher.ui_areas.lobby.GDKGameLobbyController;
import launcher.features.persistence.JsonPersistenceManager;
import launcher.features.lobby_features.managers.messaging.MessageBridgeManager;
import launcher.ui_areas.lobby.managers.ui.StatusLabelManager;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;
import launcher.ui_areas.lobby.subcontrollers.JsonActionButtonsController;
import launcher.ui_areas.lobby.subcontrollers.TopBarController;
import launcher.features.module_handling.loading.ModuleCompiler;

/**
 * Handles post-initialization setup tasks.
 * Encapsulates setup logic that occurs after all components are created.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
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
        
        // ==================== MESSAGE BRIDGE SETUP ====================
        
        // Mirror game 'end' messages into JSON output
        messageBridgeManager.subscribeToEndMessageMirror();
        
        // ==================== MODULE COMPILER SETUP ====================
        
        // Register controller with ModuleCompiler for progress updates
        ModuleCompiler.setUIController(controller);
        
        // ==================== PERSISTENCE SETUP ====================
        
        // Load saved JSON content and toggle state
        jsonPersistenceManager.loadPersistenceSettings();
        
        // ==================== UI STATE INITIALIZATION ====================
        
        // Initialize the status label with current game count
        if (statusLabelManager != null && gameSelectionController != null) {
            statusLabelManager.updateGameCountStatus(gameSelectionController.getAvailableGameModules().size());
        }
        
        // ==================== SUBCONTROLLER INITIALIZATION ====================
        
        // Initialize all subcontrollers
        gameSelectionController.initialize();
        jsonActionButtonsController.initialize();
        topBarController.initialize();
        
        Logging.info("GDK Game Picker Controller initialized successfully");
    }
}

