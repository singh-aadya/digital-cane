@echo off
echo.
echo ============================================================
echo  ENHANCED INDOOR NAVIGATION SYSTEM - LAUNCHER
echo ============================================================
echo.

REM Check if compiled classes exist
if not exist "build\classes" (
    echo ❌ Compiled classes not found!
    echo Please run compile.bat first.
    echo.
    pause
    exit /b 1
)

echo 🚀 Starting Enhanced Indoor Navigation System...
echo.

REM Change to build directory and run the application
cd build\classes
java com.indoor.navigation.EnhancedIndoorNavigationApp

echo.
echo 👋 Application terminated.
cd ..\..

pause