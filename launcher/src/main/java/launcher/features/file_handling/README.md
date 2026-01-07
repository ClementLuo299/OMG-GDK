# File Handling

This directory contains packages for handling file and directory operations, including path resolution, directory validation, and file path generation.

## Package Overview

### 1. directory_access

**Entry Point:** `DirectoryAccessCheck.java`

**Responsibility:** Checks if a directory is accessible by performing comprehensive validation including existence, directory type, readability, and listing capability.

---

### 2. directory_existence

**Entry Point:** `DirectoryExistenceCheck.java`

**Responsibility:** Validates basic directory properties such as existence, directory type, and readability. Also includes `ParentDirectoryExistenceCheck.java` for ensuring parent directories exist for file operations.

---

### 3. file_paths

**Entry Point:** `GetOtherPaths.java`

**Responsibility:** Provides centralized file path constants for persistence, configuration, and saved data. Also includes `GetModulesDirectoryPath.java` for resolving the modules directory path and `GenerateTranscriptFilePath.java` for generating transcript file paths.

---

