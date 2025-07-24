package gdk;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;

/**
 * A string game setting that can be configured with a text field.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class StringSetting extends GameSetting<String> {
    
    private final int maxLength;
    private final String pattern;
    
    /**
     * Creates a new string setting.
     * 
     * @param key The unique identifier for this setting
     * @param displayName The human-readable name
     * @param description The description of what this setting does
     * @param defaultValue The default value
     * @param maxLength The maximum allowed length (0 for no limit)
     * @param pattern The regex pattern for validation (null for no pattern)
     * @param required Whether this setting is required
     */
    public StringSetting(String key, String displayName, String description, String defaultValue, int maxLength, String pattern, boolean required) {
        super(key, displayName, description, defaultValue, required);
        this.maxLength = maxLength;
        this.pattern = pattern;
    }
    
    /**
     * Creates a new string setting (not required by default).
     * 
     * @param key The unique identifier for this setting
     * @param displayName The human-readable name
     * @param description The description of what this setting does
     * @param defaultValue The default value
     * @param maxLength The maximum allowed length (0 for no limit)
     * @param pattern The regex pattern for validation (null for no pattern)
     */
    public StringSetting(String key, String displayName, String description, String defaultValue, int maxLength, String pattern) {
        this(key, displayName, description, defaultValue, maxLength, pattern, false);
    }
    
    /**
     * Creates a new string setting with no length limit or pattern.
     * 
     * @param key The unique identifier for this setting
     * @param displayName The human-readable name
     * @param description The description of what this setting does
     * @param defaultValue The default value
     */
    public StringSetting(String key, String displayName, String description, String defaultValue) {
        this(key, displayName, description, defaultValue, 0, null, false);
    }
    
    /**
     * Gets the maximum allowed length.
     * @return The maximum length (0 for no limit)
     */
    public int getMaxLength() {
        return maxLength;
    }
    
    /**
     * Gets the validation pattern.
     * @return The regex pattern (null for no pattern)
     */
    public String getPattern() {
        return pattern;
    }
    
    @Override
    public Control createControl() {
        TextField textField = new TextField(getCurrentValue());
        textField.setPromptText("Enter " + getDisplayName().toLowerCase());
        textField.setTooltip(new javafx.scene.control.Tooltip(getDescription()));
        
        // Set max length if specified
        if (maxLength > 0) {
            textField.setPrefColumnCount(Math.min(maxLength, 20));
        }
        
        // Update the setting when the text field changes
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            setCurrentValue(newVal);
        });
        
        return textField;
    }
    
    @Override
    public void updateFromControl(Control control) {
        if (control instanceof TextField) {
            TextField textField = (TextField) control;
            setCurrentValue(textField.getText());
        }
    }
    
    @Override
    public boolean isValid() {
        String value = getCurrentValue();
        
        // Check if required
        if (isRequired() && (value == null || value.trim().isEmpty())) {
            return false;
        }
        
        // Check max length
        if (maxLength > 0 && value != null && value.length() > maxLength) {
            return false;
        }
        
        // Check pattern
        if (pattern != null && value != null && !value.matches(pattern)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String getValidationError() {
        String value = getCurrentValue();
        
        // Check if required
        if (isRequired() && (value == null || value.trim().isEmpty())) {
            return getDisplayName() + " is required";
        }
        
        // Check max length
        if (maxLength > 0 && value != null && value.length() > maxLength) {
            return getDisplayName() + " must be " + maxLength + " characters or less";
        }
        
        // Check pattern
        if (pattern != null && value != null && !value.matches(pattern)) {
            return getDisplayName() + " format is invalid";
        }
        
        return null;
    }
} 