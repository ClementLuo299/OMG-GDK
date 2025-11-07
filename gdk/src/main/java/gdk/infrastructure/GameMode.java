package gdk.infrastructure;

/**
 * Enum for game mode
 *
 * @authors Clement Luo
 * @date November 7, 2025
 * @edited November 7, 2025
 * @since Beta 1.0
 */
public enum GameMode {
    /**
     * A mode where a single player plays alone.
     */
    SINGLEPLAYER,

    /**
     * A mode where multiple players connect over a network.
     */
    MULTIPLAYER,

    /**
     * A mode where the player competes against computer-controlled opponents.
     */
    COMPUTER,

    /**
     * A mode where multiple players play on the same device or local network.
     */
    LOCAL_MULTIPLAYER,

    /**
     * A structured competition involving multiple rounds or matches.
     */
    TOURNAMENT
}
