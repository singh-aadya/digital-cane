#!/bin/bash

echo "============================================================"
echo " ENHANCED INDOOR NAVIGATION SYSTEM - BUILD SCRIPT"
echo "============================================================"
echo

# Create build directory
mkdir -p build/classes

echo "🔨 Compiling Java source files..."

# Find all Java files and compile them
find src -name "*.java" -type f > sources.txt

# Compile with proper classpath
javac -d build/classes -cp "src/main/java" @sources.txt

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    echo
    echo "📁 Compiled classes are in: build/classes"
    echo "🚀 Run the application with: ./run.sh"
else
    echo "❌ Compilation failed!"
    echo "Please check the error messages above."
fi

# Clean up
rm sources.txt

echo