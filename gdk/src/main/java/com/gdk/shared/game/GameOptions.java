package com.gdk.shared.game;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for game-specific options and configuration.
 * Used to pass additional settings to games when they are launched.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @since 1.0
 */
public class GameOptions {
    
    private final Map<String, Object> options;
    
    /**
     * Creates a new GameOptions instance with no options.
     */
    public GameOptions() {
        this.options = new HashMap<>();
    }
    
    /**
     * Creates a new GameOptions instance with initial options.
     * @param initialOptions Initial options to set
     */
    public GameOptions(Map<String, Object> initialOptions) {
        this.options = new HashMap<>(initialOptions);
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
     * Gets a game option as a string.
     * @param key The option key
     * @param defaultValue Default value if option not found
     * @return The option value as a string
     */
    public String getStringOption(String key, String defaultValue) {
        Object value = options.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * Gets a game option as an integer.
     * @param key The option key
     * @param defaultValue Default value if option not found
     * @return The option value as an integer
     */
    public int getIntOption(String key, int defaultValue) {
        Object value = options.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Gets a game option as a boolean.
     * @param key The option key
     * @param defaultValue Default value if option not found
     * @return The option value as a boolean
     */
    public boolean getBooleanOption(String key, boolean defaultValue) {
        Object value = options.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
    
    /**
     * Gets a game option as an object.
     * @param key The option key
     * @param defaultValue Default value if option not found
     * @return The option value as an object
     */
    @SuppressWarnings("unchecked")
    public <T> T getOption(String key, T defaultValue) {
        Object value = options.get(key);
        return value != null ? (T) value : defaultValue;
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
     * Removes a game option.
     * @param key The option key
     */
    public void removeOption(String key) {
        options.remove(key);
    }
    
    /**
     * Gets all options as a map.
     * @return A copy of all options
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
     * @return The number of options
     */
    public int size() {
        return options.size();
    }
    
    /**
     * Checks if there are no options.
     * @return true if there are no options
     */
    public boolean isEmpty() {
        return options.isEmpty();
    }
    
    @Override
    public String toString() {
        return "GameOptions{" + options + "}";
    }
} 