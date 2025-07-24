package gdk;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a game mode that can be customized by individual games.
 * This replaces the fixed enum to allow games to define their own unique modes.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class GameMode {
    
    // Common predefined modes that games can use as defaults
    public static final GameMode SINGLE_PLAYER = new GameMode("single_player", "Single Player", "Play alone against AI or challenges", "🎮", "#007bff");
    public static final GameMode LOCAL_MULTIPLAYER = new GameMode("local_multiplayer", "Local Multiplayer", "Multiple players on same device", "👥", "#28a745");
    public static final GameMode ONLINE_MULTIPLAYER = new GameMode("online_multiplayer", "Online Multiplayer", "Play with others over internet", "🌐", "#dc3545");
    public static final GameMode PRACTICE = new GameMode("practice", "Practice", "Training mode for skill development", "🎯", "#6f42c1");
    public static final GameMode TUTORIAL = new GameMode("tutorial", "Tutorial", "Learn the game mechanics", "📚", "#fd7e14");
    public static final GameMode HOT_SEAT = new GameMode("hot_seat", "Hot Seat", "Players take turns on same device", "🪑", "#28a745");
    public static final GameMode RANKED = new GameMode("ranked", "Ranked", "Competitive online play with rankings", "🏆", "#dc3545");
    public static final GameMode CASUAL = new GameMode("casual", "Casual", "Relaxed online play without rankings", "😊", "#dc3545");
    public static final GameMode TIME_TRIAL = new GameMode("time_trial", "Time Trial", "Complete objectives within time limit", "⏱️", "#6f42c1");
    public static final GameMode SURVIVAL = new GameMode("survival", "Survival", "Endless gameplay until failure", "💀", "#6f42c1");
    public static final GameMode PUZZLE = new GameMode("puzzle", "Puzzle", "Logic and problem-solving challenges", "🧩", "#6f42c1");
    public static final GameMode CREATIVE = new GameMode("creative", "Creative", "Build and create without restrictions", "🎨", "#6f42c1");
    public static final GameMode SANDBOX = new GameMode("sandbox", "Sandbox", "Free-form experimentation", "🏖️", "#6f42c1");
    public static final GameMode AI_VERSUS = new GameMode("ai_versus", "AI Versus", "Play against artificial intelligence", "🤖", "#fd7e14");
    public static final GameMode AI_COOP = new GameMode("ai_coop", "AI Co-op", "Cooperate with AI against challenges", "🤖🤝", "#fd7e14");
    
    private final String id;
    private final String displayName;
    private final String description;
    private final String icon;
    private final String colorCode;
    private final Map<String, Object> properties;
    
    /**
     * Creates a new game mode.
     * 
     * @param id Unique identifier for the mode
     * @param displayName Human-readable name
     * @param description Description of the mode
     * @param icon Icon emoji or symbol
     * @param colorCode CSS color code for UI theming
     */
    public GameMode(String id, String displayName, String description, String icon, String colorCode) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.colorCode = colorCode;
        this.properties = new HashMap<>();
    }
    
    /**
     * Creates a new game mode with custom properties.
     * 
     * @param id Unique identifier for the mode
     * @param displayName Human-readable name
     * @param description Description of the mode
     * @param icon Icon emoji or symbol
     * @param colorCode CSS color code for UI theming
     * @param properties Custom properties for this mode
     */
    public GameMode(String id, String displayName, String description, String icon, String colorCode, Map<String, Object> properties) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.colorCode = colorCode;
        this.properties = new HashMap<>(properties);
    }
    
    /**
     * Gets the category of this game mode.
     * @return The category (Single Player, Local Multiplayer, Online Multiplayer, Special, AI)
     */
    public String getCategory() {
        // First check if category is explicitly set in properties
        String category = getProperty("category", null);
        if (category != null) {
            return category;
        }
        
        // Fallback: determine category based on the mode
        if (this == SINGLE_PLAYER || this == PRACTICE || this == TUTORIAL) {
            return "Single Player";
        } else if (this == LOCAL_MULTIPLAYER || this == HOT_SEAT) {
            return "Local Multiplayer";
        } else if (this == ONLINE_MULTIPLAYER || this == RANKED || this == CASUAL) {
            return "Online Multiplayer";
        } else if (this == TIME_TRIAL || this == SURVIVAL || this == PUZZLE || 
                   this == CREATIVE || this == SANDBOX) {
            return "Special";
        } else if (this == AI_VERSUS || this == AI_COOP) {
            return "AI";
        } else {
            return "Other";
        }
    }
    
    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public String getColorCode() { return colorCode; }
    
    /**
     * Gets a custom property for this game mode.
     * @param key The property key
     * @param defaultValue Default value if property doesn't exist
     * @return The property value
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        return (T) properties.getOrDefault(key, defaultValue);
    }
    
    /**
     * Sets a custom property for this game mode.
     * @param key The property key
     * @param value The property value
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    /**
     * Checks if this mode has a specific property.
     * @param key The property key
     * @return true if the property exists
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Gets all custom properties for this mode.
     * @return Map of all properties
     */
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GameMode gameMode = (GameMode) obj;
        return id.equals(gameMode.id);
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