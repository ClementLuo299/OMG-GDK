# Developer Guide: Creating Game Modules for OMG GDK

This guide explains how to create new game modules that can be run with the OMG Game Development Kit.

## 🎯 Quick Start

1. **Create a new directory** in `modules/` with your game name
2. **Follow the standard structure** (see below)
3. **Implement the GameModule interface**
4. **Test with the GDK**

## 📁 Module Structure

```
modules/your-game/
├── src/main/java/com/games/modules/your-game/
│   ├── Main.java                    # Entry point
│   ├── YourGameModule.java          # GameModule implementation
│   ├── YourGameController.java      # Game controller
│   └── YourGame.java                # Game logic
├── src/main/resources/games/your-game/
│   ├── fxml/
│   │   └── your-game.fxml          # UI layout
│   ├── css/
│   │   └── your-game.css           # Styling
│   └── icons/
│       └── your-game-icon.png      # Game icon
└── README.md                       # Game documentation
```

## 🔧 Implementation Steps

### Step 1: Create the Main Entry Point

```java
// Main.java
package com.games.modules.yourgame;

import com.game.GameModule;

public class Main {
    public static GameModule getGameModule() {
        return new YourGameModule();
    }
}
```

### Step 2: Implement GameModule Interface

```java
// YourGameModule.java
package com.games.modules.yourgame;

import com.game.GameModule;
import com.game.enums.GameDifficulty;
import com.game.enums.GameMode;
import com.game.GameOptions;
import com.game.GameState;
import com.utils.error_handling.Logging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class YourGameModule implements GameModule {
    
    private static final String GAME_ID = "your-game";
    private static final String GAME_NAME = "Your Game";
    private static final String GAME_DESCRIPTION = "A description of your game";
    
    @Override
    public String getGameId() {
        return GAME_ID;
    }
    
    @Override
    public String getGameName() {
        return GAME_NAME;
    }
    
    @Override
    public String getGameDescription() {
        return GAME_DESCRIPTION;
    }
    
    @Override
    public int getMinPlayers() {
        return 1;
    }
    
    @Override
    public int getMaxPlayers() {
        return 4;
    }
    
    @Override
    public int getEstimatedDuration() {
        return 15; // minutes
    }
    
    @Override
    public GameDifficulty getDifficulty() {
        return GameDifficulty.MEDIUM;
    }
    
    @Override
    public String getGameCategory() {
        return "Puzzle"; // or "Strategy", "Card", "Classic", etc.
    }
    
    @Override
    public boolean supportsOnlineMultiplayer() {
        return true; // or false
    }
    
    @Override
    public boolean supportsLocalMultiplayer() {
        return true;
    }
    
    @Override
    public boolean supportsSinglePlayer() {
        return true;
    }
    
    @Override
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions) {
        Logging.info("🎮 Launching " + getGameName() + " with mode: " + gameMode.getDisplayName());
        
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(getGameFxmlPath()));
            Scene scene = new Scene(loader.load());
            
            // Apply CSS
            String cssPath = getGameCssPath();
            if (cssPath != null && !cssPath.isEmpty()) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }
            
            // Initialize controller
            YourGameController controller = loader.getController();
            if (controller != null) {
                controller.initializeGame(gameMode, playerCount, gameOptions);
            }
            
            Logging.info("✅ " + getGameName() + " launched successfully");
            return scene;
            
        } catch (Exception e) {
            Logging.error("❌ Failed to launch " + getGameName() + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public String getGameIconPath() {
        return "/games/your-game/icons/your-game-icon.png";
    }
    
    @Override
    public String getGameFxmlPath() {
        return "/games/your-game/fxml/your-game.fxml";
    }
    
    @Override
    public String getGameCssPath() {
        return "/games/your-game/css/your-game.css";
    }
    
    @Override
    public void onGameClose() {
        Logging.info("🔄 " + getGameName() + " closing - cleaning up resources");
    }
    
    @Override
    public GameState getGameState() {
        GameOptions options = new GameOptions();
        options.setOption("exampleOption", "exampleValue");
        
        GameState gameState = new GameState(GAME_ID, GAME_NAME, GameMode.LOCAL_MULTIPLAYER, 2, options);
        gameState.setStateValue("exampleData", "This is example game data");
        
        return gameState;
    }
    
    @Override
    public void loadGameState(GameState gameState) {
        Logging.info("📂 Loading " + getGameName() + " state");
        
        if (gameState != null) {
            String exampleData = gameState.getStringStateValue("exampleData", "No data");
            Logging.info("📊 Loaded game state: " + exampleData);
        }
    }
}
```

