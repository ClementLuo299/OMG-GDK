package com.games.modules.tictactoe;

import com.core.screens.ScreenLoadable;
import com.game.GameModule;
import com.game.GameOptions;
import com.game.enums.GameMode;
import com.utils.error_handling.Logging;

import java.util.function.Supplier;

/**
 * Screen configuration for the TicTacToe game.
 * This provides TicTacToe-specific screen configuration while keeping all
 * game-specific resources and configuration within the module.
 *
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class TicTacToeScreenLoadable {
    
    private final GameMode gameMode;
    private final int playerCount;
    private final GameOptions gameOptions;
    
    // Screen configuration constants
    public static final String FXML_PATH = "/games/tictactoe/fxml/tictactoe.fxml";
    public static final String CSS_PATH = "/games/tictactoe/css/tictactoe.css";
    public static final Class<?> CONTROLLER_CLASS = TicTacToeController.class;
    
    /**
     * Creates a new TicTacToeScreenLoadable for the specified game parameters.
     * 
     * @param gameMode The game mode
     * @param playerCount Number of players
     * @param gameOptions Game-specific options
     */
    public TicTacToeScreenLoadable(GameMode gameMode, int playerCount, GameOptions gameOptions) {
        this.gameMode = gameMode;
        this.playerCount = playerCount;
        this.gameOptions = gameOptions;
    }
    
    /**
     * Gets the FXML path for this screen.
     * @return The FXML path
     */
    public String getFxmlPath() {
        return FXML_PATH;
    }
    
    /**
     * Gets the CSS path for this screen.
     * @return The CSS path
     */
    public String getCssPath() {
        return CSS_PATH;
    }
    
    /**
     * Gets the controller class for this screen.
     * @return The controller class
     */
    public Class<?> getControllerClass() {
        return CONTROLLER_CLASS;
    }
    
    /**
     * Gets the game mode for this screen.
     * @return The game mode
     */
    public GameMode getGameMode() {
        return gameMode;
    }
    
    /**
     * Gets the player count for this screen.
     * @return The player count
     */
    public int getPlayerCount() {
        return playerCount;
    }
    
    /**
     * Gets the game options for this screen.
     * @return The game options
     */
    public GameOptions getGameOptions() {
        return gameOptions;
    }
    
    /**
     * Gets a unique screen name for this TicTacToe screen.
     * @return A unique screen name
     */
    public String getScreenName() {
        return "TicTacToe_" + gameMode.getDisplayName().replace(" ", "_") + "_" + System.currentTimeMillis();
    }
} 