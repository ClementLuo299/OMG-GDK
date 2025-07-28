#!/bin/bash

echo "🎮 GDK Module Testing Script"
echo "============================"
echo ""

echo "📋 Current modules:"
ls -1 modules/ | grep -v "README.md" | sort
echo ""

echo "🧪 Testing scenarios:"
echo "1. Start the app with all modules"
echo "2. Comment out a module's Main.java and refresh"
echo "3. Uncomment the module and refresh"
echo "4. Add a new module and refresh"
echo "5. Remove a module directory and refresh"
echo ""

echo "📝 Quick commands:"
echo "  Comment out a module: sed -i '1s/^/\/\/ /' modules/snake/src/main/java/Main.java"
echo "  Uncomment a module: sed -i '1s/^\/\/ //' modules/snake/src/main/java/Main.java"
echo "  Remove a module: rm -rf modules/snake"
echo "  Add a module: cp -r modules/example modules/newgame"
echo ""

echo "🎯 Expected behaviors:"
echo "  ✅ New modules: '✅ New game module '[name]' was added'"
echo "  ⚠️  Removed modules: '⚠️ Game module '[name]' was removed'"
echo "  🔄 No recompilation messages (silent compilation)"
echo ""

echo "🚀 Ready to test! Start the app with './run.sh'" 