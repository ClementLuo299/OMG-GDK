package com.gdk.shared.game;

import com.gdk.shared.enums.GameMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the state of a game for saving and loading.
 * Contains all necessary information to restore a game to a specific point.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @since 1.0
 */
public class GameState {
    
    private final String gameId;
    private final String gameName;
    private final GameMode gameMode;
    private final int playerCount;
    private final GameOptions gameOptions;
    private final Map<String, Object> stateData;
    private final long timestamp;
    
    /**
     * Creates a new GameState with basic information.
     * 
     * @param gameId The unique identifier for the game
     * @param gameName The display name of the game
     * @param gameMode The game mode being played
     * @param playerCount The number of players
     * @param gameOptions The game options used
     */
    public GameState(String gameId, String gameName, GameMode gameMode, int playerCount, GameOptions gameOptions) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.gameMode = gameMode;
        this.playerCount = playerCount;
        this.gameOptions = gameOptions;
        this.stateData = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Creates a new GameState with all information including state data.
     * 
     * @param gameId The unique identifier for the game
     * @param gameName The display name of the game
     * @param gameMode The game mode being played
     * @param playerCount The number of players
     * @param gameOptions The game options used
     * @param stateData Additional state data
     */
    public GameState(String gameId, String gameName, GameMode gameMode, int playerCount, GameOptions gameOptions, Map<String, Object> stateData) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.gameMode = gameMode;
        this.playerCount = playerCount;
        this.gameOptions = gameOptions;
        this.stateData = new HashMap<>(stateData);
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Gets the game ID.
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
     * Gets the game mode.
     * @return The game mode
     */
    public GameMode getGameMode() {
        return gameMode;
    }
    
    /**
     * Gets the player count.
     * @return The player count
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
     * Gets the timestamp when this state was created.
     * @return The timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
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
     * Gets a state value as a string.
     * @param key The state key
     * @param defaultValue Default value if not found
     * @return The state value as a string
     */
    public String getStringStateValue(String key, String defaultValue) {
        Object value = stateData.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * Gets a state value as an integer.
     * @param key The state key
     * @param defaultValue Default value if not found
     * @return The state value as an integer
     */
    public int getIntStateValue(String key, int defaultValue) {
        Object value = stateData.get(key);
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
     * Gets a state value as a boolean.
     * @param key The state key
     * @param defaultValue Default value if not found
     * @return The state value as a boolean
     */
    public boolean getBooleanStateValue(String key, boolean defaultValue) {
        Object value = stateData.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
    
    /**
     * Gets a state value as an object.
     * @param key The state key
     * @param defaultValue Default value if not found
     * @return The state value as an object
     */
    @SuppressWarnings("unchecked")
    public <T> T getStateValue(String key, T defaultValue) {
        Object value = stateData.get(key);
        return value != null ? (T) value : defaultValue;
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
     * @return A copy of all state data
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
     * @return The number of state values
     */
    public int getStateDataSize() {
        return stateData.size();
    }
    
    /**
     * Checks if there are no state values.
     * @return true if there are no state values
     */
    public boolean isStateDataEmpty() {
        return stateData.isEmpty();
    }
    
    @Override
    public String toString() {
        return "GameState{" +
                "gameId='" + gameId + '\'' +
                ", gameName='" + gameName + '\'' +
                ", gameMode=" + gameMode +
                ", playerCount=" + playerCount +
                ", gameOptions=" + gameOptions +
                ", stateData=" + stateData +
                ", timestamp=" + timestamp +
                '}';
    }
} 