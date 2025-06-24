#!/bin/bash

echo "============================================================"
echo " ENHANCED INDOOR NAVIGATION SYSTEM - LAUNCHER"
echo "============================================================"
echo

# Check if compiled classes exist
if [ ! -d "build/classes" ]; then
    echo "❌ Compiled classes not found!"
    echo "Please run ./compile.sh first."
    echo
    exit 1
fi

echo "🚀 Starting Enhanced Indoor Navigation System..."
echo

# Change to build directory and run the application
cd build/classes
java com.indoor.navigation.EnhancedIndoorNavigationApp

echo
echo "👋 Application terminated."
cd ../..