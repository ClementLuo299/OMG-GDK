# Persistence

This directory contains packages for handling persistence operations, including loading, saving, and clearing persisted application state and user preferences.

## Package Overview

### 1. JsonPersistenceManager

**Entry Point:** `JsonPersistenceManager.java`

**Responsibility:** Coordinates all persistence operations across multiple persistence helpers, providing a unified API for loading, saving, and clearing persisted data.

---

### 2. load

**Entry Point:** `LoadPreviousJsonInput.java`

**Responsibility:** Handles loading persisted settings on startup, including JSON content and persistence toggle state. Includes helper classes for loading individual persistence components.

---

### 3. save

**Entry Point:** `SaveJsonContent.java`, `SavePersistenceToggleState.java`, `SavePreviouslySelectedGame.java`

**Responsibility:** Handles saving persistence settings, including JSON content (if enabled), persistence toggle state, and selected game name.

---

### 4. clear

**Entry Point:** `ClearJsonFile.java`

**Responsibility:** Handles clearing persisted JSON content files.

---

