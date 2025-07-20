package com.game;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for game-specific options and configuration.
 * Allows games to define custom parameters while maintaining a common interface.
 *
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class GameOptions {
    
    private final Map<String, Object> options;
    
    public GameOptions() {
        this.options = new HashMap<>();
    }
    
    /**
     * Sets a game option.
     * @param key The option key
     * @param value The option value
     */
    public void setOption(String key, Object value) {
        options.put(key, value);
    }
    
    /**
     * Gets a game option.
     * @param key The option key
     * @return The option value, or null if not found
     */
    public Object getOption(String key) {
        return options.get(key);
    }
    
    /**
     * Gets a game option with a default value.
     * @param key The option key
     * @param defaultValue The default value if option not found
     * @return The option value or default value
     */
    public Object getOption(String key, Object defaultValue) {
        return options.getOrDefault(key, defaultValue);
    }
    
    /**
     * Gets a string option.
     * @param key The option key
     * @return The string value, or null if not found
     */
    public String getStringOption(String key) {
        Object value = options.get(key);
        return value instanceof String ? (String) value : null;
    }
    
    /**
     * Gets a string option with a default value.
     * @param key The option key
     * @param defaultValue The default value
     * @return The string value or default value
     */
    public String getStringOption(String key, String defaultValue) {
        String value = getStringOption(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Gets an integer option.
     * @param key The option key
     * @return The integer value, or null if not found
     */
    public Integer getIntOption(String key) {
        Object value = options.get(key);
        return value instanceof Integer ? (Integer) value : null;
    }
    
    /**
     * Gets an integer option with a default value.
     * @param key The option key
     * @param defaultValue The default value
     * @return The integer value or default value
     */
    public int getIntOption(String key, int defaultValue) {
        Integer value = getIntOption(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Gets a boolean option.
     * @param key The option key
     * @return The boolean value, or null if not found
     */
    public Boolean getBooleanOption(String key) {
        Object value = options.get(key);
        return value instanceof Boolean ? (Boolean) value : null;
    }
    
    /**
     * Gets a boolean option with a default value.
     * @param key The option key
     * @param defaultValue The default value
     * @return The boolean value or default value
     */
    public boolean getBooleanOption(String key, boolean defaultValue) {
        Boolean value = getBooleanOption(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Checks if an option exists.
     * @param key The option key
     * @return true if the option exists
     */
    public boolean hasOption(String key) {
        return options.containsKey(key);
    }
    
    /**
     * Removes an option.
     * @param key The option key
     */
    public void removeOption(String key) {
        options.remove(key);
    }
    
    /**
     * Gets all options as a map.
     * @return Map of all options
     */
    public Map<String, Object> getAllOptions() {
        return new HashMap<>(options);
    }
    
    /**
     * Clears all options.
     */
    public void clear() {
        options.clear();
    }
    
    /**
     * Gets the number of options.
     * @return Number of options
     */
    public int size() {
        return options.size();
    }
    
    /**
     * Checks if there are no options.
     * @return true if no options exist
     */
    public boolean isEmpty() {
        return options.isEmpty();
    }
} 