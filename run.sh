#!/bin/bash

# GDK Runner - Build process moved to JavaFX application

echo "🚀 Starting GDK..."

# Check if GDK needs to be rebuilt (only if source files are newer than JAR)
GDK_JAR="gdk/target/gdk-1.0.0-beta.jar"
GDK_SOURCES="gdk/src/main/java"
NEEDS_REBUILD=false

if [ ! -f "$GDK_JAR" ]; then
    NEEDS_REBUILD=true
    echo "📦 GDK JAR not found, will build..."
elif [ -d "$GDK_SOURCES" ] && [ "$GDK_SOURCES" -nt "$GDK_JAR" ]; then
    NEEDS_REBUILD=true
    echo "📦 GDK sources newer than JAR, will rebuild..."
fi

# Only install if needed (saves time on subsequent runs)
if [ "$NEEDS_REBUILD" = true ]; then
    echo "📦 Installing local GDK dependency (and its parent POM)..."
    if ! mvn -q -pl gdk -am install; then
        echo "❌ Failed to install GDK locally."
        exit 1
    fi
else
    echo "✅ GDK is up-to-date, skipping install..."
fi

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

# Use exec:java but with minimal Maven output for faster startup
mvn exec:java -Dexec.mainClass="launcher.GDKApplication" -q 
