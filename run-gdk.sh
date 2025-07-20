#!/bin/bash

# OMG Game Development Kit Runner Script
# This script makes it easy to run the GDK

echo "🎮 OMG Game Development Kit"
echo "=========================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 11 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "❌ Java 11 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven."
    exit 1
fi

echo "✅ Maven version: $(mvn -version | head -n 1)"

# Create modules directory if it doesn't exist
if [ ! -d "modules" ]; then
    echo "📁 Creating modules directory..."
    mkdir -p modules
fi

# Check if modules exist
MODULE_COUNT=$(find modules -maxdepth 1 -type d | wc -l)
MODULE_COUNT=$((MODULE_COUNT - 1))  # Subtract 1 for the modules directory itself

if [ "$MODULE_COUNT" -eq 0 ]; then
    echo "⚠️  No game modules found in modules/ directory"
    echo "   You can add game modules to test them with the GDK"
fi

echo "📦 Found $MODULE_COUNT game module(s)"

# Run the GDK
echo ""
echo "🚀 Starting GDK..."
echo "=========================="

# Use Maven to run the application
mvn clean javafx:run

echo ""
echo "👋 GDK closed" 