package tictactoe;

import gdk.GameModule;
import gdk.GameMode;
import gdk.GameOptions;
import gdk.GameState;

import gdk.Logging;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Map;
import java.util.HashMap;

/**
 * Game metadata and configuration for the TicTacToe game module.
 * Contains all the game information, supported modes, difficulties, and player counts.
 * This class is responsible for providing metadata to the GDK launcher.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class GameMetadata implements GameModule {
    
    // ==================== GAME METADATA ====================
    
    private static final String GAME_ID = "tictactoe";
    private static final String GAME_NAME = "Tic Tac Toe";
    private static final String GAME_DESCRIPTION = "Classic Tic Tac Toe game with AI opponent";
    private static final String GAME_CATEGORY = "Strategy";
    private static final int MIN_PLAYERS = 1;
    private static final int MAX_PLAYERS = 2;
    private static final int ESTIMATED_DURATION = 5; // 5 minutes
    
    // ==================== GAME MODULE INTERFACE ====================
    
    @Override
    public String getGameId() {
        return GAME_ID;
    }
    
    @Override
    public String getGameName() {
        return GAME_NAME;
    }
    
    @Override
    public String getGameDescription() {
        return GAME_DESCRIPTION;
    }
    
    @Override
    public int getMinPlayers() {
        return MIN_PLAYERS;
    }
    
    @Override
    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }
    
    @Override
    public int getEstimatedDuration() {
        return ESTIMATED_DURATION;
    }
    

    
    @Override
    public String getGameCategory() {
        return GAME_CATEGORY;
    }
    
    @Override
    public boolean supportsOnlineMultiplayer() {
        return false; // TicTacToe is typically local
    }
    
    @Override
    public boolean supportsLocalMultiplayer() {
        return true;
    }
    
    @Override
    public boolean supportsSinglePlayer() {
        return true; // Against AI
    }
    
    @Override
    public String getGameFxmlPath() {
        return "/games/tictactoe/fxml/tictactoe.fxml";
    }
    
    @Override
    public String getGameCssPath() {
        return "/games/tictactoe/css/tictactoe.css";
    }
    
    @Override
    public String getGameIconPath() {
        return "/games/tictactoe/icons/tic_tac_toe_icon.png";
    }
    
    // ==================== ENHANCED SUPPORT METHODS ====================
    
    @Override
    public GameMode[] getSupportedGameModes() {
        return new GameMode[] {
            GameMode.SINGLE_PLAYER,    // Against AI
            GameMode.LOCAL_MULTIPLAYER, // Two players on same device
            GameMode.HOT_SEAT,         // Turn-based local multiplayer
            GameMode.AI_VERSUS,        // Player vs AI
            GameMode.PRACTICE          // Practice mode
        };
    }
    

    
    @Override
    public Map<GameMode, int[]> getSupportedPlayerCounts() {
        Map<GameMode, int[]> playerCounts = new HashMap<>();
        
        // Single player modes (against AI)
        playerCounts.put(GameMode.SINGLE_PLAYER, new int[]{1});
        playerCounts.put(GameMode.AI_VERSUS, new int[]{1});
        playerCounts.put(GameMode.PRACTICE, new int[]{1});
        
        // Multiplayer modes
        playerCounts.put(GameMode.LOCAL_MULTIPLAYER, new int[]{2});
        playerCounts.put(GameMode.HOT_SEAT, new int[]{2});
        
        return playerCounts;
    }
    
    @Override
    public GameMode getDefaultGameMode() {
        return GameMode.SINGLE_PLAYER;
    }
    

    
    @Override
    public int getDefaultPlayerCount(GameMode gameMode) {
        Map<GameMode, int[]> playerCounts = getSupportedPlayerCounts();
        int[] counts = playerCounts.get(gameMode);
        return counts != null && counts.length > 0 ? counts[0] : 1;
    }
    
    @Override
    public boolean supportsGameMode(GameMode gameMode) {
        GameMode[] supportedModes = getSupportedGameModes();
        for (GameMode mode : supportedModes) {
            if (mode.equals(gameMode)) {
                return true;
            }
        }
        return false;
    }
    

    
    @Override
    public boolean supportsPlayerCount(GameMode gameMode, int playerCount) {
        Map<GameMode, int[]> supportedCounts = getSupportedPlayerCounts();
        int[] counts = supportedCounts.get(gameMode);
        if (counts != null) {
            for (int count : counts) {
                if (count == playerCount) {
                    return true;
                }
            }
        }
        return false;
    }
    

    
    // ==================== GAME EXECUTION (DELEGATED) ====================
    
    @Override
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions, Object eventHandler) {
        // Delegate to the actual game implementation
        TicTacToeModule gameModule = new TicTacToeModule();
        return gameModule.launchGame(primaryStage, gameMode, playerCount, gameOptions, eventHandler);
    }
    
    @Override
    public GameState getGameState() {
        return new GameState(GAME_ID, "Not Started", GameMode.SINGLE_PLAYER, 1, new GameOptions());
    }
    
    @Override
    public void loadGameState(GameState gameState) {
        Logging.info("📂 Loading game state for " + GAME_NAME);
    }
    
    @Override
    public void onGameClose() {
        Logging.info("🔒 " + GAME_NAME + " closing - cleaning up resources");
    }
} 