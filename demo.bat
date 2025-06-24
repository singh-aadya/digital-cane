@echo off
echo.
echo ============================================================
echo  ENHANCED INDOOR NAVIGATION SYSTEM - DEMO LAUNCHER
echo ============================================================
echo.
echo This demo showcases the advanced features of the Enhanced 
echo Indoor Navigation System including:
echo.
echo ✨ HIGHLIGHTS:
echo   🧭 Multi-mode navigation (Standard, Wheelchair, Visually Impaired, Emergency)
echo   🏢 Multi-floor building with 21 rooms across 3 floors
echo   🎤 Voice commands and text-to-speech navigation
echo   📱 QR code location scanning simulation
echo   🚧 Dynamic obstacle detection and re-routing
echo   🏷️ Landmark-based navigation instructions
echo   💾 Persistent user preferences and navigation history
echo   🔧 Comprehensive admin panel for building management
echo.
echo 🎯 DEMO SCENARIOS:
echo   1. Basic navigation from entrance to cafeteria
echo   2. Wheelchair-accessible route to 2nd floor consultation room
echo   3. Emergency evacuation to nearest exit
echo   4. Multi-floor navigation using elevators
echo   5. Dynamic obstacle simulation and re-routing
echo   6. Voice command navigation
echo   7. Admin panel building management
echo.
echo ⚠️  IMPORTANT NOTES:
echo   • This is a console-based simulation system
echo   • TTS and voice recognition are simulated via text input/output
echo   • QR codes are selected from a menu for demonstration
echo   • All navigation is audio-guided with visual feedback
echo.
echo 🔧 TECHNICAL FEATURES:
echo   • Dijkstra's algorithm with mode-specific weight calculations
echo   • Graph-based building representation with adjacency lists
echo   • Real-time path blocking and alternative route calculation
echo   • Persistent data storage for maps and user preferences
echo   • Comprehensive accessibility feature modeling
echo.

echo Press any key to start the demo...
pause > nul

echo.
echo 🚀 Starting Enhanced Indoor Navigation System Demo...
echo.

REM Check if compiled
if not exist "build\classes" (
    echo 🔨 Compiling system first...
    call compile.bat
    echo.
)

REM Run the application
cd build\classes
java com.indoor.navigation.EnhancedIndoorNavigationApp

echo.
echo 🎉 Demo completed! Thank you for exploring the Enhanced Indoor Navigation System.
echo.
echo 📚 For more information, see README.md
echo 🔧 To access admin features, run option 19 from the main menu
echo 💾 All preferences are automatically saved between sessions
echo.
cd ..\..

pause