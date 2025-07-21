package com.utils.error_handling.enums;

/**
 * Error categories for better error management and classification.
 * Provides standardized categories for different types of errors in the application.
 * 
 * @authors Clement Luo
 * @date June 24, 2025
 * @edited June 24, 2025
 * @since 1.0
 */
public enum ErrorCategory {
    NETWORK("Network Error"),
    DATABASE("Database Error"),
    FILE_IO("File I/O Error"),
    VALIDATION("Validation Error"),
    AUTHENTICATION("Authentication Error"),
    AUTHORIZATION("Authorization Error"),
    CONFIGURATION("Configuration Error"),
    RESOURCE("Resource Error"),
    TIMEOUT("Timeout Error"),
    SYSTEM("System Error"),
    UNKNOWN("Unknown Error");

    private final String displayName;

    ErrorCategory(String displayName) { this.displayName = displayName; }

    /**
     * Gets the user-friendly display name for this error category.
     *
     * @return the display name
     */
    public String getDisplayName() { return displayName; }
} 