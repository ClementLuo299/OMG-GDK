package launcher.ui_areas.lobby.subcontrollers;

import gdk.api.GameModule;
import launcher.ui_areas.lobby.messaging.MessageManager;
import launcher.ui_areas.lobby.ui_management.LaunchButtonManager;
import launcher.features.persistence.helpers.save.SavePreviouslySelectedGame;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

/**
 * Subcontroller for the Game Selection UI area.
 * 
 * Handles game selection dropdown and launch button only.
 * 
 * @author Clement Luo
 * @date December 29, 2025
 * @edited December 29, 2025
 * @since Beta 1.0
 */
public class GameSelectionController {
    
    // UI Components
    private final ComboBox<GameModule> gameSelector;
    private final Button launchGameButton;
    
    // Managers
    private final MessageManager messageManager;
    private final LaunchButtonManager launchButtonManager;
    
    // State
    private final ObservableList<GameModule> availableGameModules;
    private GameModule selectedGameModule;
    
    // Callbacks
    private Runnable onLaunchGame;
    private Runnable onGameSelected;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a new GameSelectionController.
     * 
     * @param gameSelector The game selection ComboBox
     * @param launchGameButton The launch game button
     * @param availableGameModules The observable list of available game modules
     * @param messageManager The message manager
     * @param launchButtonManager The launch button manager
     */
    public GameSelectionController(
            ComboBox<GameModule> gameSelector,
            Button launchGameButton,
            ObservableList<GameModule> availableGameModules,
            MessageManager messageManager,
            LaunchButtonManager launchButtonManager) {
        
        this.gameSelector = gameSelector;
        this.launchGameButton = launchGameButton;
        this.availableGameModules = availableGameModules;
        this.messageManager = messageManager;
        this.launchButtonManager = launchButtonManager;
    }
    
    // ==================== INITIALIZATION ====================
    
    /**
     * Initialize the game selection UI and event handlers.
     * Sets up the ComboBox display, selection handler, and launch button handler.
     */
    public void initialize() {
        // Configure the ComboBox to display game module names from extract_metadata
        gameSelector.setCellFactory(param -> new javafx.scene.control.ListCell<GameModule>() {
            @Override
            protected void updateItem(GameModule gameModule, boolean empty) {
                super.updateItem(gameModule, empty);
                if (empty || gameModule == null) {
                    setText("Select a game.");
                } else {
                    setText(gameModule.getMetadata().getGameName());
                }
            }
        });
        
        gameSelector.setButtonCell(gameSelector.getCellFactory().call(null));
        gameSelector.setItems(availableGameModules);
        
        // Game Selection Handler
        gameSelector.setOnAction(event -> {
            selectedGameModule = gameSelector.getValue();
            String selectedGameName = selectedGameModule != null ? selectedGameModule.getMetadata().getGameName() : "None";
            
            // Update launch button state based on selection
            launchButtonManager.updateLaunchButtonState(selectedGameModule != null);
            
            if (selectedGameModule != null) {
                messageManager.addMessage("Selected game: " + selectedGameName);
                SavePreviouslySelectedGame.save(selectedGameName);
            } else {
                messageManager.addMessage("No game selected");
            }
            
            if (onGameSelected != null) {
                onGameSelected.run();
            }
        });
        
        // Launch button: Start the selected game with module_source_validation
        launchGameButton.setOnAction(event -> {
            if (onLaunchGame != null) {
                onLaunchGame.run();
            }
        });
        
        // Initialize launch button state (disabled until a game is selected)
        launchButtonManager.updateLaunchButtonState(false);
    }
    
    // ==================== CALLBACK SETTERS ====================
    
    /**
     * Set callback for when launch game is requested.
     * Typically delegates to GameLaunchingManager.launchGameFromUI().
     * 
     * @param onLaunchGame The callback to execute (e.g., launch game with module_source_validation)
     */
    public void setOnLaunchGame(Runnable onLaunchGame) {
        this.onLaunchGame = onLaunchGame;
    }
    
    /**
     * Set callback for when a game is selected from the dropdown.
     * Typically used to notify other components (e.g., JsonActionButtonsController).
     * 
     * @param onGameSelected The callback to execute (e.g., update JSON editor state)
     */
    public void setOnGameSelected(Runnable onGameSelected) {
        this.onGameSelected = onGameSelected;
    }
    
    // ==================== STATE MANAGEMENT ====================
    
    /**
     * Get the currently selected game module.
     * 
     * @return The selected game module, or null if none selected
     */
    public GameModule getSelectedGameModule() {
        return selectedGameModule;
    }
    
    /**
     * Set the selected game module programmatically.
     * Updates the ComboBox value and triggers selection handler.
     * 
     * @param gameModule The game module to select
     */
    public void setSelectedGameModule(GameModule gameModule) {
        this.selectedGameModule = gameModule;
        if (gameModule != null) {
            gameSelector.setValue(gameModule);
        }
    }
    
    /**
     * Get the observable list of available game modules.
     * This list is updated by GameModuleRefreshManager during refresh operations.
     * 
     * @return The observable list of available game modules
     */
    public ObservableList<GameModule> getAvailableGameModules() {
        return availableGameModules;
    }
}