### Step 3: Create the Game Controller

```java
// YourGameController.java
package com.games.modules.yourgame;

import com.game.enums.GameMode;
import com.game.GameOptions;
import com.utils.error_handling.Logging;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class YourGameController {
    
    @FXML private VBox gameContainer;
    @FXML private Label statusLabel;
    
    private GameMode gameMode;
    private int playerCount;
    private GameOptions gameOptions;
    private YourGame gameLogic;
    
    @FXML
    public void initialize() {
        Logging.info("🎯 Initializing YourGame Controller");
    }
    
    public void initializeGame(GameMode gameMode, int playerCount, GameOptions gameOptions) {
        this.gameMode = gameMode;
        this.playerCount = playerCount;
        this.gameOptions = gameOptions;
        
        Logging.info("🎮 Initializing game with " + playerCount + " players in " + gameMode.getDisplayName() + " mode");
        
        // Initialize game logic
        gameLogic = new YourGame(gameMode, playerCount, gameOptions);
        
        // Update UI
        updateUI();
        
        // Start the game
        startGame();
    }
    
    private void updateUI() {
        statusLabel.setText("Game Mode: " + gameMode.getDisplayName() + " | Players: " + playerCount);
        
        // Add your game-specific UI elements here
        // For example, create game board, buttons, etc.
    }
    
    private void startGame() {
        Logging.info("🚀 Starting YourGame");
        
        // Initialize your game logic here
        // This is where you'd set up the game board, players, etc.
    }
    
    // Add your game-specific methods here
    // For example: handlePlayerMove(), checkWinCondition(), etc.
}
```

### Step 4: Create the FXML Layout

```xml
<!-- your-game.fxml -->
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<VBox fx:id="gameContainer" spacing="10" alignment="CENTER" 
      xmlns="http://javafx.com/javafx/11.0.1" xmlns:fx="http://javafx.com/fxml/1" 
      fx:controller="com.games.modules.yourgame.YourGameController"
      styleClass="your-game">
    
    <padding>
        <Insets top="20" right="20" bottom="20" left="20"/>
    </padding>
    
    <!-- Game Title -->
    <Label text="Your Game" styleClass="game-title"/>
    
    <!-- Status Label -->
    <Label fx:id="statusLabel" text="Game Status" styleClass="status-label"/>
    
    <!-- Game Area -->
    <VBox styleClass="game-area" VBox.vgrow="ALWAYS">
        <!-- Add your game-specific UI elements here -->
        <Label text="Your game content goes here" styleClass="placeholder"/>
    </VBox>
    
    <!-- Control Buttons -->
    <HBox spacing="10" alignment="CENTER">
        <Button text="New Game" onAction="#handleNewGame" styleClass="game-button"/>
        <Button text="Back to GDK" onAction="#handleBackToGDK" styleClass="game-button"/>
    </HBox>
    
</VBox>
```

### Step 5: Create CSS Styling

```css
/* your-game.css */
.your-game {
    -fx-background-color: #2b2b2b;
    -fx-text-fill: white;
}

.game-title {
    -fx-font-size: 24px;
    -fx-font-weight: bold;
    -fx-text-fill: #4CAF50;
}

.status-label {
    -fx-font-size: 16px;
    -fx-text-fill: #cccccc;
}

.game-area {
    -fx-background-color: #3c3c3c;
    -fx-background-radius: 8px;
    -fx-padding: 20px;
    -fx-border-color: #555555;
    -fx-border-radius: 8px;
    -fx-border-width: 1px;
}

.placeholder {
    -fx-font-size: 18px;
    -fx-text-fill: #888888;
    -fx-font-style: italic;
}

.game-button {
    -fx-background-color: #4CAF50;
    -fx-text-fill: white;
    -fx-font-size: 14px;
    -fx-padding: 8px 16px;
    -fx-background-radius: 4px;
    -fx-cursor: hand;
}

.game-button:hover {
    -fx-background-color: #45a049;
}
```

### Step 6: Create Game Logic Class

```java
// YourGame.java
package com.games.modules.yourgame;

import com.game.enums.GameMode;
import com.game.GameOptions;
import com.utils.error_handling.Logging;

public class YourGame {
    
    private GameMode gameMode;
    private int playerCount;
    private GameOptions gameOptions;
    private boolean gameRunning;
    
    public YourGame(GameMode gameMode, int playerCount, GameOptions gameOptions) {
        this.gameMode = gameMode;
        this.playerCount = playerCount;
        this.gameOptions = gameOptions;
        this.gameRunning = false;
        
        Logging.info("🎮 YourGame created with " + playerCount + " players");
    }
    
    public void startGame() {
        gameRunning = true;
        Logging.info("🚀 YourGame started");
        
        // Initialize your game logic here
        // Set up players, game board, etc.
    }
    
    public void stopGame() {
        gameRunning = false;
        Logging.info("🛑 YourGame stopped");
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
    
    // Add your game-specific methods here
    // For example: makeMove(), checkWinCondition(), etc.
}
```

