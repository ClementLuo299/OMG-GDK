#!/bin/bash

# Build all modules first
echo "Building all modules..."
mvn clean install -DskipTests

# Get the paths to the built JARs
GDK_CORE_JAR="gdk-core/target/gdk-core-1.0.0.jar"
TICTACTOE_JAR="modules/tictactoe/target/tictactoe-module-1.0.0.jar"

# Check if JARs exist
if [ ! -f "$GDK_CORE_JAR" ]; then
    echo "Error: GDK Core JAR not found at $GDK_CORE_JAR"
    exit 1
fi

if [ ! -f "$TICTACTOE_JAR" ]; then
    echo "Error: TicTacToe module JAR not found at $TICTACTOE_JAR"
    exit 1
fi

# Run the GDK using Maven exec plugin with JavaFX
echo "Running GDK with modules..."
cd gdk-core
mvn javafx:run -Djavafx.mainClass="com.GDKApplication" -Dexec.args="--modules-dir=../modules" 