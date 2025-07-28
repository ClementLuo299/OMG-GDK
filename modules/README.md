# Game Modules

This directory contains all the game modules for the OMG Game Development Kit (GDK).

## Available Modules

### Core Modules
- **example** - Example game module demonstrating GDK integration
- **tictactoe** - Classic Tic Tac Toe game

### Classic Arcade Games
- **snake** - Classic Snake game with modern graphics
- **pong** - The original arcade tennis game
- **tetris** - The classic falling blocks puzzle game
- **pacman** - Eat dots and avoid ghosts in this classic maze game
- **breakout** - Smash bricks with a bouncing ball and paddle

## Module Structure

Each module follows the standard Maven structure:

```
module-name/
├── pom.xml                    # Maven configuration
├── src/
│   └── main/
│       └── java/
│           ├── Main.java      # Main game class (implements GameModule)
│           └── Metadata.java  # Game metadata class
└── target/                    # Compiled classes (auto-generated)
```

## Testing Module Detection

You can test the module detection system by:

1. **Adding modules** - Create new modules and refresh to see "✅ New game module '[name]' was added"
2. **Removing modules** - Comment out or delete Main.java and refresh to see "⚠️ Game module '[name]' was removed"
3. **Multiple changes** - Add and remove modules simultaneously to test both messages

## Module Requirements

For a module to be detected, it must:

1. Have a valid `pom.xml` file
2. Contain a `Main.java` file that implements `GameModule`
3. Have a `Metadata.java` file with game information
4. Compile successfully with Maven

## Development

To create a new module:

1. Create a new directory in `modules/`
2. Add `pom.xml` with GDK dependency
3. Create `Main.java` implementing `GameModule`
4. Create `Metadata.java` with game info
5. Run `mvn compile` to build
6. Refresh in the GDK launcher to see the new module 