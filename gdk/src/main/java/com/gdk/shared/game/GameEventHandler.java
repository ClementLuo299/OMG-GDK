package com.gdk.shared.game;

/**
 * Interface for handling game events.
 * Implemented by the GDK to receive events from game modules.
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @since 1.0
 */
@FunctionalInterface
public interface GameEventHandler {
    
    /**
     * Handles a game event.
     * This method is called by game modules to communicate with the GDK.
     * 
     * @param event The game event to handle
     */
    void handleGameEvent(GameEvent event);
} 