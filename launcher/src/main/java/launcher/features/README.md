# Features

This directory contains packages for core application features, including module handling, game launching, messaging, persistence, transcript recording, file operations, JSON processing, and development utilities.

## Package Overview

### 1. development

**Entry Point:** `ProgramDelay.java`

**Responsibility:** Provides development utilities for debugging, including controlled delays during application startup.

---

### 2. file_handling

**Entry Point:** See `file_handling/README.md`

**Responsibility:** Handles file and directory operations, including path resolution, directory validation, and file path generation.

---

### 3. game_launching

**Entry Point:** `LaunchGame.java`

**Responsibility:** Coordinates game launch operations, including validation of prerequisites, parsing and validating start messages, and initiating game execution.

---

### 4. game_messaging

**Entry Point:** `SendMessageToGame.java`

**Responsibility:** Handles sending messages to game modules during gameplay and recording message exchanges for transcript purposes.

---

### 5. json_processing

**Entry Point:** `JsonParser.java`, `JsonFormatter.java`, `MessageFunctionCheck.java`

**Responsibility:** Provides JSON parsing, formatting, and message function validation utilities for processing game messages and configuration data.

---

### 6. module_handling

**Entry Point:** See `module_handling/README.md`

**Responsibility:** Handles game modules throughout their lifecycle, from discovery and compilation to loading and validation.

---

### 7. persistence

**Entry Point:** See `persistence/README.md`

**Responsibility:** Handles persistence operations for application state and user preferences, including loading, saving, and clearing persisted data.

---

### 8. transcript_recording

**Entry Point:** See `transcript_recording/README.md`

**Responsibility:** Handles transcript recording throughout game sessions, from session management to message recording and transcript saving.

---

