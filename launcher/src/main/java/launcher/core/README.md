# Core

This directory contains the core application entry point and lifecycle management for the GDK launcher application.

## Package Overview

### 1. GDKApplication

**Entry Point:** `GDKApplication.java`

**Responsibility:** Main JavaFX application class that serves as the entry point for the GDK launcher. Initializes the JavaFX application and delegates startup and shutdown operations to the lifecycle packages.

---

### 2. lifecycle/start

**Entry Point:** `Startup.java`

**Responsibility:** Orchestrates the application startup process, including displaying the startup window and determining the appropriate launch mode (auto-launch or standard launch).

---

### 3. lifecycle/start/auto_launch

**Entry Point:** `AutoLaunchProcess.java`

**Responsibility:** Handles the auto-launch process when a previously selected game should be launched automatically on startup.

---

### 4. lifecycle/start/launch

**Entry Point:** `StandardLaunchProcess.java`

**Responsibility:** Handles the standard launch process, including module loading and UI initialization.

---

### 5. lifecycle/stop

**Entry Point:** `Shutdown.java`

**Responsibility:** Orchestrates the application shutdown process, coordinating cleanup task execution, executor service shutdown, and application exit.

---

### 6. lifecycle/stop/helpers

**Entry Point:** `CleanupTaskExecutor.java`, `ExecutorServiceShutdown.java`, `ShutdownTaskRegistry.java`

**Responsibility:** Provides helper utilities for shutdown operations, including task execution, executor management, and resource registry.

---

