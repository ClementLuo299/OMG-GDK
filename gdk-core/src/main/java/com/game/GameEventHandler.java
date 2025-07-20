package com.game;

/**
 * Interface for handling game events.
 * Games use this to communicate state changes back to the GDK.
 */
@FunctionalInterface
public interface GameEventHandler {
    
    /**
     * Handle a game event.
     * 
     * @param event The game event that occurred
     */
    void handleGameEvent(GameEvent event);
} 