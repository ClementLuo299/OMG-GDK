package example;

import gdk.GameSettings;
import gdk.GameSetting;
import gdk.BooleanSetting;
import gdk.IntegerSetting;
import gdk.StringSetting;

import java.util.Arrays;
import java.util.List;

/**
 * Example implementation of custom game settings.
 * Demonstrates how games can define their own configurable options.
 *
 * @authors Clement Luo
 * @date July 21, 2025
 * @since 1.0
 */
public class ExampleGameSettings implements GameSettings {
    
    private final List<GameSetting<?>> settings;
    
    public ExampleGameSettings() {
        // Create various types of settings for demonstration
        this.settings = Arrays.asList(
            // Boolean setting
            new BooleanSetting(
                "sound_enabled",
                "Sound Effects",
                "Enable or disable sound effects during gameplay",
                true
            ),
            
            // Integer setting with range
            new IntegerSetting(
                "max_turns",
                "Maximum Turns",
                "Maximum number of turns allowed in the game (1-100)",
                50,
                1,
                100,
                true
            ),
            
            // String setting with validation
            new StringSetting(
                "player_name",
                "Player Name",
                "Enter your player name (3-20 characters)",
                "Player",
                20,
                "[A-Za-z0-9_]{3,20}"
            ),
            
            // Boolean setting for music
            new BooleanSetting(
                "music_enabled",
                "Background Music",
                "Enable or disable background music",
                true
            ),
            
            // Integer setting for difficulty multiplier
            new IntegerSetting(
                "difficulty_multiplier",
                "Difficulty Multiplier",
                "Multiplier for game difficulty (1-10)",
                3,
                1,
                10
            ),
            
            // String setting for game theme
            new StringSetting(
                "game_theme",
                "Game Theme",
                "Choose the visual theme for the game",
                "Classic"
            )
        );
    }
    
    @Override
    public List<GameSetting<?>> getCustomSettings() {
        return settings;
    }
    
    @Override
    public String getSettingsDisplayName() {
        return "Example Game Settings";
    }
    
    @Override
    public String getSettingsDescription() {
        return "Configure various options for the Example Game including sound, difficulty, and appearance.";
    }
} 