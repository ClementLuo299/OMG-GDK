# Application Flow

This document describes the complete application flow of the OMG Game Development Kit (GDK), from startup to shutdown.

## Table of Contents

1. [Application Entry Point](#application-entry-point)
2. [Startup Process](#startup-process)
3. [Module Loading](#module-loading)
4. [Lobby Interface](#lobby-interface)
5. [Game Launch Process](#game-launch-process)
6. [Game Execution](#game-execution)
7. [Messaging System](#messaging-system)
8. [Shutdown Process](#shutdown-process)

---

## Application Entry Point

### Main Method (`GDKApplication.main`)

The application starts at `GDKApplication.main(String[] args)`:

1. **Shutdown Hook Registration**
   - Registers a JVM shutdown hook to ensure cleanup on unexpected termination
   - Calls `Shutdown.forceShutdown()` if the application is terminated unexpectedly

2. **JavaFX Launch**
   - Calls `Application.launch(args)` to start the JavaFX application framework
   - JavaFX framework calls `GDKApplication.start(Stage primaryStage)`

### JavaFX Start Method (`GDKApplication.start`)

When JavaFX initializes:

1. **Delegates to StartupProcess**
   - Calls `StartupProcess.start(primaryStage)` to begin the startup sequence
   - The primary stage is passed through the startup chain

2. **Shutdown Handler**
   - The `stop()` method is registered to be called when the application closes
   - Delegates to `Shutdown.shutdown()` for cleanup

---

## Startup Process

### StartupProcess.start()

The startup process orchestrates the entire application initialization:

#### Step 1: Resource Initialization
- **ResourceInitializer.initialize()**
  - Initializes application-wide resources
  - Sets up any required static configurations

#### Step 2: Startup Window Display
- **StartupWindowManager.createAndShow()**
  - Creates and displays a Swing-based startup progress window
  - Shows progress updates during initialization
  - Uses an estimated step count initially, then calculates actual steps

#### Step 3: Launch Mode Determination
The process checks for auto-launch mode:

**Auto-Launch Mode** (if enabled):
- Checks if auto-launch is enabled via `AutoLaunchUtil.isAutoLaunchEnabled()`
- If enabled, attempts `AutoLaunch.launch()`:
  - Loads saved auto-launch data (selected game, JSON configuration)
  - Finds and loads the selected game module
  - Creates ViewModel and controller components
  - Configures the primary stage for the game
  - Sets up return-to-lobby callback
  - Launches the game directly with saved configuration
  - If successful, startup completes here

**Standard Launch Mode** (default or fallback):
- If auto-launch is disabled or fails, proceeds with `StandardLaunch.launch()`

### StandardLaunch.launch()

The standard launch process initializes the full GDK interface:

#### Step 1: Progress Update
- Updates startup window: "Starting GDK application"
- Adds development delay (if enabled)

#### Step 2: UI Initialization
- **UIInitializer.initialize()**
  - Loads main lobby scene from FXML (`GDKGameLobby.fxml`)
  - Creates `GDKGameLobbyController` instance
  - Creates and configures `GDKViewModel` (business logic layer)
  - Initializes primary stage with lobby scene
  - Applies performance optimizations
  - Wires controller with ViewModel
  - Updates startup window: "Loading user interface"

#### Step 3: Module Loading (Background)
- **LoadModules.load()**
  - Starts background thread for module discovery and loading
  - Updates startup window: "Starting module loading"
  - Creates `ModuleLoadingThread` to handle heavy work
  - Registers cleanup tasks for shutdown
  - Thread continues in background while UI remains responsive

---

## Module Loading

### ModuleLoadingThread

A background thread that loads game modules without blocking the UI:

#### Step 1: Module Discovery
- Scans the `modules/` directory for game modules
- Each module must have:
  - A `Main.java` class with `getGameModule()` method
  - Implementation of `GameModule` interface
  - Proper module structure

#### Step 2: Module Loading
- For each discovered module:
  - Loads the module class
  - Instantiates the `GameModule` via `Main.getGameModule()`
  - Validates module metadata (game name, description, player limits)
  - Updates progress in startup window

#### Step 3: UI Update
- Updates the lobby controller with loaded modules
- Populates the game selection ComboBox
- Updates startup window progress

#### Step 4: Window Transition
- Hides the startup progress window
- Shows the main application window (lobby interface)
- Completes startup process

---

## Lobby Interface

### GDKGameLobbyController

The main controller for the lobby interface, managing:

#### Initialization (LobbyInitializationManager)
1. **ViewModel Setup**
   - Receives `GDKViewModel` reference
   - Sets up bidirectional communication

2. **Manager Initialization**
   - `MessageManager`: Handles message display queue
   - `LoadingAnimationManager`: Manages loading indicators
   - `ModuleCompilationChecker`: Validates module compilation status
   - `GameLaunchingManager`: Coordinates game launches
   - `GameModuleRefreshManager`: Handles module refresh
   - `ControllerModeManager`: Manages controller state
   - `LobbyShutdownManager`: Handles cleanup

3. **UI Component Wiring**
   - Game selection ComboBox
   - Launch game button
   - JSON input/output editors
   - Message display area
   - Settings button
   - Refresh button

4. **Event Handlers**
   - Game selection change
   - Launch button click
   - JSON persistence toggle
   - Settings navigation
   - Module refresh

#### Key Subcontrollers

**GameSelectionController**
- Manages game selection ComboBox
- Handles selection changes
- Updates UI based on selected game

**JsonActionButtonsController**
- Manages JSON input/output editors
- Handles JSON validation
- Manages JSON persistence
- Processes metadata requests
- Handles message sending

**TopBarController**
- Manages top bar UI elements
- Handles settings navigation
- Manages exit functionality

---

## Game Launch Process

### User Interaction Flow

1. **Game Selection**
   - User selects a game from the ComboBox
   - `GameSelectionController` handles selection change
   - UI updates to show selected game information

2. **JSON Configuration** (Optional)
   - User can enter JSON configuration in the input editor
   - JSON can be validated before launch
   - JSON persistence can be enabled to save configuration

3. **Launch Button Click**
   - User clicks "Launch Game" button
   - `GameLaunchingManager.launchGame()` is called

### GameLaunchingManager.launchGame()

The launch manager coordinates the launch process:

#### Step 1: Validation
- Validates ViewModel availability
- Validates game launch prerequisites via `GameLaunchService`
- Checks if a game is already running
- Validates JSON configuration (if provided)

#### Step 2: Configuration Application
- Applies JSON configuration to the game module
- Sends configuration via `GameModule.handleMessage()`
- Updates UI with configuration status

#### Step 3: Launch Execution
- Calls `GameLaunchService.executeLaunch()`
- Delegates to `GDKViewModel.launchGame()`

### GDKViewModel.launchGame()

The ViewModel handles the business logic of launching:

#### Step 1: State Reset
- Resets game state flags
- Cleans up any previous game instances
- Determines game mode from JSON (single player, local multiplayer, online)

#### Step 2: Messaging Bridge Setup
- Sets up `MessagingBridge` consumer to receive game messages
- Sets up transcript recording subscription
- Configures message routing to server simulator

#### Step 3: Scene Creation
- Calls `GameModule.launchGame(primaryStage)`
- Game module creates and returns its JavaFX Scene
- Scene contains the game's UI

#### Step 4: Stage Configuration
- Sets the primary stage's scene to the game scene
- Updates window title to game name
- Configures window close handler
- Sets up keyboard shortcuts (Escape key to return to lobby)

#### Step 5: Server Simulator (if needed)
- For online multiplayer games:
  - Creates server simulator window
  - Configures message routing
  - Displays server simulator UI
- For single player/local multiplayer:
  - Skips server simulator creation

#### Step 6: State Update
- Updates ViewModel state to track running game
- Stores game module reference
- Marks game as active

---

## Game Execution

### Game Module Interface

Game modules implement the `GameModule` interface:

1. **launchGame(Stage stage)**
   - Creates and returns the game's JavaFX Scene
   - Initializes game controller
   - Sets up game logic

2. **handleMessage(Map<String, Object> message)**
   - Receives messages from the launcher
   - Processes configuration and commands
   - Updates game state accordingly

### Messaging During Game Execution

#### Game → Launcher Messages
- Games publish messages via `MessagingBridge.publish()`
- Messages are received by registered consumers in the launcher
- Messages are routed to:
  - Server simulator (for display)
  - JSON output editor (for end messages)
  - Transcript recorder (for logging)

#### Launcher → Game Messages
- Launcher sends messages via `GameModule.handleMessage()`
- Messages include:
  - Configuration data (JSON)
  - Commands (metadata requests, etc.)
  - Acknowledgments

### Return to Lobby

Games can return to the lobby in several ways:

1. **Window Close**
   - User closes the game window
   - Close handler calls `cleanupGameAndServerSimulator()`
   - Returns to lobby scene

2. **Escape Key**
   - Keyboard shortcut (Escape) triggers return
   - Prevents softlocks if game UI doesn't have return button

3. **Game-Initiated Return**
   - Game can publish `WINDOW_CLOSE_REQUEST` event
   - Launcher handles event and returns to lobby

4. **Server Simulator Close Request**
   - Game can send `close_server_simulator` message
   - Launcher closes server simulator window

---

## Messaging System

### MessagingBridge

A thread-safe publish/subscribe system for inter-module communication:

#### Architecture
- **Publishers**: Games publish messages via `MessagingBridge.publish()`
- **Consumers**: Launcher components subscribe via `MessagingBridge.addConsumer()`
- **Thread Safety**: Uses `CopyOnWriteArrayList` for consumer list

#### Message Flow

1. **Game Publishes Message**
   ```
   Game → MessagingBridge.publish(message)
   ```

2. **Bridge Distributes to Consumers**
   ```
   MessagingBridge → All registered consumers
   ```

3. **Consumers Process Messages**
   - Server Simulator: Displays messages in UI
   - Transcript Recorder: Logs messages to file
   - JSON Output Editor: Shows end messages
   - ViewModel: Handles special commands (e.g., close_server_simulator)

#### Message Types

**Configuration Messages**
- Sent from launcher to game
- Contains game configuration (mode, players, options)
- Processed by `GameModule.handleMessage()`

**Game State Messages**
- Sent from game to launcher
- Contains game events, moves, state updates
- Displayed in server simulator

**End Messages**
- Sent from game when game ends
- Special handling: displayed in JSON output editor
- Triggers transcript finalization

**Command Messages**
- Special messages for launcher commands
- Example: `close_server_simulator` to close server simulator window

### Transcript Recording

All messages are recorded to transcript files:

1. **Message Reception**
   - Messages received via MessagingBridge
   - Forwarded to `TranscriptRecorder`

2. **File Writing**
   - Messages written to timestamped transcript files
   - Files stored in `launcher/saved/transcripts/`
   - Both JSON and text formats

3. **Finalization**
   - Transcript finalized when game ends
   - Files saved with game name and timestamp

---

## Shutdown Process

### Normal Shutdown

When the application closes normally:

#### Step 1: JavaFX Stop Method
- JavaFX calls `GDKApplication.stop()`
- Delegates to `Shutdown.shutdown()`

#### Step 2: Shutdown Process (Shutdown.shutdown())
1. **Check Shutdown State**
   - Prevents duplicate shutdown calls
   - Sets `isShuttingDown` flag

2. **Get Registered Resources**
   - Retrieves all registered cleanup tasks
   - Retrieves all registered executor services
   - Clears the registry

3. **Execute Cleanup Tasks**
   - `CleanupTaskExecutor.executeCleanupTasks()`
   - Runs all registered cleanup tasks
   - Handles errors gracefully

4. **Shutdown Executor Services**
   - `ExecutorServiceShutdown.shutdownExecutors()`
   - Gracefully shuts down with 2-second timeout
   - Force shutdown if timeout exceeded

5. **Application Exit**
   - Logs shutdown completion
   - Calls `System.exit(0)`

### Force Shutdown

For emergency scenarios (shutdown hook, unexpected termination):

1. **Shutdown Hook Triggered**
   - JVM shutdown hook calls `Shutdown.forceShutdown()`

2. **Immediate Executor Shutdown**
   - Forcefully shuts down all executors immediately
   - No timeout waiting

3. **Clear Registry**
   - Clears all registered resources
   - Skips cleanup task execution

4. **Force Exit**
   - Immediately calls `System.exit(0)`

### Cleanup Tasks

Various components register cleanup tasks:

- **Module Loading Thread**: Interrupts loading thread if still active
- **Startup Window**: Hides startup progress window
- **Game State**: Cleans up running game instances
- **Server Simulator**: Closes server simulator window
- **Messaging Bridge**: Unsubscribes all consumers
- **Transcript Recorder**: Finalizes transcript files

---

## Flow Diagrams

### Startup Flow

```
main()
  └─> launch(args)
       └─> start(primaryStage)
            └─> StartupProcess.start()
                 ├─> ResourceInitializer.initialize()
                 ├─> StartupWindowManager.createAndShow()
                 └─> [Auto-Launch Check]
                      ├─> AutoLaunch.launch() [if enabled]
                      │    └─> Game Launch (direct)
                      └─> StandardLaunch.launch() [default]
                           ├─> UIInitializer.initialize()
                           │    ├─> Load FXML
                           │    ├─> Create ViewModel
                           │    └─> Wire Controller
                           └─> LoadModules.load()
                                └─> ModuleLoadingThread [background]
                                     ├─> Discover Modules
                                     ├─> Load Modules
                                     └─> Update UI
```

### Game Launch Flow

```
User Clicks Launch
  └─> GameLaunchingManager.launchGame()
       ├─> Validate Prerequisites
       ├─> Apply JSON Configuration
       └─> GameLaunchService.executeLaunch()
            └─> GDKViewModel.launchGame()
                 ├─> Reset State
                 ├─> Setup MessagingBridge
                 ├─> GameModule.launchGame()
                 │    └─> Create Game Scene
                 ├─> Set Stage Scene
                 ├─> Setup Close Handler
                 └─> [Create Server Simulator] (if needed)
```

### Messaging Flow

```
Game → MessagingBridge.publish(message)
  └─> All Consumers Receive
       ├─> Server Simulator Consumer
       │    └─> Display in UI
       ├─> Transcript Recorder Consumer
       │    └─> Write to File
       ├─> JSON Output Editor Consumer
       │    └─> Display End Messages
       └─> ViewModel Consumer
            └─> Handle Commands
```

### Shutdown Flow

```
Application Close
  └─> GDKApplication.stop()
       └─> Shutdown.shutdown()
            ├─> Get Cleanup Tasks
            ├─> Execute Cleanup Tasks
            ├─> Shutdown Executors
            └─> System.exit(0)
```

---

## Key Components Summary

### Core Classes

- **GDKApplication**: Main entry point, JavaFX application class
- **StartupProcess**: Orchestrates startup sequence
- **StandardLaunch**: Standard launch mode implementation
- **AutoLaunch**: Auto-launch mode implementation
- **LoadModules**: Coordinates module loading
- **GDKGameLobbyController**: Main lobby UI controller
- **GDKViewModel**: Business logic layer, application state
- **GameLaunchingManager**: Coordinates game launches
- **Shutdown**: Orchestrates shutdown process

### Key Interfaces

- **GameModule**: Interface that game modules must implement
- **MessagingBridge**: Publish/subscribe messaging system

### Key Utilities

- **ModuleDiscovery**: Discovers game modules in filesystem
- **TranscriptRecorder**: Records game messages to files
- **AutoLaunchUtil**: Manages auto-launch state persistence

---

## Notes

- The application uses a layered architecture:
  - **UI Layer**: FXML controllers and JavaFX components
  - **Business Layer**: ViewModel and services
  - **Data Layer**: Module loading and persistence

- Threading:
  - JavaFX UI thread for all UI operations
  - Background threads for module loading
  - Executor services for async operations

- Error Handling:
  - Errors are logged via `Logging` utility
  - UI errors are displayed to users
  - Critical errors trigger application shutdown

- State Management:
  - ViewModel maintains application state
  - Auto-launch state persisted to files
  - JSON configuration can be persisted

---

*Last Updated: December 2025*
*Version: Beta 1.0*

