#!/bin/bash

# GDK Runner - Fast incremental build

echo "🚀 Starting GDK..."

# Check if we need to build
NEED_BUILD=false

# Check if compiled classes exist
if [ ! -d "launcher/target/classes" ] || [ ! -d "modules/example/target/classes" ] || [ ! -d "modules/tictactoe/target/classes" ]; then
    echo "📦 Building modules (classes missing)..."
    NEED_BUILD=true
else
    # Simple check: if target directories exist and are recent, skip build
    LAUNCHER_AGE=$(( $(date +%s) - $(stat -c %Y "launcher/target/classes" 2>/dev/null || echo 0) ))
    EXAMPLE_AGE=$(( $(date +%s) - $(stat -c %Y "modules/example/target/classes" 2>/dev/null || echo 0) ))
    TICTACTOE_AGE=$(( $(date +%s) - $(stat -c %Y "modules/tictactoe/target/classes" 2>/dev/null || echo 0) ))
    
    # If all are less than 5 minutes old, assume no changes
    if [ "$LAUNCHER_AGE" -lt 300 ] && [ "$EXAMPLE_AGE" -lt 300 ] && [ "$TICTACTOE_AGE" -lt 300 ]; then
        echo "✅ Using existing builds (recent compilation detected)"
    else
        echo "📦 Building modules (checking for changes)..."
        NEED_BUILD=true
    fi
fi

# Build only if necessary - use fast incremental build
if [ "$NEED_BUILD" = true ]; then
    echo "🔨 Building modules (incremental)..."
    
    # Build GDK first (if needed)
    if [ ! -d "gdk/target/classes" ]; then
        echo "📦 Building GDK..."
        (cd gdk && mvn compile -DskipTests -q)
    fi
    
    # Build example module (fast)
    echo "📦 Building example module..."
    (cd modules/example && mvn compile -DskipTests -q)
    
    # Build tictactoe module (fast)
    echo "📦 Building tictactoe module..."
    (cd modules/tictactoe && mvn compile -DskipTests -q)
    
    # Build launcher (fast)
    echo "📦 Building launcher..."
    (cd launcher && mvn compile -DskipTests -q)
    
    if [ $? -ne 0 ]; then
        echo "❌ Incremental build failed, use run-full.sh for complete rebuild"
        exit 1
    fi
fi

# Check if compiled classes exist after build
if [ ! -d "launcher/target/classes" ]; then
    echo "❌ Error: Launcher classes not found at launcher/target/classes"
    echo "💡 Try running: ./run-full.sh"
    exit 1
fi

if [ ! -d "modules/example/target/classes" ]; then
    echo "❌ Error: Example module classes not found at modules/example/target/classes"
    echo "💡 Try running: ./run-full.sh"
    exit 1
fi

if [ ! -d "modules/tictactoe/target/classes" ]; then
    echo "❌ Error: TicTacToe module classes not found at modules/tictactoe/target/classes"
    echo "💡 Try running: ./run-full.sh"
    exit 1
fi

# Run the GDK
echo "🎮 Launching GDK..."
cd launcher
mvn javafx:run -Djavafx.mainClass="GDKApplication" -q 