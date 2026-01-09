package launcher.ui_areas.lobby.lifecycle.controller_initialization;

import launcher.ui_areas.lobby.json_editor.JsonEditor;
import launcher.ui_areas.lobby.ui_management.LoadingAnimationManager;
import launcher.ui_areas.lobby.game_launching.ModuleCompilationChecker;
import launcher.ui_areas.lobby.json_editor.JsonEditorOperations;
import launcher.ui_areas.lobby.ui_management.StatusLabelManager;
import launcher.ui_areas.lobby.ui_management.LaunchButtonManager;
import launcher.ui_areas.lobby.game_launching.ModuleChangesReporter;
import launcher.ui_areas.lobby.game_launching.GameLaunchingManager;
import launcher.ui_areas.lobby.messaging.MessageBridgeManager;
import launcher.ui_areas.lobby.messaging.MessageManager;
import launcher.ui_areas.lobby.lifecycle.shutdown.LobbyShutdownManager;
import launcher.ui_areas.settings_page.SettingsNavigationManager;
import launcher.ui_areas.lobby.subcontrollers.GameSelectionController;
import launcher.ui_areas.lobby.subcontrollers.JsonActionButtonsController;
import launcher.ui_areas.lobby.subcontrollers.TopBarController;
import launcher.ui_areas.lobby.game_launching.GameModuleRefreshManager;

/**
 * Result of initialization containing all created managers and subcontrollers.
 * Used to pass initialization results between methods and to the main controller.
 * 
 * @author Clement Luo
 * @date January 8, 2026
 * @since Beta 1.0
 */
public record InitializationResult(
    JsonEditor jsonInputEditor,
    JsonEditor jsonOutputEditor,
    MessageManager messageManager,
    LoadingAnimationManager loadingAnimationManager,
    ModuleCompilationChecker moduleCompilationChecker,
    JsonEditorOperations jsonEditorOperations,
    StatusLabelManager statusLabelManager,
    LaunchButtonManager launchButtonManager,
    ModuleChangesReporter moduleChangeReporter,
    GameLaunchingManager gameLaunchManager,
    MessageBridgeManager messageBridgeManager,
    LobbyShutdownManager lobbyShutdownManager,
    SettingsNavigationManager settingsNavigationManager,
    GameSelectionController gameSelectionController,
    JsonActionButtonsController jsonActionButtonsController,
    TopBarController topBarController,
    GameModuleRefreshManager gameModuleRefreshManager
) {}

