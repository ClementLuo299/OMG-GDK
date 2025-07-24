package gdk;

import java.util.List;

/**
 * Interface for games to define their custom settings.
 * Games implement this interface to specify what settings they support.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public interface GameSettings {
    
    /**
     * Gets the list of custom settings for this game.
     * @return List of game settings
     */
    List<GameSetting<?>> getCustomSettings();
    
    /**
     * Gets a setting by its key.
     * @param key The setting key
     * @return The setting, or null if not found
     */
    default GameSetting<?> getSetting(String key) {
        return getCustomSettings().stream()
            .filter(setting -> setting.getKey().equals(key))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Gets the value of a setting by its key.
     * @param key The setting key
     * @param defaultValue The default value if setting not found
     * @return The setting value
     */
    @SuppressWarnings("unchecked")
    default <T> T getSettingValue(String key, T defaultValue) {
        GameSetting<?> setting = getSetting(key);
        if (setting != null) {
            return (T) setting.getCurrentValue();
        }
        return defaultValue;
    }
    
    /**
     * Sets the value of a setting by its key.
     * @param key The setting key
     * @param value The new value
     */
    default <T> void setSettingValue(String key, T value) {
        GameSetting<?> setting = getSetting(key);
        if (setting != null) {
            ((GameSetting<T>) setting).setCurrentValue(value);
        }
    }
    
    /**
     * Validates all settings.
     * @return true if all settings are valid
     */
    default boolean validateSettings() {
        return getCustomSettings().stream().allMatch(GameSetting::isValid);
    }
    
    /**
     * Gets validation errors for all settings.
     * @return List of validation error messages
     */
    default List<String> getValidationErrors() {
        return getCustomSettings().stream()
            .map(GameSetting::getValidationError)
            .filter(error -> error != null)
            .toList();
    }
    
    /**
     * Resets all settings to their default values.
     */
    default void resetToDefaults() {
        getCustomSettings().forEach(GameSetting::resetToDefault);
    }
    
    /**
     * Gets the display name for the settings section.
     * @return The settings section name
     */
    default String getSettingsDisplayName() {
        return "Game Settings";
    }
    
    /**
     * Gets the description for the settings section.
     * @return The settings section description
     */
    default String getSettingsDescription() {
        return "Configure custom settings for this game";
    }
} 