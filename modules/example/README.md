# Example Game Module

A template game module to demonstrate the dynamic game discovery system.

## Game Information

- **Game ID**: `example-game`
- **Category**: Puzzle
- **Difficulty**: Medium
- **Duration**: 15 minutes
- **Players**: 1-4
- **Game Modes**: Single Player, Local Multiplayer, Online Multiplayer

## Features

- Template implementation
- Demonstrates all game modes
- Configurable game options
- State persistence example
- Logging and error handling

## Implementation Status

- ✅ Game module structure
- ✅ Basic UI placeholder
- ✅ Template implementation
- ✅ State management example
- ✅ Logging integration

## File Structure

```
modules/example/
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── games/
│   │   │           └── modules/
│   │   │               └── example/
│   │   │                   └── ExampleGameModule.java
│   │   └── resources/
│   │       └── games/
│   │           └── example/
│   │               ├── fxml/
│   │               │   └── example.fxml
│   │               ├── css/
│   │               │   └── example.css
│   │               └── icons/
```

## Development Notes

This module serves as a template for creating new games. It demonstrates:
- How to implement the GameModule interface
- Proper package structure
- Resource organization
- State management
- Logging and error handling

Use this as a starting point when creating new game modules. 