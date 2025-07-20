# OMG Game Development Kit (GDK)

A simple and lightweight Game Development Kit for testing and running game modules without complex authentication or UI systems.

## 🎯 Overview

The GDK provides a streamlined interface for developers to:
- **Discover and run game modules** from the `modules/` directory
- **Test different game modes** (Single Player, Local Multiplayer, Online Multiplayer)
- **Configure game settings** through a simple properties file
- **View real-time logs** of game module discovery and execution

## 🚀 Quick Start

### Prerequisites

1. **Java 11 or higher**
2. **JavaFX SDK** (included with Java 11+ or download separately)
3. **Maven** (for building)

### Running the GDK

1. **Clone or download** the project
2. **Navigate to the project directory**
3. **Run the GDK application**:

```bash
# Using Maven
mvn clean javafx:run

# Or compile and run manually
mvn clean compile
java -cp target/classes com.GDKApplication
```

### First Run

On first run, the GDK will:
1. Create a `gdk-config.properties` file with default settings
2. Scan the `modules/` directory for game modules
3. Display the launcher interface

## 📁 Project Structure

```
OMG-GDK/
├── src/main/java/com/
│   ├── GDKApplication.java          # Main GDK application
│   ├── game/                        # Game interfaces and enums
│   ├── utils/                       # Utility classes (ModuleLoader, etc.)
│   └── ...
├── modules/                         # Game modules directory
│   ├── example/                     # Example game module
│   ├── tictactoe/                   # TicTacToe game module
│   └── [your-game]/                 # Your custom game modules
├── gdk-config.properties           # GDK configuration (auto-generated)
└── README.md                       # This file
```

## 🎮 Using the GDK

### 1. Game Module Discovery

The GDK automatically discovers game modules in the `modules/` directory. Each module must:
- Implement the `GameModule` interface
- Have a `Main.java` class that returns a `GameModule` instance
- Follow the standard module structure

### 2. Launching Games

1. **Select a game** from the dropdown list
2. **Choose game mode**:
   - **Single Player**: Play against AI
   - **Local Multiplayer**: Play on the same machine
   - **Online Multiplayer**: Play over network (requires server)
3. **Set player count** (within the game's supported range)
4. **Click "Launch Game"**

### 3. Configuration

Click the "⚙️ Settings" button to configure:
- **Username**: Your developer name
- **Server URL**: For multiplayer testing
- **Server Port**: For multiplayer testing

Settings are saved to `gdk-config.properties`.

## 🛠️ Creating Game Modules

### Module Structure

```
modules/your-game/
├── src/main/java/com/games/modules/your-game/
│   ├── Main.java                    # Entry point (returns GameModule)
│   ├── YourGameModule.java          # GameModule implementation
│   ├── YourGameController.java      # Game controller
│   └── ...
├── src/main/resources/games/your-game/
│   ├── fxml/
│   │   └── your-game.fxml          # Game UI layout
│   ├── css/
│   │   └── your-game.css           # Game styling
│   └── icons/
│       └── your-game-icon.png      # Game icon
└── README.md                       # Game documentation
```

### Example Module Implementation

```java
// Main.java
package com.games.modules.yourgame;

import com.game.GameModule;

public class Main {
    public static GameModule getGameModule() {
        return new YourGameModule();
    }
}

// YourGameModule.java
public class YourGameModule implements GameModule {
    @Override
    public String getGameId() { return "your-game"; }
    
    @Override
    public String getGameName() { return "Your Game"; }
    
    @Override
    public String getGameDescription() { return "A description of your game"; }
    
    @Override
    public int getMinPlayers() { return 1; }
    
    @Override
    public int getMaxPlayers() { return 4; }
    
    @Override
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions options) {
        // Load your game's FXML and return the scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/games/yourgame/fxml/your-game.fxml"));
        Scene scene = new Scene(loader.load());
        
        // Initialize your game controller
        YourGameController controller = loader.getController();
        controller.initializeGame(gameMode, playerCount, options);
        
        return scene;
    }
    
    // Implement other required methods...
}
```

## 🔧 Configuration

### GDK Configuration File

The `gdk-config.properties` file contains:

```properties
# User settings
username=GDK Developer
serverUrl=localhost
serverPort=8080

# Development settings
enableLogging=true
enableDebugMode=true
```

### Game Options

When launching games, the GDK passes these options:

```java
GameOptions options = new GameOptions();
options.setOption("debugMode", "true");
options.setOption("serverUrl", "localhost");
options.setOption("serverPort", "8080");
```

## 📋 Available Game Modules

### Core Modules

- **Example Game** (`modules/example/`)
  - Template implementation
  - Demonstrates all game modes
  - Good starting point for new games

- **TicTacToe** (`modules/tictactoe/`)
  - Complete implementation
  - Classic 3x3 grid game
  - Supports all game modes

### Adding Your Own Modules

1. Create a new directory in `modules/`
2. Follow the module structure above
3. Implement the `GameModule` interface
4. The GDK will automatically discover your module

## 🐛 Troubleshooting

### Common Issues

1. **No games found**
   - Check that modules are in the `modules/` directory
   - Ensure each module has a `Main.java` class
   - Verify the `Main.java` returns a `GameModule` instance

2. **JavaFX not found**
   - Ensure Java 11+ is installed
   - Add JavaFX to your classpath if using older Java

3. **Game fails to launch**
   - Check the log area for error messages
   - Verify FXML files are in the correct location
   - Ensure all required resources are present

### Logging

The GDK provides real-time logging in the log area. Common log messages:

- `📦 Found X game modules` - Module discovery
- `✅ Game Name - Description` - Successful module loading
- `🚀 Launching Game` - Game launch attempt
- `❌ Error message` - Error occurred

## 🤝 Contributing

To contribute to the GDK:

1. **Fork the repository**
2. **Create a feature branch**
3. **Add your game module** to the `modules/` directory
4. **Test thoroughly** with different game modes
5. **Submit a pull request**

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support or questions:
- Check the troubleshooting section above
- Review the example modules for reference
- Create an issue on the project repository

---

**Happy Game Development! 🎮** 