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

# Development mode - disable caching for faster UI updates
export MAVEN_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -Dprism.order=d3d,opengl,sw -Dprism.vsync=false -Dprism.forceGPU=true -Djavafx.animation.fullspeed=true -Djavafx.css.cache=false -Djavafx.fxml.cache=false"

echo "🔧 Development mode enabled - caching disabled for faster UI updates"
echo "💡 UI changes will now be visible immediately!"

mvn exec:java -Dexec.mainClass="launcher.GDKApplication" -q 