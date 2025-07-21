# Enhanced GDK Architecture

## Overview
The GDK has been significantly enhanced with a modular architecture that separates concerns and provides flexible game configuration capabilities. This document outlines the new architecture and its benefits.

## Key Architectural Changes

### **1. Shared Library (`gdk-shared`)**
- **Purpose**: Contains common enums, interfaces, and utilities shared across all modules
- **Benefits**: 
  - Eliminates code duplication
  - Ensures consistency across modules
  - Makes the system more maintainable
  - Allows independent versioning of shared components

### **2. Modular Enum System**
- **GameDifficulty**: Moved from `gdk-core` to `gdk-shared`
- **GameMode**: Moved from `gdk-core` to `gdk-shared`
- **Benefits**:
  - Games can use the same enums without depending on core
  - Easier to extend and modify enums
  - Better separation of concerns

### **3. Custom Settings System**
- **Purpose**: Allows games to define their own configurable options
- **Components**:
  - `GameSetting<T>`: Base class for all settings
  - `BooleanSetting`: For true/false options
  - `IntegerSetting`: For numeric options with ranges
  - `StringSetting`: For text options with validation
  - `GameSettings`: Interface for games to implement

## Architecture Diagram

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   gdk-shared    │    │    gdk-core     │    │   game modules  │
│                 │    │                 │    │                 │
│ • GameMode      │◄───┤ • GDKApplication│◄───┤ • ExampleGame   │
│ • GameDifficulty│    │ • GameModule    │    │ • TicTacToe     │
│ • GameSetting   │    │ • GameOptions   │    │ • Custom Games  │
│ • GameSettings  │    │ • GameState     │    │                 │
│ • Settings UI   │    │ • UI Controllers│    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Module Dependencies

### **gdk-shared**
- **Dependencies**: JavaFX Controls, SLF4J
- **Exports**: Enums, Settings interfaces, UI components

### **gdk-core**
- **Dependencies**: gdk-shared, JavaFX, JSON libraries
- **Exports**: Core GDK functionality, UI controllers

### **Game Modules**
- **Dependencies**: gdk-shared, gdk-core (optional)
- **Exports**: Game implementations

## Custom Settings System

### **Setting Types**

#### **BooleanSetting**
```java
new BooleanSetting(
    "sound_enabled",
    "Sound Effects", 
    "Enable or disable sound effects",
    true
);
```

#### **IntegerSetting**
```java
new IntegerSetting(
    "max_turns",
    "Maximum Turns",
    "Maximum number of turns (1-100)",
    50,
    1,
    100,
    true  // required
);
```

#### **StringSetting**
```java
new StringSetting(
    "player_name",
    "Player Name",
    "Enter your name (3-20 characters)",
    "Player",
    20,
    "[A-Za-z0-9_]{3,20}"  // regex validation
);
```

### **Game Settings Implementation**
```java
public class ExampleGameSettings implements GameSettings {
    private final List<GameSetting<?>> settings;
    
    public ExampleGameSettings() {
        this.settings = Arrays.asList(
            new BooleanSetting("sound_enabled", "Sound Effects", "...", true),
            new IntegerSetting("max_turns", "Maximum Turns", "...", 50, 1, 100, true),
            new StringSetting("player_name", "Player Name", "...", "Player", 20, "[A-Za-z0-9_]{3,20}")
        );
    }
    
    @Override
    public List<GameSetting<?>> getCustomSettings() {
        return settings;
    }
}
```

### **GDK Integration**
```java
@Override
public GameSettings getCustomSettings() {
    return new ExampleGameSettings();
}
```

## UI Integration

### **Settings Dialog**
- **Automatic Generation**: Creates UI controls based on setting types
- **Validation**: Real-time validation with error messages
- **Reset Functionality**: Reset to default values
- **Modal Dialog**: Prevents interaction with main UI during configuration

### **Settings Button**
- **Conditional Display**: Only shows for games with custom settings
- **Integration**: Seamlessly integrated into the game lobby

## Benefits of the New Architecture

### **For Game Developers**
1. **Flexibility**: Define exactly what settings your game needs
2. **Type Safety**: Strongly typed settings with validation
3. **UI Automation**: No need to create custom UI for settings
4. **Consistency**: All games use the same settings framework
5. **Extensibility**: Easy to add new setting types

### **For Players**
1. **Consistent Experience**: All games have similar settings dialogs
2. **Validation**: Prevents invalid configurations
3. **User-Friendly**: Clear labels, descriptions, and error messages
4. **Accessibility**: Standard UI components work with assistive technologies

### **For the GDK**
1. **Modularity**: Clear separation of concerns
2. **Maintainability**: Easier to update and extend
3. **Scalability**: New games can easily add custom settings
4. **Consistency**: Standardized approach across all games

## Migration Guide

### **For Existing Games**
1. **No Changes Required**: Existing games continue to work
2. **Optional Enhancement**: Add custom settings when needed
3. **Gradual Migration**: Can be enhanced over time

### **For New Games**
1. **Implement GameSettings**: Define your game's configurable options
2. **Override getCustomSettings()**: Return your settings implementation
3. **Test Validation**: Ensure all settings validate correctly

## Example Usage

### **1. Define Settings**
```java
public class MyGameSettings implements GameSettings {
    private final List<GameSetting<?>> settings = Arrays.asList(
        new BooleanSetting("fullscreen", "Fullscreen Mode", "Run in fullscreen", false),
        new IntegerSetting("volume", "Volume", "Sound volume (0-100)", 75, 0, 100),
        new StringSetting("username", "Username", "Your display name", "Player", 15)
    );
    
    @Override
    public List<GameSetting<?>> getCustomSettings() {
        return settings;
    }
}
```

### **2. Integrate with Game**
```java
@Override
public GameSettings getCustomSettings() {
    return new MyGameSettings();
}
```

### **3. Use Settings in Game**
```java
GameSettings settings = getCustomSettings();
boolean fullscreen = settings.getSettingValue("fullscreen", false);
int volume = settings.getSettingValue("volume", 75);
String username = settings.getSettingValue("username", "Player");
```

## Future Enhancements

### **Planned Features**
1. **Setting Categories**: Group related settings together
2. **Advanced Validation**: Custom validation rules
3. **Setting Persistence**: Save/load settings between sessions
4. **Setting Dependencies**: Conditional settings based on other values
5. **Custom UI Components**: Game-specific setting controls

### **Extension Points**
1. **New Setting Types**: Easy to add new setting types
2. **Custom Validators**: Game-specific validation logic
3. **Setting UI Customization**: Custom rendering for special settings
4. **Setting Import/Export**: Share settings between players

## Conclusion

The enhanced GDK architecture provides a robust, flexible, and maintainable foundation for game development. The modular design, custom settings system, and improved separation of concerns make it easier to develop, maintain, and extend games while providing a better user experience.

The system is designed to be backward compatible, allowing existing games to continue working while providing a clear migration path for adding new features like custom settings. 