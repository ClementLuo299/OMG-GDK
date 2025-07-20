package com.game.enums;

/**
 * Game modes that define how a game can be played.
 * Used to specify the type of gameplay (single player, multiplayer, etc.).
 *
 * @authors Clement Luo
 * @date July 19, 2025
 * @edited July 19, 2025
 * @since 1.0
 */
public enum GameMode {
    SINGLE_PLAYER("Single Player"),
    LOCAL_MULTIPLAYER("Local Multiplayer"),
    ONLINE_MULTIPLAYER("Online Multiplayer");
    
    private final String displayName;
    
    GameMode(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the human-readable display name for this game mode.
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
} 