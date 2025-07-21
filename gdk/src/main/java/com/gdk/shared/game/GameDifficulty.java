package com.gdk.shared.game;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a game difficulty level that can be customized by individual games.
 * This replaces the fixed enum to allow games to define their own unique difficulty levels.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class GameDifficulty {
    
    // Common predefined difficulties that games can use as defaults
    public static final GameDifficulty EASY = new GameDifficulty("easy", "Easy", "Suitable for beginners", "😊", "#28a745", 1);
    public static final GameDifficulty MEDIUM = new GameDifficulty("medium", "Medium", "Balanced challenge", "😐", "#ffc107", 2);
    public static final GameDifficulty HARD = new GameDifficulty("hard", "Hard", "Challenging for experienced players", "😰", "#fd7e14", 3);
    public static final GameDifficulty EXPERT = new GameDifficulty("expert", "Expert", "Extremely challenging", "😱", "#dc3545", 4);
    public static final GameDifficulty NIGHTMARE = new GameDifficulty("nightmare", "Nightmare", "Nearly impossible", "💀", "#6f42c1", 5);
    public static final GameDifficulty CUSTOM = new GameDifficulty("custom", "Custom", "User-defined difficulty", "⚙️", "#6c757d", 0);
    
    private final String id;
    private final String displayName;
    private final String description;
    private final String icon;
    private final String colorCode;
    private final int level;
    private final Map<String, Object> properties;
    
    /**
     * Creates a new game difficulty.
     * 
     * @param id Unique identifier for the difficulty
     * @param displayName Human-readable name
     * @param description Description of the difficulty
     * @param icon Icon emoji or symbol
     * @param colorCode CSS color code for UI theming
     * @param level Numeric difficulty level (1=easiest, higher=harder)
     */
    public GameDifficulty(String id, String displayName, String description, String icon, String colorCode, int level) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.colorCode = colorCode;
        this.level = level;
        this.properties = new HashMap<>();
    }
    
    /**
     * Creates a new game difficulty with custom properties.
     * 
     * @param id Unique identifier for the difficulty
     * @param displayName Human-readable name
     * @param description Description of the difficulty
     * @param icon Icon emoji or symbol
     * @param colorCode CSS color code for UI theming
     * @param level Numeric difficulty level (1=easiest, higher=harder)
     * @param properties Custom properties for this difficulty
     */
    public GameDifficulty(String id, String displayName, String description, String icon, String colorCode, int level, Map<String, Object> properties) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.colorCode = colorCode;
        this.level = level;
        this.properties = new HashMap<>(properties);
    }
    
    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public String getColorCode() { return colorCode; }
    public int getLevel() { return level; }
    
    /**
     * Gets a custom property for this difficulty.
     * @param key The property key
     * @param defaultValue Default value if property doesn't exist
     * @return The property value
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        return (T) properties.getOrDefault(key, defaultValue);
    }
    
    /**
     * Sets a custom property for this difficulty.
     * @param key The property key
     * @param value The property value
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    /**
     * Checks if this difficulty has a specific property.
     * @param key The property key
     * @return true if the property exists
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Gets all custom properties for this difficulty.
     * @return Map of all properties
     */
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }
    
    /**
     * Compares this difficulty with another based on level.
     * @param other The other difficulty to compare with
     * @return negative if this is easier, positive if harder, 0 if same level
     */
    public int compareTo(GameDifficulty other) {
        return Integer.compare(this.level, other.level);
    }
    
    /**
     * Checks if this difficulty is harder than another.
     * @param other The other difficulty to compare with
     * @return true if this difficulty is harder
     */
    public boolean isHarderThan(GameDifficulty other) {
        return this.level > other.level;
    }
    
    /**
     * Checks if this difficulty is easier than another.
     * @param other The other difficulty to compare with
     * @return true if this difficulty is easier
     */
    public boolean isEasierThan(GameDifficulty other) {
        return this.level < other.level;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GameDifficulty that = (GameDifficulty) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return icon + " " + displayName;
    }
} 