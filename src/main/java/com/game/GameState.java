package com.game;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.game.enums.GameMode;

/**
 * Represents the state of a game for saving and loading purposes.
 * Each game can define its own state structure while using this common interface.
 *
 * @authors Clement Luo
 * @date January 2025
 * @since 1.0
 */
public class GameState {
    
    private final String gameId;
    private final String gameName;
    private final LocalDateTime saveTime;
    private final Map<String, Object> stateData;
    private final GameMode gameMode;
    private final int playerCount;
    private final GameOptions gameOptions;
    
    /**
     * Creates a new game state.
     * 
     * @param gameId The game identifier
     * @param gameName The game name
     * @param gameMode The game mode
     * @param playerCount Number of players
     * @param gameOptions Game options
     */
    public GameState(String gameId, String gameName, GameMode gameMode, 
                    int playerCount, GameOptions gameOptions) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.saveTime = LocalDateTime.now();
        this.stateData = new HashMap<>();
        this.gameMode = gameMode;
        this.playerCount = playerCount;
        this.gameOptions = gameOptions;
    }
    
    /**
     * Gets the game identifier.
     * @return The game ID
     */
    public String getGameId() {
        return gameId;
    }
    
    /**
     * Gets the game name.
     * @return The game name
     */
    public String getGameName() {
        return gameName;
    }
    
    /**
     * Gets the save time.
     * @return When the game was saved
     */
    public LocalDateTime getSaveTime() {
        return saveTime;
    }
    
    /**
     * Gets the game mode.
     * @return The game mode
     */
    public GameMode getGameMode() {
        return gameMode;
    }
    
    /**
     * Gets the player count.
     * @return Number of players
     */
    public int getPlayerCount() {
        return playerCount;
    }
    
    /**
     * Gets the game options.
     * @return The game options
     */
    public GameOptions getGameOptions() {
        return gameOptions;
    }
    
    /**
     * Sets a state value.
     * @param key The state key
     * @param value The state value
     */
    public void setStateValue(String key, Object value) {
        stateData.put(key, value);
    }
    
    /**
     * Gets a state value.
     * @param key The state key
     * @return The state value, or null if not found
     */
    public Object getStateValue(String key) {
        return stateData.get(key);
    }
    
    /**
     * Gets a state value with a default value.
     * @param key The state key
     * @param defaultValue The default value
     * @return The state value or default value
     */
    public Object getStateValue(String key, Object defaultValue) {
        return stateData.getOrDefault(key, defaultValue);
    }
    
    /**
     * Gets a string state value.
     * @param key The state key
     * @return The string value, or null if not found
     */
    public String getStringStateValue(String key) {
        Object value = stateData.get(key);
        return value instanceof String ? (String) value : null;
    }
    
    /**
     * Gets a string state value with a default value.
     * @param key The state key
     * @param defaultValue The default value
     * @return The string value or default value
     */
    public String getStringStateValue(String key, String defaultValue) {
        String value = getStringStateValue(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Gets an integer state value.
     * @param key The state key
     * @return The integer value, or null if not found
     */
    public Integer getIntStateValue(String key) {
        Object value = stateData.get(key);
        return value instanceof Integer ? (Integer) value : null;
    }
    
    /**
     * Gets an integer state value with a default value.
     * @param key The state key
     * @param defaultValue The default value
     * @return The integer value or default value
     */
    public int getIntStateValue(String key, int defaultValue) {
        Integer value = getIntStateValue(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Gets a boolean state value.
     * @param key The state key
     * @return The boolean value, or null if not found
     */
    public Boolean getBooleanStateValue(String key) {
        Object value = stateData.get(key);
        return value instanceof Boolean ? (Boolean) value : null;
    }
    
    /**
     * Gets a boolean state value with a default value.
     * @param key The state key
     * @param defaultValue The default value
     * @return The boolean value or default value
     */
    public boolean getBooleanStateValue(String key, boolean defaultValue) {
        Boolean value = getBooleanStateValue(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Checks if a state value exists.
     * @param key The state key
     * @return true if the state value exists
     */
    public boolean hasStateValue(String key) {
        return stateData.containsKey(key);
    }
    
    /**
     * Removes a state value.
     * @param key The state key
     */
    public void removeStateValue(String key) {
        stateData.remove(key);
    }
    
    /**
     * Gets all state data as a map.
     * @return Map of all state data
     */
    public Map<String, Object> getAllStateData() {
        return new HashMap<>(stateData);
    }
    
    /**
     * Clears all state data.
     */
    public void clearStateData() {
        stateData.clear();
    }
    
    /**
     * Gets the number of state values.
     * @return Number of state values
     */
    public int getStateDataSize() {
        return stateData.size();
    }
    
    /**
     * Checks if there are no state values.
     * @return true if no state values exist
     */
    public boolean isStateDataEmpty() {
        return stateData.isEmpty();
    }
    
    /**
     * Gets a formatted save time string.
     * @return Formatted save time
     */
    public String getFormattedSaveTime() {
        return saveTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * Gets a display name for the saved game.
     * @return Display name
     */
    public String getDisplayName() {
        return String.format("%s - %s (%d players) - %s", 
                           gameName, gameMode.getDisplayName(), playerCount, getFormattedSaveTime());
    }
} 