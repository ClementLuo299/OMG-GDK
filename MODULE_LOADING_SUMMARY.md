# 📦 GDK Module Loading - Source Only

## 🎯 **Current Module Loading Strategy**

The GDK now uses **source-based module loading only**, which is perfect for development and testing.

---

## 🔍 **How Module Discovery Works**

### **1. Classpath Loading (Primary)**
```java
// GDK looks for modules in the current classpath
String[][] knownModules = {
    {"tictactoe", "TicTacToeModule"},
    {"example", "ExampleGameModule"}
};

// Tries to load: com.games.modules.tictactoe.TicTacToeModule
// Tries to load: com.games.modules.example.ExampleGameModule
```

### **2. Source Directory Loading (Fallback)**
```java
// If not in classpath, looks in modules/ directory
File modulesDir = new File("modules");
File[] moduleDirs = modulesDir.listFiles(File::isDirectory);

// For each directory, tries to find the game module class
```

### **3. Outer Directory Loading (Development)**
```java
// Also checks outer modules directory for development
File outerModulesDir = new File("modules");
```

---

## 🚀 **Benefits of Source-Only Loading**

### **✅ Advantages:**
- **⚡ Fast loading** - Direct class access
- **🔧 Easy debugging** - Source code available
- **🔄 Quick iteration** - No JAR building needed
- **📝 Simple setup** - Just compile and run
- **🐛 Better error messages** - Direct stack traces

### **❌ Limitations:**
- **🔗 Shared dependencies** - Modules share GDK's classpath
- **📦 No isolation** - Modules can't have conflicting dependencies
- **🚀 No distribution** - Can't package individual games
- **🏗️ Development only** - Not suitable for production deployment

---

## 📋 **Module Requirements**

### **1. Class Structure:**
```java
package com.games.modules.tictactoe;

public class TicTacToeModule implements GameModule {
    @Override
    public String getGameId() { return "tictactoe"; }
    
    @Override
    public String getGameName() { return "Tic Tac Toe"; }
    
    @Override
    public Scene launchGame(Stage stage, GameMode mode, int players, GameOptions options) {
        // Game implementation
    }
}
```

### **2. Directory Structure:**
```
modules/
├── tictactoe/
│   ├── src/main/java/com/games/modules/tictactoe/
│   │   ├── TicTacToeModule.java
│   │   └── TicTacToeController.java
│   └── src/main/resources/games/tictactoe/
│       ├── fxml/tictactoe.fxml
│       ├── css/tictactoe.css
│       └── icons/tic_tac_toe_icon.png
└── example/
    └── ... (similar structure)
```

### **3. Maven Integration:**
```xml
<!-- Module pom.xml -->
<parent>
    <groupId>com.omg</groupId>
    <artifactId>gdk-modules</artifactId>
    <version>1.0.0</version>
</parent>
```

---

## 🔧 **Adding New Modules**

### **Step 1: Create Module Structure**
```bash
mkdir modules/mynewgame
cd modules/mynewgame
# Create Maven project structure
```

### **Step 2: Implement GameModule**
```java
public class MyNewGameModule implements GameModule {
    // Implement required methods
}
```

### **Step 3: Build Module**
```bash
cd modules/mynewgame
mvn clean compile
```

### **Step 4: Update ModuleLoader**
```java
// Add to knownModules array in ModuleLoader.java
String[][] knownModules = {
    {"tictactoe", "TicTacToeModule"},
    {"example", "ExampleGameModule"},
    {"mynewgame", "MyNewGameModule"}  // Add this line
};
```

---

## 🎮 **Current Supported Modules**

| Module | Status | Class Name |
|--------|--------|------------|
| **TicTacToe** | ✅ Working | `TicTacToeModule` |
| **Example** | ⚠️ Broken | `ExampleGameModule` |

---

## 🔄 **Development Workflow**

### **1. Normal Development:**
```bash
# Build all modules
mvn clean compile

# Run GDK
./run.sh
```

### **2. Module Development:**
```bash
# Work on specific module
cd modules/tictactoe
# Make changes
mvn compile

# Test in GDK
cd ../..
./run.sh
```

### **3. Adding New Module:**
```bash
# Create new module
mkdir modules/newgame
# Set up Maven project
# Implement GameModule
# Add to ModuleLoader
# Build and test
```

---

## 🚀 **Future JAR Support**

When you're ready to add JAR support later:

1. **Create JarModuleLoader** class
2. **Add JAR discovery** to ModuleLoader
3. **Update GDK** to use both loading strategies
4. **Add JAR building** scripts

**For now, source-based loading is perfect for development!** 🎯 