#!/bin/bash

echo "🔍 Testing module discovery..."

# Build all modules first
echo "🔨 Building all modules..."
mvn clean install -DskipTests

# Test module discovery with proper classpath
echo "📦 Testing module discovery..."
cd gdk-core

# Create a simple test class
cat > TestDiscovery.java << 'EOF'
import com.utils.ModuleLoader;
import com.game.GameModule;
import java.util.List;

public class TestDiscovery {
    public static void main(String[] args) {
        System.out.println("🔍 Testing module discovery...");
        
        List<GameModule> modules = ModuleLoader.discoverModules("../modules");
        
        System.out.println("📦 Found " + modules.size() + " modules:");
        for (GameModule module : modules) {
            System.out.println("  ✅ " + module.getGameName() + " (" + module.getGameId() + ")");
        }
    }
}
EOF

# Compile the test
javac -cp "target/classes:../modules/tictactoe/target/tictactoe-module-1.0.0.jar" TestDiscovery.java

# Run the test with all dependencies
java -cp "target/classes:../modules/tictactoe/target/tictactoe-module-1.0.0.jar:$HOME/.m2/repository/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar:$HOME/.m2/repository/ch/qos/logback/logback-classic/1.2.11/logback-classic-1.2.11.jar:$HOME/.m2/repository/ch/qos/logback/logback-core/1.2.11/logback-core-1.2.11.jar" TestDiscovery

# Clean up
rm TestDiscovery.java TestDiscovery.class

echo "✅ Test complete!" 