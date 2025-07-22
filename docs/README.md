# GDK PlantUML Documentation

This directory contains PlantUML diagrams that visualize the OMG Game Development Kit (GDK) architecture, structure, and relationships.

## 📊 Available Diagrams

### 1. **architecture.puml** - Overall GDK Architecture
- **Purpose**: Shows the high-level architecture and relationships between GDK core components and game modules
- **Key Elements**:
  - GDK Core (gdk module) with interfaces and utilities
  - Launcher application with UI controllers
  - Game modules with their internal structure
  - Relationships and dependencies between components

### 2. **module-structure.puml** - Game Module Structure
- **Purpose**: Detailed view of the Main/GameMetadata/GameModule pattern used in each game module
- **Key Elements**:
  - Main class responsibilities (JSON communication, game control)
  - GameMetadata class responsibilities (configuration, metadata)
  - GameModule class responsibilities (game logic, UI)
  - Delegation patterns and interfaces

### 3. **class-hierarchy.puml** - Class Hierarchy and Inheritance
- **Purpose**: Shows inheritance relationships, interfaces, and class structures
- **Key Elements**:
  - Core interfaces (GameModule, GameEventHandler, GameSettings)
  - Core classes (GameMode, GameDifficulty, GameOptions, GameState, GameEvent)
  - Settings system with abstract GameSetting and concrete implementations
  - Module implementations showing the pattern

### 4. **data-flow.puml** - Data Flow and Interactions
- **Purpose**: Sequence diagram showing how data flows between components
- **Key Elements**:
  - Module discovery process
  - Game configuration and JSON data handling
  - Game launch sequence
  - Event communication
  - Game execution and cleanup

### 5. **package-structure.puml** - Package Structure and File Organization
- **Purpose**: Shows the file and package organization of the entire project
- **Key Elements**:
  - Directory structure
  - Package relationships
  - Resource organization
  - File dependencies

## 🛠️ How to Use These Diagrams

### Prerequisites
1. **PlantUML**: Install PlantUML or use an online version
2. **Java**: PlantUML requires Java to run
3. **Graphviz** (optional): For better diagram rendering

### Installation Options

#### Option 1: Online PlantUML Server
1. Go to [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
2. Copy the content of any `.puml` file
3. Paste it into the online editor
4. The diagram will be generated automatically

#### Option 2: Local Installation
```bash
# Install PlantUML
wget https://github.com/plantuml/plantuml/releases/download/v1.2023.10/plantuml-1.2023.10.jar

# Generate diagrams
java -jar plantuml-1.2023.10.jar docs/*.puml
```

#### Option 3: VS Code Extension
1. Install the "PlantUML" extension in VS Code
2. Open any `.puml` file
3. Use `Ctrl+Shift+P` and run "PlantUML: Preview Current Diagram"

### Command Line Usage
```bash
# Generate all diagrams
java -jar plantuml.jar docs/*.puml

# Generate specific diagram
java -jar plantuml.jar docs/architecture.puml

# Generate with specific format
java -jar plantuml.jar -tpng docs/architecture.puml
```

## 📋 Diagram Descriptions

### Architecture Overview
The GDK follows a modular architecture with clear separation of concerns:

1. **GDK Core (gdk module)**: Provides interfaces, utilities, and shared components
2. **Launcher**: Main application that discovers and manages game modules
3. **Game Modules**: Individual games that implement the GDK interfaces

### Module Pattern
Each game module follows a consistent three-class pattern:

1. **Main**: Entry point, handles JSON communication and game control
2. **GameMetadata**: Contains all game configuration and metadata
3. **GameModule**: Implements the actual game logic and UI

### Key Relationships
- **Delegation**: Main classes delegate to GameMetadata and GameModule
- **Interface Implementation**: All modules implement the GameModule interface
- **Event Communication**: Components communicate through GameEventHandler
- **JSON Data Flow**: Configuration data flows from launcher to modules

## 🎯 Benefits of These Diagrams

1. **Understanding**: Visual representation of complex relationships
2. **Documentation**: Self-updating documentation that reflects code changes
3. **Onboarding**: Help new developers understand the architecture
4. **Design**: Aid in architectural decisions and refactoring
5. **Communication**: Share architecture with stakeholders

## 🔄 Keeping Diagrams Updated

The diagrams are designed to be maintainable:

1. **Interface-based**: Focus on interfaces rather than implementation details
2. **Pattern-based**: Show consistent patterns across modules
3. **Modular**: Each diagram focuses on a specific aspect
4. **Automated**: Can be generated from code comments (with additional setup)

## 📝 Notes

- Diagrams use a consistent color scheme and styling
- All relationships and dependencies are clearly marked
- Notes provide additional context and explanations
- The diagrams are designed to be readable at different zoom levels

## 🤝 Contributing

When adding new components or changing the architecture:

1. Update the relevant PlantUML diagrams
2. Ensure consistency across all diagrams
3. Add notes for complex relationships
4. Test diagram generation to ensure syntax is correct

---

*These diagrams provide a comprehensive view of the GDK architecture and help maintain a clear understanding of the system's design and relationships.* 