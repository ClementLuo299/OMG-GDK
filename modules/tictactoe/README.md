# TicTacToe Game Module (Standalone)

This directory contains all code and resources for the TicTacToe game, fully separated from the main app.

## Structure

```
modules/tictactoe/
├── src/
│   ├── main/
│   │   ├── java/com/games/modules/tictactoe/
│   │   │   ├── TicTacToeController.java
│   │   │   ├── TicTacToeGame.java
│   │   │   ├── TicTacToeModule.java
│   │   │   └── TicTacToePlayer.java
│   │   └── resources/games/tictactoe/
│   │       ├── css/tictactoe.css
│   │       ├── fxml/tictactoe.fxml
│   │       └── icons/tic_tac_toe_icon.png
└── README.md
```

## Integration
- The main app can reference this module by adding it to the `LocalGameSource`.
- All resources are self-contained for easy reuse or packaging as a plugin.

## How to Build/Run
- Ensure the main app's build system includes this directory in its source/resource path, or package as a JAR for plugin loading.

---

This structure makes it easy to maintain, test, or distribute TicTacToe independently from the main platform. 