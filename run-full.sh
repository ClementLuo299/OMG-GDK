#!/bin/bash

# Build all modules first
echo "Building all modules..."
mvn clean install -DskipTests

# Get the paths to the built JARs
LAUNCHER_JAR="launcher/target/launcher-1.0.0.jar"
EXAMPLE_JAR="modules/example/target/example-module-1.0.0.jar"
TICTACTOE_JAR="modules/tictactoe/target/tictactoe-module-1.0.0.jar"

# Check if JARs exist
if [ ! -f "$LAUNCHER_JAR" ]; then
    echo "Error: Launcher JAR not found at $LAUNCHER_JAR"
    exit 1
fi
if [ ! -f "$EXAMPLE_JAR" ]; then
    echo "Error: Example module JAR not found at $EXAMPLE_JAR"
    exit 1
fi
if [ ! -f "$TICTACTOE_JAR" ]; then
    echo "Error: TicTacToe module JAR not found at $TICTACTOE_JAR"
    exit 1
fi

# Run the GDK using Maven exec plugin with JavaFX
echo "Running GDK with modules..."
cd launcher
mvn javafx:run -Djavafx.mainClass="GDKApplication" -Djavafx.jvmargs="--enable-native-access=javafx.graphics --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED" -Dexec.args="--modules-dir=../modules" 