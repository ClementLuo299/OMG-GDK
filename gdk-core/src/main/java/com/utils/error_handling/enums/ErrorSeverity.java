package com.utils.error_handling.enums;

/**
 * Error severity levels for prioritization and appropriate handling.
 * Provides standardized severity levels for different types of errors in the application.
 * 
 * @authors Clement Luo
 * @date June 24, 2025
 * @edited June 24, 2025
 * @since 1.0
 */
public enum ErrorSeverity {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    private final String displayName;

    ErrorSeverity(String displayName) { this.displayName = displayName; }

    /**
     * Gets the user-friendly display name for this error severity.
     *
     * @return the display name
     */
    public String getDisplayName() { return displayName; }
} 