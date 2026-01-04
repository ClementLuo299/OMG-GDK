# Module Handling

This directory contains packages for handling game modules throughout their lifecycle, from discovery to loading and validation.

## Package Overview

### 1. check_compilation_status

**Entry Point:** `CheckCompilationNeeded.java`

**Responsibility:** Determines if a module needs to be recompiled.

---

### 2. compile_modules

**Entry Point:** `CompileModule.java`

**Responsibility:** Compiles a module given its directory.

---

### 3. extract_metadata

**Entry Point:** `ModuleMetadataExtractor.java`

**Responsibility:** Extracts and processes metadata from loaded game modules.

---

### 4. load_modules

**Entry Point:** `LoadModules.java` (includes `ModuleLoadResult` inner class)

**Responsibility:** Loads compiled modules into memory (class loading and instantiation).

**Key Functionality:**
- Creates class loaders for module dependencies
- Loads Main class from compiled bytecode
- Instantiates GameModule instances
- Handles JavaFX platform initialization
- Includes timeout protection

---

### 5. module_finding

**Entry Point:** `ModuleDiscovery.java`

**Responsibility:** Finds and loads a game module by its game name.

**Key Functionality:**
- Scans modules directory for available modules
- Loads modules one by one until finding a match
- Stops early when match is found for efficiency

---

### 6. module_root_scanning

**Entry Point:** `ScanForModuleFolders.java`

**Responsibility:** Scans the filesystem to find module directories.

**Key Functionality:**
- Checks directory accessibility
- Scans modules directory for potential module folders
- Filters out infrastructure directories (target, .git, etc.)
- Returns list of candidate module directories

---

### 7. module_source_validation

**Entry Point:** `ModuleSourceValidator.java`

**Responsibility:** Validates that module source files have the correct structure and required files.

**Key Functionality:**
- Checks for required files (Main.java, Metadata.java)
- Validates file structure and directory layout
- Verifies minimal API signatures in source files

---

### 8. module_target_validation

**Entry Point:** `ModuleTargetValidator.java`

**Responsibility:** Validates loaded module classes after they've been loaded into memory.

**Key Functionality:**
- Validates that Main class implements GameModule interface
- Performs post-load class validation
- Ensures loaded modules conform to expected API

---

### 9. on_app_start

**Entry Point:** `ModuleLoadingProcess.java`

**Responsibility:** Coordinates the module loading process during application startup.

**Key Functionality:**
- Orchestrates module discovery, loading, and UI updates
- Manages background thread for module loading
- Coordinates startup phases (loading, validation, UI updates)
- Handles cleanup and shutdown tasks

---

## Package Dependencies

Packages are designed to minimize dependencies:

- **module_finding** → uses `module_root_scanning` and `load_modules`
- **load_modules** → uses `module_source_validation`, `module_target_validation`, `check_compilation_status`, and `compile_modules`
- **on_app_start** → uses `module_root_scanning`, `module_source_validation`, and `load_modules`

## Design Principles

1. **Single Responsibility:** Each package has one clear purpose
2. **Single Entry Point:** Only one class per package is accessed externally
3. **Internal Helpers:** Implementation details are in helper classes/package subdirectories
4. **Clear Boundaries:** Packages use explicit imports and avoid circular dependencies
