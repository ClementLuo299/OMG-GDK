# Startup

This directory contains the entry points and orchestration logic for starting up the lobby UI. It coordinates UI initialization, component setup, module loading, and controller initialization.

## Package Overview

### 1. LobbyStartup

**Entry Point:** `LobbyStartup.java`

**Responsibility:** Single entry point for lobby startup operations. Handles both standard launch and auto-launch modes, orchestrating the complete startup process.

---

### 2. controller_initialization

**Entry Point:** `ControllerInitialization.java`, `ViewModelInitialization.java`

**Responsibility:** Manages initialization of the lobby controller and ViewModel. `ControllerInitialization` handles controller setup (managers, subcontrollers, callbacks, UI setup) independent of ViewModel availability. `ViewModelInitialization` handles ViewModel updates and component recreation when the ViewModel becomes available.

---

### 3. ui_initialization

**Entry Point:** `InitializeLobbyUIForStandardLaunch.java`, `InitializeLobbyUIForAutoLaunch.java`

**Responsibility:** Contains entry points for initializing the lobby UI components and their internal dependencies. Orchestrates scene loading, ViewModel creation, stage configuration, and controller-ViewModel wiring.

---

### 4. module_loading

**Entry Point:** `ModuleLoadingThread.java`

**Responsibility:** Coordinates the module loading process during startup. Sets up and starts the background thread that loads game modules and manages cleanup tasks.

---

### 5. component_setup

**Entry Point:** `CallbackWiring.java`, `JsonEditorSetup.java`

**Responsibility:** Handles setup of UI components, including wiring callbacks between subcontrollers and managers, and configuring JSON editor containers and persistence listeners.

---

