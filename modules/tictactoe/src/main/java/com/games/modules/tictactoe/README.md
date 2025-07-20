# TicTacToe Game Module

This directory contains all the Java source code for the TicTacToe game module.

## File Structure

```
tictactoe/
├── TicTacToeModule.java      # Main game module class (implements GameModule)
├── TicTacToeController.java  # JavaFX controller for the game UI
├── TicTacToeGame.java        # Game logic and state management
├── TicTacToePlayer.java      # Player representation class
└── README.md                 # This documentation file
```

## Components

### TicTacToeModule.java
- Extends `BaseGameModule` to integrate with the game framework
- Defines game metadata (name, description, player count, etc.)
- Handles game launching and state management
- Registers the game with the `GameRegistry`

### TicTacToeController.java
- JavaFX controller for the game UI
- Handles user interactions and UI updates
- Manages game state, player turns, and scoring
- Implements chat functionality and move history
- Integrates with the game module framework via `initializeGame()`

### TicTacToeGame.java
- Core game logic implementation
- Manages the 3x3 game board state
- Handles move validation and placement
- Detects wins, draws, and game over conditions
- Manages player turns and game progression

### TicTacToePlayer.java
- Represents a player in the TicTacToe game
- Contains player symbol (X or O) and UserAccount reference
- Provides player information and identity

## Resources

The game's UI resources are located in:
```
src/main/resources/games/tictactoe/
├── fxml/tictactoe.fxml       # UI layout definition
├── css/tictactoe.css         # Styling and themes
└── icons/tic_tac_toe_icon.png # Game icon
```

## Features

- **Local Multiplayer**: Two players take turns on the same device
- **Move History**: Tracks and displays all moves made during the game
- **Chat System**: In-game chat functionality for players
- **Timer System**: Move timer with visual countdown
- **Win Detection**: Automatic detection of wins, draws, and winning combinations
- **Game Controls**: Restart, forfeit, and exit game functionality
- **Score Tracking**: Maintains player scores across multiple games

## Integration

This module integrates with the main application through:
- `GameRegistry` for game discovery and registration
- `GameLauncherService` for game lifecycle management
- `BaseGameModule` for common game functionality
- `ScreenManager` for navigation and UI management 