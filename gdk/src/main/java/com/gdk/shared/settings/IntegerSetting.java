package com.gdk.shared.settings;

import javafx.scene.control.Control;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * An integer game setting that can be configured with a spinner.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class IntegerSetting extends GameSetting<Integer> {
    
    private final int minValue;
    private final int maxValue;
    
    /**
     * Creates a new integer setting.
     * 
     * @param key The unique identifier for this setting
     * @param displayName The human-readable name
     * @param description The description of what this setting does
     * @param defaultValue The default value
     * @param minValue The minimum allowed value
     * @param maxValue The maximum allowed value
     * @param required Whether this setting is required
     */
    public IntegerSetting(String key, String displayName, String description, int defaultValue, int minValue, int maxValue, boolean required) {
        super(key, displayName, description, defaultValue, required);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    /**
     * Creates a new integer setting (not required by default).
     * 
     * @param key The unique identifier for this setting
     * @param displayName The human-readable name
     * @param description The description of what this setting does
     * @param defaultValue The default value
     * @param minValue The minimum allowed value
     * @param maxValue The maximum allowed value
     */
    public IntegerSetting(String key, String displayName, String description, int defaultValue, int minValue, int maxValue) {
        this(key, displayName, description, defaultValue, minValue, maxValue, false);
    }
    
    /**
     * Gets the minimum allowed value.
     * @return The minimum value
     */
    public int getMinValue() {
        return minValue;
    }
    
    /**
     * Gets the maximum allowed value.
     * @return The maximum value
     */
    public int getMaxValue() {
        return maxValue;
    }
    
    @Override
    public Control createControl() {
        Spinner<Integer> spinner = new Spinner<>();
        SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
            minValue, maxValue, getCurrentValue()
        );
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        spinner.setTooltip(new javafx.scene.control.Tooltip(getDescription()));
        
        // Update the setting when the spinner changes
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            setCurrentValue(newVal);
        });
        
        return spinner;
    }
    
    @Override
    public void updateFromControl(Control control) {
        if (control instanceof Spinner) {
            Spinner<Integer> spinner = (Spinner<Integer>) control;
            setCurrentValue(spinner.getValue());
        }
    }
    
    @Override
    public boolean isValid() {
        int value = getCurrentValue();
        return value >= minValue && value <= maxValue;
    }
    
    @Override
    public String getValidationError() {
        if (!isValid()) {
            return "Value must be between " + minValue + " and " + maxValue;
        }
        return null;
    }
} 