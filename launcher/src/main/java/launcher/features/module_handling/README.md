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

---

### 5. module_finding

**Entry Point:** `ModuleDiscovery.java`

**Responsibility:** Finds and loads a game module by its game name.

---

### 6. module_root_scanning

**Entry Point:** `ScanForModuleFolders.java`

**Responsibility:** Scans the filesystem to find module folders.

---

### 7. module_source_validation

**Entry Point:** `ModuleSourceValidator.java`

**Responsibility:** Validates that module source files have the correct structure and required files.

---

### 8. module_target_validation

**Entry Point:** `ModuleTargetValidator.java`

**Responsibility:** Validates loaded module classes after they've been loaded into memory.

---
