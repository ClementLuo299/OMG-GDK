package launcher.ui_areas.lobby.lifecycle.startup.ui_initialization.viewmodel;

import launcher.ui_areas.lobby.GDKViewModel;
import launcher.ui_areas.lobby.GDKGameLobbyController;

/**
 * Handles wiring the ViewModel to the controller.
 * 
 * <p>This class is responsible for establishing the connection between
 * the UI controller and the application's ViewModel, enabling the controller
 * to directory_access and update the ViewModel's state.
 * 
 * @author Clement Luo
 * @date January 2, 2026
 * @edited January 2, 2026
 * @since Beta 1.0
 */
public final class WireViewModelToController {
    
    private WireViewModelToController() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Wires up the controller with the ViewModel.
     * 
     * <p>This method establishes the connection between the UI controller
     * and the application's data model. This enables the controller to
     * directory_access and update the ViewModel's state.
     * 
     * @param applicationViewModel The ViewModel instance containing application state
     * @param lobbyController The lobby controller to connect with the ViewModel
     * @throws RuntimeException if the controller is null or wiring fails
     */
    public static void wireUp(GDKViewModel applicationViewModel, GDKGameLobbyController lobbyController) {
        try {
            if (lobbyController != null) {
                lobbyController.setViewModel(applicationViewModel);
            } else {
                throw new RuntimeException("Lobby controller is null - cannot wire up ViewModel");
            }
        } catch (Exception wiringError) {
            throw new RuntimeException("Failed to wire up controller with ViewModel", wiringError);
        }
    }
}

