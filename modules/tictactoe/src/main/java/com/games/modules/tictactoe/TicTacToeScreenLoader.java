package com.games.modules.tictactoe;

import com.core.screens.ScreenLoader;
import com.core.screens.ScreenLoadResult;
import com.game.GameModule;
import com.game.GameOptions;
import com.utils.error_handling.Logging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Custom ScreenLoader for the TicTacToe game module.
 * This extends the main application's ScreenLoader to handle TicTacToe-specific
 * loading and initialization while integrating with the main screen system.
 *
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class TicTacToeScreenLoader extends ScreenLoader {
    
    /**
     * Loads a TicTacToe screen with game-specific initialization.
     * 
     * @param screenLoadable The TicTacToe screen loadable
     * @return The screen load result
     */
    public ScreenLoadResult<TicTacToeController> loadTicTacToeScreen(TicTacToeScreenLoadable screenLoadable) {
        Logging.info("🎮 Loading TicTacToe screen with game parameters");
        
        try {
            // Load the FXML using the game's resource path
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screenLoadable.getFxmlPath()));
            
            // Load the root node
            Parent root = loader.load();
            
            // Get the controller
            TicTacToeController controller = loader.getController();
            
            if (controller != null) {
                // Initialize the game controller with game parameters
                controller.initializeGame(
                    screenLoadable.getGameMode(),
                    screenLoadable.getPlayerCount(),
                    screenLoadable.getGameOptions()
                );
                
                Logging.info("✅ TicTacToe controller initialized with game parameters");
            } else {
                Logging.warning("⚠️ No TicTacToe controller found");
            }
            
            // Create and return the screen load result
            ScreenLoadResult<TicTacToeController> result = new ScreenLoadResult<>(
                root,
                controller
            );
            
            Logging.info("✅ TicTacToe screen loaded successfully");
            return result;
            
        } catch (IOException e) {
            Logging.error("❌ Failed to load TicTacToe screen: " + e.getMessage(), e);
            // Return null for error case since ScreenLoadResult doesn't support null values
            return null;
        }
    }
} 