package com.gdk.shared.settings;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;

/**
 * A boolean game setting that can be configured with a checkbox.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class BooleanSetting extends GameSetting<Boolean> {
    
    /**
     * Creates a new boolean setting.
     * 
     * @param key The unique identifier for this setting
     * @param displayName The human-readable name
     * @param description The description of what this setting does
     * @param defaultValue The default value
     * @param required Whether this setting is required
     */
    public BooleanSetting(String key, String displayName, String description, boolean defaultValue, boolean required) {
        super(key, displayName, description, defaultValue, required);
    }
    
    /**
     * Creates a new boolean setting (not required by default).
     * 
     * @param key The unique identifier for this setting
     * @param displayName The human-readable name
     * @param description The description of what this setting does
     * @param defaultValue The default value
     */
    public BooleanSetting(String key, String displayName, String description, boolean defaultValue) {
        this(key, displayName, description, defaultValue, false);
    }
    
    @Override
    public Control createControl() {
        CheckBox checkBox = new CheckBox(getDisplayName());
        checkBox.setSelected(getCurrentValue());
        checkBox.setTooltip(new javafx.scene.control.Tooltip(getDescription()));
        
        // Update the setting when the checkbox changes
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            setCurrentValue(newVal);
        });
        
        return checkBox;
    }
    
    @Override
    public void updateFromControl(Control control) {
        if (control instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) control;
            setCurrentValue(checkBox.isSelected());
        }
    }
    
    @Override
    public boolean isValid() {
        // Boolean settings are always valid
        return true;
    }
    
    @Override
    public String getValidationError() {
        // Boolean settings are always valid
        return null;
    }
} 