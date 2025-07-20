#!/bin/bash

# Multi-Module GDK Runner Script
# This script builds and runs the GDK with multi-module support

echo "🚀 Starting OMG Game Development Kit (Multi-Module)"

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed or not in PATH"
    exit 1
fi

# Build all modules
echo "🔨 Building all modules..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build successful"

# Run the GDK
echo "🎮 Starting GDK..."
cd gdk-core
mvn javafx:run

echo "👋 GDK stopped" 