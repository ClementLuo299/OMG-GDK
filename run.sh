#!/bin/bash

# GDK Runner - Uses compiled classes

echo "🚀 Starting GDK..."

# Check if we need to build
NEED_BUILD=false

# Check if compiled classes exist
if [ ! -d "gdk-core/target/classes" ] || [ ! -d "modules/tictactoe/target/classes" ]; then
    echo "📦 Building modules (classes missing)..."
    NEED_BUILD=true
else
    # Check if source files are newer than compiled classes
    GDK_CLASSES_TIME=$(stat -c %Y "gdk-core/target/classes" 2>/dev/null || echo 0)
    TICTACTOE_CLASSES_TIME=$(stat -c %Y "modules/tictactoe/target/classes" 2>/dev/null || echo 0)
    
    # Find newest source file
    NEWEST_SOURCE=$(find . -name "*.java" -o -name "*.fxml" -o -name "*.css" | xargs stat -c %Y 2>/dev/null | sort -n | tail -1)
    
    if [ "$NEWEST_SOURCE" -gt "$GDK_CLASSES_TIME" ] || [ "$NEWEST_SOURCE" -gt "$TICTACTOE_CLASSES_TIME" ]; then
        echo "📦 Building modules (source files changed)..."
        NEED_BUILD=true
    else
        echo "✅ Using existing builds (no changes detected)"
    fi
fi

# Build only if necessary
if [ "$NEED_BUILD" = true ]; then
    echo "🔨 Building modules..."
    mvn compile -DskipTests -q
    if [ $? -ne 0 ]; then
        echo "❌ Build failed, trying full build..."
        mvn clean install -DskipTests -q
    fi
fi

# Check if compiled classes exist after build
if [ ! -d "gdk-core/target/classes" ]; then
    echo "❌ Error: GDK Core classes not found at gdk-core/target/classes"
    exit 1
fi

if [ ! -d "modules/tictactoe/target/classes" ]; then
    echo "❌ Error: TicTacToe module classes not found at modules/tictactoe/target/classes"
    exit 1
fi

# Run the GDK
echo "🎮 Launching GDK..."
cd gdk-core
mvn javafx:run -Djavafx.mainClass="com.GDKApplication" -Dexec.args="--modules-dir=../modules" -q 