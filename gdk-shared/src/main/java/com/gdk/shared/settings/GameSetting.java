package com.gdk.shared.settings;

import javafx.scene.control.Control;

/**
 * Base class for game settings that can be configured by the GDK.
 * Each setting represents a configurable option for a game.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public abstract class GameSetting<T> {
    
    private final String key;
    private final String displayName;
    private final String description;
    private final T defaultValue;
    private T currentValue;
    private boolean required;
    
    /**
     * Creates a new game setting.
     * 
     * @param key The unique identifier for this setting
     * @param displayName The human-readable name
     * @param description The description of what this setting does
     * @param defaultValue The default value for this setting
     * @param required Whether this setting is required
     */
    protected GameSetting(String key, String displayName, String description, T defaultValue, boolean required) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
        this.required = required;
    }
    
    /**
     * Gets the unique key for this setting.
     * @return The setting key
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Gets the display name for this setting.
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description of this setting.
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the default value for this setting.
     * @return The default value
     */
    public T getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * Gets the current value of this setting.
     * @return The current value
     */
    public T getCurrentValue() {
        return currentValue;
    }
    
    /**
     * Sets the current value of this setting.
     * @param value The new value
     */
    public void setCurrentValue(T value) {
        this.currentValue = value;
    }
    
    /**
     * Checks if this setting is required.
     * @return true if required
     */
    public boolean isRequired() {
        return required;
    }
    
    /**
     * Creates a JavaFX control for editing this setting.
     * @return A JavaFX control
     */
    public abstract Control createControl();
    
    /**
     * Updates the setting value from the control.
     * @param control The control to read from
     */
    public abstract void updateFromControl(Control control);
    
    /**
     * Validates the current value.
     * @return true if the value is valid
     */
    public abstract boolean isValid();
    
    /**
     * Gets the validation error message if the value is invalid.
     * @return The error message, or null if valid
     */
    public abstract String getValidationError();
    
    /**
     * Resets the setting to its default value.
     */
    public void resetToDefault() {
        this.currentValue = this.defaultValue;
    }
    
    @Override
    public String toString() {
        return displayName + ": " + currentValue;
    }
} 