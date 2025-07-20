package com.game.enums;

/**
 * Game difficulty levels.
 * Used to categorize games by their complexity and challenge level.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 19, 2025
 * @since 1.0
 */
public enum GameDifficulty {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    VARIABLE("Variable");
    
    private final String displayName;
    
    GameDifficulty(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the human-readable display name for this difficulty level.
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
} 