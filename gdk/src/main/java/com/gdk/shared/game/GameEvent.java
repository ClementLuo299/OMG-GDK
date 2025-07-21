package com.gdk.shared.game;

/**
 * Represents an event that can be sent from a game module to the GDK.
 * Used for communication between games and the main application.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @since 1.0
 */
public class GameEvent {
    
    /**
     * Types of events that can be sent.
     */
    public enum EventType {
        GAME_STARTED("Game Started"),
        GAME_ENDED("Game Ended"),
        MOVE_MADE("Move Made"),
        PLAYER_TURN_CHANGED("Player Turn Changed"),
        ERROR_OCCURRED("Error Occurred"),
        BACK_TO_LOBBY_REQUESTED("Back to Lobby Requested"),
        GAME_PAUSED("Game Paused"),
        GAME_RESUMED("Game Resumed"),
        PLAYER_JOINED("Player Joined"),
        PLAYER_LEFT("Player Left"),
        SCORE_UPDATED("Score Updated"),
        LEVEL_COMPLETED("Level Completed"),
        ACHIEVEMENT_UNLOCKED("Achievement Unlocked"),
        CUSTOM_EVENT("Custom Event");
        
        private final String displayName;
        
        EventType(String displayName) {
            this.displayName = displayName;
        }
        
        /**
         * Gets the display name for this event type.
         * @return The display name
         */
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final EventType eventType;
    private final String gameId;
    private final String message;
    private final Object data;
    private final long timestamp;
    
    /**
     * Creates a new GameEvent with basic information.
     * 
     * @param eventType The type of event
     * @param gameId The ID of the game sending the event
     * @param message A human-readable message describing the event
     */
    public GameEvent(EventType eventType, String gameId, String message) {
        this(eventType, gameId, message, null);
    }
    
    /**
     * Creates a new GameEvent with additional data.
     * 
     * @param eventType The type of event
     * @param gameId The ID of the game sending the event
     * @param message A human-readable message describing the event
     * @param data Additional data associated with the event
     */
    public GameEvent(EventType eventType, String gameId, String message, Object data) {
        this.eventType = eventType;
        this.gameId = gameId;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Gets the event type.
     * @return The event type
     */
    public EventType getEventType() {
        return eventType;
    }
    
    /**
     * Gets the game ID.
     * @return The game ID
     */
    public String getGameId() {
        return gameId;
    }
    
    /**
     * Gets the event message.
     * @return The event message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the event data.
     * @return The event data, or null if no data
     */
    public Object getData() {
        return data;
    }
    
    /**
     * Gets the timestamp when this event was created.
     * @return The timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the event data as a specific type.
     * @param defaultValue Default value if data is null or wrong type
     * @return The event data cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> T getDataAs(Class<T> type, T defaultValue) {
        if (data != null && type.isInstance(data)) {
            return (T) data;
        }
        return defaultValue;
    }
    
    /**
     * Gets the event data as a string.
     * @param defaultValue Default value if data is null
     * @return The event data as a string
     */
    public String getDataAsString(String defaultValue) {
        return data != null ? data.toString() : defaultValue;
    }
    
    /**
     * Gets the event data as an integer.
     * @param defaultValue Default value if data is null or not a number
     * @return The event data as an integer
     */
    public int getDataAsInt(int defaultValue) {
        if (data instanceof Number) {
            return ((Number) data).intValue();
        } else if (data instanceof String) {
            try {
                return Integer.parseInt((String) data);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Gets the event data as a boolean.
     * @param defaultValue Default value if data is null
     * @return The event data as a boolean
     */
    public boolean getDataAsBoolean(boolean defaultValue) {
        if (data instanceof Boolean) {
            return (Boolean) data;
        } else if (data instanceof String) {
            return Boolean.parseBoolean((String) data);
        }
        return defaultValue;
    }
    
    @Override
    public String toString() {
        return "GameEvent{" +
                "eventType=" + eventType +
                ", gameId='" + gameId + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
} 