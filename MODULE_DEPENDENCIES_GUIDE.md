# Module Dependencies Guide

This guide explains the different approaches for handling Maven dependencies in GDK modules.

## Overview

There are several ways to handle module dependencies, each with different trade-offs:

1. **Multi-Module Maven Project** (Recommended)
2. **JAR-based Modules**
3. **Dynamic Classpath Loading**
4. **Plugin Architecture**

---

## 1. Multi-Module Maven Project (Recommended)

### Structure
```
omg-gdk/
├── pom.xml (parent)
├── gdk-core/
│   ├── pom.xml
│   └── src/
├── modules/
│   ├── tictactoe/
│   │   ├── pom.xml
│   │   └── src/
│   └── example/
│       ├── pom.xml
│       └── src/
```

### Benefits
- ✅ Each module has its own dependencies
- ✅ Clean separation of concerns
- ✅ Easy to build and test modules independently
- ✅ Maven handles dependency resolution
- ✅ IDE support for each module

### Example Module pom.xml
```xml
<parent>
    <groupId>com.omg</groupId>
    <artifactId>omg-gdk</artifactId>
    <version>1.0.0</version>
</parent>

<artifactId>tictactoe-module</artifactId>

<dependencies>
    <!-- Core GDK -->
    <dependency>
        <groupId>com.omg</groupId>
        <artifactId>gdk-core</artifactId>
        <version>${project.version}</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Module-specific dependencies -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>
    
    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
        <version>4.1.94.Final</version>
    </dependency>
</dependencies>
```

### Usage
```bash
# Build all modules
mvn clean install

# Build specific module
cd modules/tictactoe
mvn clean package

# Run GDK with modules
cd gdk-core
mvn javafx:run
```

---

## 2. JAR-based Modules

### Structure
```
omg-gdk/
├── modules/
│   ├── tictactoe.jar (self-contained)
│   ├── example.jar (self-contained)
│   └── chess.jar (self-contained)
```

### Benefits
- ✅ Modules are completely self-contained
- ✅ Can be distributed independently
- ✅ No dependency conflicts
- ✅ Easy to add/remove modules

### Example Module JAR
Each module JAR contains:
- All its dependencies (shaded)
- Main class implementing GameModule
- Resources (FXML, CSS, images)

### Usage
```java
// Load modules from JAR files
List<GameModule> modules = JarModuleLoader.discoverModules();

// Each module has its own classloader and dependencies
for (GameModule module : modules) {
    // Module runs with its own dependencies
    module.launchGame(stage, mode, players, options);
}
```

---

## 3. Dynamic Classpath Loading

### Approach
- Modules are compiled separately
- Dependencies are managed at runtime
- ClassLoader hierarchy handles conflicts

### Benefits
- ✅ Runtime module loading
- ✅ Flexible dependency management
- ✅ Hot reloading possible

### Example
```java
public class DynamicModuleLoader {
    public static GameModule loadModule(File moduleDir) {
        // Create classloader with module dependencies
        URLClassLoader classLoader = new URLClassLoader(
            getModuleDependencies(moduleDir),
            DynamicModuleLoader.class.getClassLoader()
        );
        
        // Load and instantiate module
        Class<?> moduleClass = classLoader.loadClass("Main");
        return (GameModule) moduleClass.newInstance();
    }
}
```

---

## 4. Plugin Architecture

### Approach
- Modules implement a plugin interface
- GDK provides extension points
- Dependency injection for services

### Benefits
- ✅ Loose coupling
- ✅ Extensible architecture
- ✅ Service-oriented design

### Example
```java
public interface GamePlugin {
    String getPluginId();
    GameModule createGameModule();
    List<Class<?>> getRequiredServices();
}

public class TicTacToePlugin implements GamePlugin {
    @Override
    public GameModule createGameModule() {
        return new TicTacToeModule();
    }
    
    @Override
    public List<Class<?>> getRequiredServices() {
        return Arrays.asList(NetworkService.class, AudioService.class);
    }
}
```

---

## Comparison

| Approach | Dependencies | Complexity | Flexibility | Distribution |
|----------|-------------|------------|-------------|--------------|
| Multi-Module | Per module | Low | High | Source code |
| JAR-based | Self-contained | Medium | High | JAR files |
| Dynamic | Runtime | High | Very High | Mixed |
| Plugin | Injected | High | Very High | Mixed |

---

## Recommendations

### For Development
Use **Multi-Module Maven Project**:
- Easy to develop and debug
- Good IDE support
- Clear dependency management

### For Distribution
Use **JAR-based Modules**:
- Self-contained modules
- Easy to distribute
- No dependency conflicts

### For Advanced Use Cases
Use **Plugin Architecture**:
- Maximum flexibility
- Service-oriented design
- Runtime extensibility

---

## Implementation Steps

### 1. Multi-Module Setup
1. Convert main pom.xml to parent
2. Create gdk-core module
3. Move existing code to gdk-core
4. Create module pom.xml files
5. Update ModuleLoader to use new structure

### 2. JAR-based Setup
1. Create JarModuleLoader
2. Build modules as JARs
3. Place JARs in modules directory
4. Update GDK to use JarModuleLoader

### 3. Dynamic Setup
1. Create DynamicModuleLoader
2. Implement dependency resolution
3. Handle classloader hierarchy
4. Add hot reloading support

### 4. Plugin Setup
1. Define plugin interfaces
2. Create extension points
3. Implement service injection
4. Add plugin discovery

---

## Example: Adding a New Module

### Multi-Module Approach
```bash
# 1. Create module directory
mkdir modules/chess

# 2. Create pom.xml
# (see example above)

# 3. Add to parent pom.xml
<modules>
    <module>modules/chess</module>
</modules>

# 4. Build
mvn clean install
```

### JAR-based Approach
```bash
# 1. Create module
mkdir modules/chess
# Add source and pom.xml

# 2. Build JAR
cd modules/chess
mvn clean package

# 3. Copy JAR to modules directory
cp target/chess-module.jar ../../modules/
```

---

## Best Practices

1. **Keep modules independent** - Minimize cross-module dependencies
2. **Use semantic versioning** - For module APIs and dependencies
3. **Document dependencies** - Clear requirements for each module
4. **Test modules separately** - Ensure they work independently
5. **Handle conflicts gracefully** - Provide fallbacks and error handling
6. **Use dependency scopes** - `provided` for GDK core, `compile` for module-specific
7. **Version management** - Use dependency management in parent POM
8. **Resource isolation** - Modules should manage their own resources

---

## Troubleshooting

### Common Issues

1. **ClassNotFoundException**
   - Check if dependencies are included in module JAR
   - Verify classpath configuration

2. **Version Conflicts**
   - Use dependency management in parent POM
   - Exclude conflicting dependencies

3. **Resource Loading**
   - Ensure resources are in correct location
   - Use proper resource loading methods

4. **Build Issues**
   - Check Maven configuration
   - Verify module structure

### Debug Tips

1. Enable debug logging
2. Check classloader hierarchy
3. Verify JAR contents
4. Test modules independently
5. Use Maven dependency tree analysis 