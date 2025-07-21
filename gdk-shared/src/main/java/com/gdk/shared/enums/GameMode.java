package com.gdk.shared.enums;

/**
 * Game modes that define how a game can be played.
 * Used to specify the type of gameplay (single player, multiplayer, etc.).
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 21, 2025
 * @since 1.0
 */
public enum GameMode {
    // Single Player Modes
    SINGLE_PLAYER("Single Player", "Play alone against AI or challenges"),
    STORY_MODE("Story Mode", "Narrative-driven single player experience"),
    CAMPAIGN("Campaign", "Progressive single player missions"),
    PRACTICE("Practice", "Training mode for skill development"),
    TUTORIAL("Tutorial", "Learn the game mechanics"),
    
    // Local Multiplayer Modes
    LOCAL_MULTIPLAYER("Local Multiplayer", "Multiple players on same device"),
    HOT_SEAT("Hot Seat", "Players take turns on same device"),
    SPLIT_SCREEN("Split Screen", "Multiple players with divided screen"),
    COUCH_COOP("Couch Co-op", "Cooperative play on same device"),
    LOCAL_VERSUS("Local Versus", "Competitive play on same device"),
    
    // Online Multiplayer Modes
    ONLINE_MULTIPLAYER("Online Multiplayer", "Play with others over internet"),
    RANKED("Ranked", "Competitive online play with rankings"),
    CASUAL("Casual", "Relaxed online play without rankings"),
    TOURNAMENT("Tournament", "Organized competitive events"),
    TEAM_BASED("Team Based", "Cooperative online team play"),
    
    // Special Modes
    TIME_TRIAL("Time Trial", "Complete objectives within time limit"),
    SURVIVAL("Survival", "Endless gameplay until failure"),
    PUZZLE("Puzzle", "Logic and problem-solving challenges"),
    CREATIVE("Creative", "Build and create without restrictions"),
    SANDBOX("Sandbox", "Free-form experimentation"),
    
    // AI Modes
    AI_VERSUS("AI Versus", "Play against artificial intelligence"),
    AI_COOP("AI Co-op", "Cooperate with AI against challenges"),
    AI_TRAINING("AI Training", "Practice against AI opponents");
    
    private final String displayName;
    private final String description;
    
    GameMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets the human-readable display name for this game mode.
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description of this game mode.
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the category of this game mode.
     * @return The category (Single Player, Local Multiplayer, Online Multiplayer, Special, AI)
     */
    public String getCategory() {
        if (this == SINGLE_PLAYER || this == STORY_MODE || this == CAMPAIGN || 
            this == PRACTICE || this == TUTORIAL) {
            return "Single Player";
        } else if (this == LOCAL_MULTIPLAYER || this == HOT_SEAT || this == SPLIT_SCREEN || 
                   this == COUCH_COOP || this == LOCAL_VERSUS) {
            return "Local Multiplayer";
        } else if (this == ONLINE_MULTIPLAYER || this == RANKED || this == CASUAL || 
                   this == TOURNAMENT || this == TEAM_BASED) {
            return "Online Multiplayer";
        } else if (this == TIME_TRIAL || this == SURVIVAL || this == PUZZLE || 
                   this == CREATIVE || this == SANDBOX) {
            return "Special";
        } else if (this == AI_VERSUS || this == AI_COOP || this == AI_TRAINING) {
            return "AI";
        } else {
            return "Other";
        }
    }
    
    /**
     * Gets the icon for this game mode.
     * @return The icon emoji
     */
    public String getIcon() {
        switch (this) {
            // Single Player
            case SINGLE_PLAYER: return "👤";
            case STORY_MODE: return "📖";
            case CAMPAIGN: return "🎯";
            case PRACTICE: return "🎮";
            case TUTORIAL: return "📚";
            
            // Local Multiplayer
            case LOCAL_MULTIPLAYER: return "👥";
            case HOT_SEAT: return "🪑";
            case SPLIT_SCREEN: return "📺";
            case COUCH_COOP: return "🛋️";
            case LOCAL_VERSUS: return "⚔️";
            
            // Online Multiplayer
            case ONLINE_MULTIPLAYER: return "🌐";
            case RANKED: return "🏆";
            case CASUAL: return "😊";
            case TOURNAMENT: return "🏅";
            case TEAM_BASED: return "🤝";
            
            // Special
            case TIME_TRIAL: return "⏱️";
            case SURVIVAL: return "💀";
            case PUZZLE: return "🧩";
            case CREATIVE: return "🎨";
            case SANDBOX: return "🏖️";
            
            // AI
            case AI_VERSUS: return "🤖";
            case AI_COOP: return "🤖🤝";
            case AI_TRAINING: return "🎓";
            
            default: return "🎮";
        }
    }
    
    /**
     * Gets the color code for this game mode.
     * @return The CSS color code
     */
    public String getColorCode() {
        switch (this.getCategory()) {
            case "Single Player": return "#007bff"; // Blue
            case "Local Multiplayer": return "#28a745"; // Green
            case "Online Multiplayer": return "#dc3545"; // Red
            case "Special": return "#6f42c1"; // Purple
            case "AI": return "#fd7e14"; // Orange
            default: return "#6c757d"; // Gray
        }
    }
    
    /**
     * Gets all game modes in a specific category.
     * @param category The category name
     * @return Array of game modes in that category
     */
    public static GameMode[] getModesByCategory(String category) {
        return java.util.Arrays.stream(values())
            .filter(mode -> mode.getCategory().equals(category))
            .toArray(GameMode[]::new);
    }
    
    /**
     * Gets the default game mode for a category.
     * @param category The category name
     * @return The default game mode for that category
     */
    public static GameMode getDefaultForCategory(String category) {
        switch (category) {
            case "Single Player": return SINGLE_PLAYER;
            case "Local Multiplayer": return LOCAL_MULTIPLAYER;
            case "Online Multiplayer": return ONLINE_MULTIPLAYER;
            case "Special": return PUZZLE;
            case "AI": return AI_VERSUS;
            default: return SINGLE_PLAYER;
        }
    }
    
    @Override
    public String toString() {
        return getIcon() + " " + displayName;
    }
} 