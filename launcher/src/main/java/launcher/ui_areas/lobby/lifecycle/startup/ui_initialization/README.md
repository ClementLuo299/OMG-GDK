# UI Initialization

This directory contains the entry points for initializing the lobby UI components and their internal dependencies. It orchestrates scene loading, ViewModel creation, stage configuration, and controller-ViewModel wiring.

## Package Overview

### 1. InitializeLobbyUIForStandardLaunch

**Entry Point:** `InitializeLobbyUIForStandardLaunch.java`

**Responsibility:** Orchestrates the complete UI initialization process for standard launch mode, coordinating scene loading, ViewModel creation, stage configuration, and controller-ViewModel wiring.

---

### 2. InitializeLobbyUIForAutoLaunch

**Entry Point:** `InitializeLobbyUIForAutoLaunch.java`

**Responsibility:** Handles auto-launch functionality, including checking if auto-launch is enabled, loading saved configuration, creating components, and launching games automatically.

---

### 3. scene_and_controller

**Entry Point:** `LoadLobbySceneAndGetController.java`

**Responsibility:** Loads the main user interface scene from FXML resources, handles scene creation, CSS styling, and controller extraction.

---

### 4. viewmodel

**Entry Point:** `ViewModelInitializer.java`, `WireViewModelToController.java`

**Responsibility:** Creates and configures the ViewModel for the application and wires it to the controller for bidirectional communication.

---

### 5. main_stage

**Entry Point:** `MainStageInitializer.java`

**Responsibility:** Initializes the primary application stage with basic properties, event handlers, and configuration settings.

---

### 6. ui_optimizers

**Entry Point:** `StageOptimizer.java`, `SceneOptimizer.java`

**Responsibility:** Applies performance optimizations to the JavaFX stage and scene for improved rendering and responsiveness.

---
