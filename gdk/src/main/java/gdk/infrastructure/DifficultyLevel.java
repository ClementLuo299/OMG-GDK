package gdk.infrastructure;

/**
 * Enum for game mode
 *
 * @authors Clement Luo
 * @date November 9, 2025
 * @edited November 9, 2025
 * @since Beta 1.0
 */
public enum DifficultyLevel {
    /**
     * Easy difficulty.
     */
    EASY(1, "Easy", true),

    /**
     * Normal difficulty.
     */
    NORMAL(2, "Normal", true),

    /**
     * Hard difficulty.
     */
    HARD(3, "Hard", true),

    /**
     * Expert difficulty.
     */
    EXPERT(4, "Expert", true),

    /**
     * Adaptive difficulty.
     */
    ADAPTIVE(0, "Adaptive", false); // not ranked

    //Difficulty rank
    private final int rank;

    //Name of difficulty level
    private final String label;

    //Whether a difficulty level should be included in the ranking
    private final boolean ranked;

    /**
     * Enum constructor and utility methods for representing and comparing difficulty levels.
     * Each difficulty level has a numeric rank (used for ordering), a display label, and a flag
     * indicating whether it participates in ranking comparisons (e.g. Adaptive may not be ranked).
     */
    DifficultyLevel(int rank, String label, boolean ranked) {
        this.rank = rank;
        this.label = label;
        this.ranked = ranked;
    }

    /**
     * Returns whether this difficulty level is part of the ranked order.
     *
     * @return true if ranked and can be compared with others, false otherwise
     */
    public boolean isRanked() {
        return ranked;
    }

    /**
     * Returns the numeric rank associated with this difficulty.
     * Lower numbers represent easier difficulties.
     *
     * @return the difficulty rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Returns the display label of this difficulty (e.g. "Easy", "Normal", "Hard").
     *
     * @return the display label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Checks whether this difficulty is harder than another difficulty.
     * Only works if both difficulties are ranked.
     *
     * @param other the difficulty to compare against
     * @return true if this difficulty is ranked and has a higher rank number
     */
    public boolean isHarderThan(DifficultyLevel other) {
        // Skip comparison if either difficulty is unranked (e.g. Adaptive)
        if (!this.ranked || !other.ranked) return false;

        // A higher rank value means a harder difficulty
        return this.rank > other.rank;
    }

    /**
     * Checks whether this difficulty is easier than another difficulty.
     * Only works if both difficulties are ranked.
     *
     * @param other the difficulty to compare against
     * @return true if this difficulty is ranked and has a lower rank number
     */
    public boolean isEasierThan(DifficultyLevel other) {
        if (!this.ranked || !other.ranked) return false;
        return this.rank < other.rank;
    }

    /**
     * Returns the user-friendly label of this difficulty level.
     * This method is automatically called when converting the enum to a string,
     * e.g., in logging, UI display, or concatenation.
     *
     * @return the label (e.g. "Hard" instead of "HARD")
     */
    @Override
    public String toString() {
        return label;
    }
}