## 🧪 Testing Your Module

1. **Run the GDK**:
   ```bash
   ./run-gdk.sh  # Linux/Mac
   # or
   run-gdk.bat   # Windows
   ```

2. **Check module discovery**:
   - Your game should appear in the dropdown list
   - Check the log area for any errors

3. **Test different modes**:
   - Try Single Player mode
   - Try Local Multiplayer mode
   - Try Online Multiplayer mode (if supported)

4. **Test player counts**:
   - Try minimum and maximum player counts
   - Verify validation works correctly

## 🔍 Debugging Tips

### Common Issues

1. **Module not discovered**:
   - Check that `Main.java` exists and has the correct package
   - Verify `getGameModule()` method returns a `GameModule` instance
   - Check the log area for error messages

2. **Game fails to launch**:
   - Verify FXML file path is correct
   - Check that controller class is specified in FXML
   - Ensure all required resources exist

3. **UI not displaying correctly**:
   - Check CSS file path and syntax
   - Verify FXML structure is valid
   - Test with different window sizes

### Logging

Use the `Logging` utility for debugging:

```java
import com.utils.error_handling.Logging;

// Info messages
Logging.info("✅ Game started successfully");

// Warning messages
Logging.warning("⚠️ Player count is at maximum");

// Error messages
Logging.error("❌ Failed to load game: " + e.getMessage(), e);
```

## 📋 Best Practices

1. **Follow the naming convention**: Use lowercase with hyphens for game IDs
2. **Provide meaningful descriptions**: Help users understand what your game is
3. **Handle errors gracefully**: Always catch exceptions and provide user feedback
4. **Use consistent styling**: Follow the dark theme pattern established in the GDK
5. **Test thoroughly**: Test all game modes and player counts
6. **Document your game**: Include a README.md in your module directory

## 🎯 Example: Simple Counter Game

Here's a complete example of a simple counter game:

### Main.java
```java
package com.games.modules.counter;

import com.game.GameModule;

public class Main {
    public static GameModule getGameModule() {
        return new CounterGameModule();
    }
}
```

### CounterGameModule.java
```java
package com.games.modules.counter;

import com.game.GameModule;
import com.game.enums.GameDifficulty;
import com.game.enums.GameMode;
import com.game.GameOptions;
import com.game.GameState;
import com.utils.error_handling.Logging;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CounterGameModule implements GameModule {
    
    @Override
    public String getGameId() { return "counter"; }
    
    @Override
    public String getGameName() { return "Counter Game"; }
    
    @Override
    public String getGameDescription() { return "A simple counting game"; }
    
    @Override
    public int getMinPlayers() { return 1; }
    
    @Override
    public int getMaxPlayers() { return 4; }
    
    @Override
    public int getEstimatedDuration() { return 5; }
    
    @Override
    public GameDifficulty getDifficulty() { return GameDifficulty.EASY; }
    
    @Override
    public String getGameCategory() { return "Puzzle"; }
    
    @Override
    public boolean supportsOnlineMultiplayer() { return false; }
    
    @Override
    public boolean supportsLocalMultiplayer() { return true; }
    
    @Override
    public boolean supportsSinglePlayer() { return true; }
    
    @Override
    public Scene launchGame(Stage primaryStage, GameMode gameMode, int playerCount, GameOptions gameOptions) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/games/counter/fxml/counter.fxml"));
            Scene scene = new Scene(loader.load());
            
            CounterController controller = loader.getController();
            controller.initializeGame(gameMode, playerCount, gameOptions);
            
            return scene;
        } catch (Exception e) {
            Logging.error("Failed to launch Counter Game: " + e.getMessage(), e);
            return null;
        }
    }
    
    // Implement other required methods...
}
```

This example shows the minimal implementation needed to create a working game module.

## 🚀 Next Steps

Once you've created your game module:

1. **Test it thoroughly** with the GDK
2. **Add more features** to your game
3. **Consider multiplayer support** if appropriate
4. **Share your module** with other developers
5. **Contribute to the GDK** by improving the framework

Happy game development! 🎮 