package gdk.infrastructure;

/**
 * Enum representing the various game modes supported by the GDK (Game Development Kit).
 * Each mode defines a distinct style of play — from solo sessions to networked multiplayer tournaments.
 *
 * @authors Clement Luo
 * @date November 7, 2025
 * @edited November 9, 2025
 * @since Beta 1.0
 */
public enum GameMode {

    /**
     * A mode where a single player plays alone.
     */
    SINGLEPLAYER("Singleplayer"),

    /**
     * A mode where multiple players connect over a network.
     */
    MULTIPLAYER("Multiplayer"),

    /**
     * A mode where the player competes against computer-controlled opponents.
     */
    COMPUTER("Computer"),

    /**
     * A mode where multiple players play on the same device, taking turns.
     */
    PASS_AND_PLAY("Pass and Play"),

    /**
     * A structured competition involving multiple rounds or matches.
     */
    TOURNAMENT("Tournament");

    // ==================== FIELDS ====================

    /** Human-readable label for the game mode (used in UI and logs). */
    private final String label;

    // ==================== CONSTRUCTOR ====================

    GameMode(String label) {
        this.label = label;
    }

    // ==================== ACCESSORS ====================

    /**
     * Returns the user-friendly label of this game mode.
     *
     * @return the display name of the mode (e.g. "Multiplayer")
     */
    public String getLabel() {
        return label;
    }

    // ==================== UTILITY ====================

    /**
     * Returns the human-readable label of the game mode.
     * This method is automatically used when the enum is converted to a string,
     * for example in logs, debug output, or UI display.
     *
     * @return the label of the game mode
     */
    @Override
    public String toString() {
        return label;
    }
}
