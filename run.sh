#!/bin/bash

# GDK Runner - Build process moved to JavaFX application

echo "🚀 Starting GDK..."

# Check if launcher classes exist (minimal check)
if [ ! -d "launcher/target/classes" ]; then
    echo "❌ Error: Launcher classes not found at launcher/target/classes"
    echo "💡 Try running: ./run-full.sh"
    exit 1
fi

# Run the GDK (build process will happen inside the JavaFX app)
echo "🎮 Launching GDK..."
cd launcher
mvn exec:java -Dexec.mainClass="launcher.GDKApplication" -q 