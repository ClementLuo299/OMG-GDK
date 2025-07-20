# Game Modules Directory

This directory contains all the external game modules for the OMG Platform. Each module is a self-contained game that can be dynamically loaded by the platform.

## 📁 Module Structure

Each game module follows this structure:
```
modules/
├── [game-name]/
│   ├── src/main/java/com/games/modules/[game-name]/
│   │   └── Main.java
│   ├── src/main/resources/games/[game-name]/
│   │   ├── css/
│   │   ├── fxml/
│   │   └── icons/
│   └── README.md
```

## 🎮 Available Games

### Core Games
- **TicTacToe** - Classic X's and O's game
- **Example Game** - Template game for development

## 🎯 Game Categories

- **Classic**: TicTacToe
- **Template**: Example Game

## 📊 Game Statistics

- **Total Games**: 2
- **Single Player**: 2 games
- **Local Multiplayer**: 2 games
- **Online Multiplayer**: 2 games
- **Average Duration**: 15-20 minutes
- **Player Range**: 1-8 players

## 🔧 Technical Details

### Module Loading
- Games are discovered automatically from the `modules/` directory
- Each module must implement the `GameModule` interface
- Modules are loaded asynchronously during startup
- Failed modules are logged but don't prevent other games from loading

### Layout Support
- Games are displayed in a responsive grid layout
- 3 games per row by default
- Vertical scrolling for multiple rows
- Responsive design for different screen sizes

### Game Features
- Dynamic game discovery
- Multiple game modes (Single, Local, Online)
- Configurable player counts
- Estimated duration tracking
- Difficulty levels
- Category classification

## 🚀 Adding New Games

To add a new game:

1. Create a new directory in `modules/`
2. Follow the standard module structure
3. Implement the `GameModule` interface in your `Main.java` class
4. Implement required methods
5. Add resources (FXML, CSS, icons) as needed
6. Test the module discovery

## 📝 Module Requirements

Each game module must implement:
- `getGameId()` - Unique identifier
- `getGameName()` - Display name
- `getGameDescription()` - Game description
- `getGameCategory()` - Game category
- `getMinPlayers()` / `getMaxPlayers()` - Player count
- `getEstimatedDuration()` - Game duration
- `getDifficulty()` - Game difficulty
- Support flags for different game modes

## 🎨 UI Integration

- Games use JavaFX for UI
- FXML files for layout
- CSS for styling
- Icons for visual representation
- Responsive design principles

## 🔍 Discovery Process

1. Platform scans `modules/` directory
2. Identifies directories as potential modules
3. Looks for `Main.java` classes
4. Instantiates and validates modules
5. Registers valid modules with GameDiscoveryService
6. Updates UI with discovered games

## 📈 Performance

- Asynchronous discovery prevents UI blocking
- Lazy loading of game resources
- Efficient memory usage
- Fast startup times
- Scalable architecture

---

*Last updated: January 2025*
*Total modules: 2* 