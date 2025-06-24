@echo off
echo.
echo ============================================================
echo  ENHANCED INDOOR NAVIGATION SYSTEM - BUILD SCRIPT
echo ============================================================
echo.

REM Create build directory
if not exist "build\classes" mkdir build\classes

echo 🔨 Compiling Java source files...

REM Find all Java files and compile them
dir /s /b src\*.java > sources.txt

REM Compile with proper classpath
javac -d build\classes -cp "src\main\java" @sources.txt

if %ERRORLEVEL% EQU 0 (
    echo ✅ Compilation successful!
    echo.
    echo 📁 Compiled classes are in: build\classes
    echo 🚀 Run the application with: run.bat
) else (
    echo ❌ Compilation failed!
    echo Please check the error messages above.
)

REM Clean up
del sources.txt

echo.
pause