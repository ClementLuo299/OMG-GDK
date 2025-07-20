package com.game;

import java.time.LocalDateTime;

/**
 * Represents an event that occurs during gameplay.
 * Games use this to communicate state changes to the GDK.
 */
public class GameEvent {
    
    public enum EventType {
        GAME_STARTED,
        GAME_ENDED,
        MOVE_MADE,
        PLAYER_TURN_CHANGED,
        GAME_PAUSED,
        GAME_RESUMED,
        PLAYER_FORFEITED,
        ERROR_OCCURRED,
        BACK_TO_LOBBY_REQUESTED
    }
    
    private final EventType type;
    private final String gameId;
    private final String message;
    private final Object data;
    private final LocalDateTime timestamp;
    
    public GameEvent(EventType type, String gameId, String message) {
        this(type, gameId, message, null);
    }
    
    public GameEvent(EventType type, String gameId, String message, Object data) {
        this.type = type;
        this.gameId = gameId;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    public EventType getType() {
        return type;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("GameEvent{type=%s, gameId='%s', message='%s', timestamp=%s}", 
                           type, gameId, message, timestamp);
    }
} 