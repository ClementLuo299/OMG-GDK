# Game Modules

This directory contains game modules that integrate with the GDK (Game Development Kit).

## Module Structure

Each game module must follow this structure:

```
module-name/
├── pom.xml
├── README.md
├── src/
│   └── main/
│       ├── java/
│       │   ├── Main.java              # REQUIRED: Main game module class
│       │   ├── Metadata.java          # REQUIRED: Metadata class
│       │   └── ...                    # Other game classes
│       └── resources/
│           └── games/
│               └── modulename/
│                   ├── css/
│                   ├── fxml/
│                   └── icons/
```

## Required Files

### 1. Main.java (REQUIRED)
- Must implement `gdk.GameModule` interface
- Must have a constructor that creates the metadata instance
- Must implement `getMetadata()` method
- **Standardized naming**: Always named `Main.java`

### 2. Metadata.java (REQUIRED)
- Must extend `gdk.GameMetadata`
- Must implement all abstract methods
- Provides all game information for the GDK
- **Standardized naming**: Always named `Metadata.java`

## Metadata Requirements

Every game module must provide a metadata class that extends `gdk.GameMetadata` and implements:

### Basic Information
- `getGameName()` - Display name shown in UI
- `getGameVersion()` - Version string
- `getGameDescription()` - Game description
- `getGameAuthor()` - Author name

### Game Modes
- `supportsSinglePlayer()` - Whether single player is supported
- `supportsMultiPlayer()` - Whether multi player is supported
- `supportsAIOpponent()` - Whether AI opponents are supported
- `supportsTournament()` - Whether tournament mode is supported

### Requirements
- `getMinPlayers()` - Minimum number of players
- `getMaxPlayers()` - Maximum number of players
- `getMinDifficulty()` - Minimum difficulty level
- `getMaxDifficulty()` - Maximum difficulty level
- `getEstimatedDurationMinutes()` - Estimated game duration
- `getRequiredResources()` - List of required resources

## Example Implementation

```java
// Metadata.java
public class Metadata extends GameMetadata {
    @Override
    public String getGameName() {
        return "My Awesome Game";
    }
    
    @Override
    public String getGameVersion() {
        return "1.0.0";
    }
    
    // ... implement all other methods
}

// Main.java
public class Main implements GameModule {
    private final Metadata metadata;
    
    public Main() {
        this.metadata = new Metadata();
    }
    
    @Override
    public Metadata getMetadata() {
        return metadata;
    }
    
    // ... implement other GameModule methods
}
```

## Benefits

- **Organized Code**: Metadata is separated from game logic
- **Reusable**: Metadata class can be used by multiple components
- **Consistent**: All games follow the same metadata structure
- **Extensible**: Easy to add new metadata fields
- **Type Safe**: Compile-time checking of metadata requirements
- **Standardized**: Consistent naming across all modules

## Available Modules

- **example**: Simple example game for testing
- **tictactoe**: Classic Tic Tac Toe game 