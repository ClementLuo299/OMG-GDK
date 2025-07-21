package com.games.modules.example;

import com.gdk.shared.game.GameMode;
import com.gdk.shared.game.GameDifficulty;
import com.gdk.shared.game.GameModeFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Example showing how games can define their own custom game modes and difficulties.
 * This demonstrates the flexibility of the new system compared to the old fixed enums.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class CustomGameModesExample {
    
    /**
     * Example of how a chess game might define custom modes.
     */
    public static class ChessGameModes {
        
        // Custom chess-specific modes
        public static final GameMode BLITZ = GameModeFactory.createTimeBasedMode(
            "chess_blitz", 
            "Blitz Chess", 
            "Fast-paced chess with 5 minutes per player", 
            "⚡", 
            300 // 5 minutes
        );
        
        public static final GameMode BULLET = GameModeFactory.createTimeBasedMode(
            "chess_bullet", 
            "Bullet Chess", 
            "Ultra-fast chess with 1 minute per player", 
            "🔫", 
            60 // 1 minute
        );
        
        public static final GameMode RAPID = GameModeFactory.createTimeBasedMode(
            "chess_rapid", 
            "Rapid Chess", 
            "Quick chess with 15 minutes per player", 
            "🏃", 
            900 // 15 minutes
        );
        
        public static final GameMode CLASSICAL = GameModeFactory.createOnlineMultiplayerMode(
            "chess_classical", 
            "Classical Chess", 
            "Traditional chess with no time pressure", 
            "♟️", 
            2, 
            true // ranked
        );
        
        public static final GameMode PUZZLE = GameModeFactory.createPuzzleMode(
            "chess_puzzle", 
            "Chess Puzzles", 
            "Solve tactical chess positions", 
            "🧩", 
            "tactical"
        );
        
        public static final GameMode AI_TRAINING = GameModeFactory.createAIMode(
            "chess_ai_training", 
            "AI Training", 
            "Practice against chess AI at different levels", 
            "🤖", 
            "training"
        );
        
        // Custom properties for chess modes
        public static GameMode createCustomChessMode(String name, boolean allowTakebacks, boolean showAnalysis) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("category", "Chess");
            properties.put("allowTakebacks", allowTakebacks);
            properties.put("showAnalysis", showAnalysis);
            properties.put("pieceStyle", "classic");
            
            return new GameMode(
                "chess_" + name.toLowerCase().replace(" ", "_"),
                name,
                "Custom chess mode with special rules",
                "♟️",
                "#6f42c1",
                properties
            );
        }
    }
    
    /**
     * Example of how a puzzle game might define custom difficulties.
     */
    public static class PuzzleGameDifficulties {
        
        // Custom puzzle-specific difficulties using direct constructor
        public static final GameDifficulty KIDS = new GameDifficulty(
            "puzzle_kids",
            "Kids Mode",
            "Very simple puzzles for young children",
            "👶",
            "#28a745",
            1
        );
        
        public static final GameDifficulty FAMILY = new GameDifficulty(
            "puzzle_family",
            "Family Mode",
            "Fun puzzles for the whole family",
            "👨‍👩‍👧‍👦",
            "#ffc107",
            2
        );
        
        public static final GameDifficulty LOGIC_MASTER = new GameDifficulty(
            "puzzle_logic_master",
            "Logic Master",
            "Extremely challenging logic puzzles",
            "🧠",
            "#6f42c1",
            5
        );
        
        // Custom speed puzzle difficulty with properties
        public static final GameDifficulty SPEED_PUZZLE = createSpeedPuzzleDifficulty(
            "puzzle_speed",
            "Speed Puzzle",
            "Solve puzzles against the clock",
            "⏱️",
            30 // 30 seconds per puzzle
        );
        
        // Helper method to create speed puzzle difficulty
        private static GameDifficulty createSpeedPuzzleDifficulty(String id, String name, String description, String icon, int timeLimit) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("timeLimit", timeLimit);
            properties.put("hasTimer", true);
            properties.put("hintsEnabled", false);
            properties.put("undoEnabled", false);
            
            return new GameDifficulty(id, name, description, icon, "#dc3545", 4, properties);
        }
        
        // Custom properties for puzzle difficulties
        public static GameDifficulty createCustomPuzzleDifficulty(String name, int gridSize, boolean allowHints, int timeLimit) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("gridSize", gridSize);
            properties.put("allowHints", allowHints);
            properties.put("timeLimit", timeLimit);
            properties.put("puzzleType", "custom");
            
            return new GameDifficulty(
                "puzzle_" + name.toLowerCase().replace(" ", "_"),
                name,
                "Custom puzzle difficulty with special settings",
                "🧩",
                "#6f42c1",
                gridSize, // Use grid size as difficulty level
                properties
            );
        }
    }
    
    /**
     * Example of how a strategy game might define custom modes.
     */
    public static class StrategyGameModes {
        
        public static final GameMode CAMPAIGN = GameModeFactory.createSinglePlayerMode(
            "strategy_campaign",
            "Campaign Mode",
            "Epic single-player campaign with story",
            "📖"
        );
        
        public static final GameMode SKIRMISH = GameModeFactory.createLocalMultiplayerMode(
            "strategy_skirmish",
            "Skirmish Mode",
            "Quick battles against AI or friends",
            "⚔️",
            4
        );
        
        public static final GameMode ONLINE_BATTLE = GameModeFactory.createOnlineMultiplayerMode(
            "strategy_online_battle",
            "Online Battle",
            "Compete against players worldwide",
            "🌐",
            2,
            true // ranked
        );
        
        public static final GameMode COOP_SURVIVAL = GameModeFactory.createSurvivalMode(
            "strategy_coop_survival",
            "Co-op Survival",
            "Work together to survive endless waves",
            "🛡️",
            true // endless
        );
        
        // Custom strategy game properties
        public static GameMode createCustomStrategyMode(String name, String mapType, int maxPlayers, boolean fogOfWar) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("category", "Strategy");
            properties.put("mapType", mapType);
            properties.put("maxPlayers", maxPlayers);
            properties.put("fogOfWar", fogOfWar);
            properties.put("resourceType", "standard");
            
            return new GameMode(
                "strategy_" + name.toLowerCase().replace(" ", "_"),
                name,
                "Custom strategy mode with unique settings",
                "🎯",
                "#28a745",
                properties
            );
        }
    }
    
    /**
     * Example of how to use these custom modes in a game module.
     */
    public static GameMode[] getChessGameModes() {
        return new GameMode[] {
            ChessGameModes.CLASSICAL,
            ChessGameModes.RAPID,
            ChessGameModes.BLITZ,
            ChessGameModes.BULLET,
            ChessGameModes.PUZZLE,
            ChessGameModes.AI_TRAINING,
            ChessGameModes.createCustomChessMode("Friendly", true, false),
            ChessGameModes.createCustomChessMode("Tournament", false, true)
        };
    }
    
    public static GameDifficulty[] getPuzzleGameDifficulties() {
        return new GameDifficulty[] {
            PuzzleGameDifficulties.KIDS,
            PuzzleGameDifficulties.FAMILY,
            GameDifficulty.MEDIUM, // Use predefined
            GameDifficulty.HARD,   // Use predefined
            PuzzleGameDifficulties.LOGIC_MASTER,
            PuzzleGameDifficulties.SPEED_PUZZLE,
            PuzzleGameDifficulties.createCustomPuzzleDifficulty("Giant Grid", 15, true, 0),
            PuzzleGameDifficulties.createCustomPuzzleDifficulty("Speed Master", 9, false, 60)
        };
    }
    
    public static GameMode[] getStrategyGameModes() {
        return new GameMode[] {
            StrategyGameModes.CAMPAIGN,
            StrategyGameModes.SKIRMISH,
            StrategyGameModes.ONLINE_BATTLE,
            StrategyGameModes.COOP_SURVIVAL,
            StrategyGameModes.createCustomStrategyMode("Desert Warfare", "desert", 4, true),
            StrategyGameModes.createCustomStrategyMode("Urban Combat", "city", 2, false)
        };
    }
} 