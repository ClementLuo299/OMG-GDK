package com.game.enums;

/**
 * Game difficulty levels.
 * Used to categorize games by their complexity and challenge level.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 21, 2025
 * @since 1.0
 */
public enum GameDifficulty {
    BEGINNER("Beginner", "Perfect for new players"),
    EASY("Easy", "Simple and straightforward"),
    MEDIUM("Medium", "Balanced challenge"),
    HARD("Hard", "Requires skill and strategy"),
    EXPERT("Expert", "For experienced players"),
    MASTER("Master", "Extreme challenge"),
    NIGHTMARE("Nightmare", "Ultimate test of skill"),
    VARIABLE("Variable", "Difficulty adapts to player"),
    CUSTOM("Custom", "Player-defined settings");
    
    private final String displayName;
    private final String description;
    
    GameDifficulty(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets the human-readable display name for this difficulty level.
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description of this difficulty level.
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the numeric value for this difficulty (1-8).
     * @return The numeric difficulty value
     */
    public int getNumericValue() {
        switch (this) {
            case BEGINNER: return 1;
            case EASY: return 2;
            case MEDIUM: return 3;
            case HARD: return 4;
            case EXPERT: return 5;
            case MASTER: return 6;
            case NIGHTMARE: return 7;
            case VARIABLE: return 8;
            case CUSTOM: return 9;
            default: return 3; // Default to Medium
        }
    }
    
    /**
     * Gets a difficulty level from its numeric value.
     * @param value The numeric value (1-9)
     * @return The corresponding difficulty level
     */
    public static GameDifficulty fromNumericValue(int value) {
        switch (value) {
            case 1: return BEGINNER;
            case 2: return EASY;
            case 3: return MEDIUM;
            case 4: return HARD;
            case 5: return EXPERT;
            case 6: return MASTER;
            case 7: return NIGHTMARE;
            case 8: return VARIABLE;
            case 9: return CUSTOM;
            default: return MEDIUM;
        }
    }
    
    /**
     * Gets the color code for this difficulty level.
     * @return The CSS color code
     */
    public String getColorCode() {
        switch (this) {
            case BEGINNER: return "#28a745"; // Green
            case EASY: return "#17a2b8";     // Blue
            case MEDIUM: return "#ffc107";   // Yellow
            case HARD: return "#fd7e14";     // Orange
            case EXPERT: return "#dc3545";   // Red
            case MASTER: return "#6f42c1";   // Purple
            case NIGHTMARE: return "#000000"; // Black
            case VARIABLE: return "#6c757d"; // Gray
            case CUSTOM: return "#20c997";   // Teal
            default: return "#ffc107";       // Default to Yellow
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 