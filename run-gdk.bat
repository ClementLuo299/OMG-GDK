@echo off
REM OMG Game Development Kit Runner Script for Windows
REM This script makes it easy to run the GDK

echo 🎮 OMG Game Development Kit
echo ==========================

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java is not installed. Please install Java 11 or higher.
    pause
    exit /b 1
)

REM Check Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
    goto :found_java_version
)
:found_java_version

echo ✅ Java version: %JAVA_VERSION%

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven is not installed. Please install Maven.
    pause
    exit /b 1
)

for /f "tokens=3" %%m in ('mvn -version ^| findstr /i "Apache Maven"') do (
    echo ✅ Maven version: %%m
    goto :found_maven_version
)
:found_maven_version

REM Create modules directory if it doesn't exist
if not exist "modules" (
    echo 📁 Creating modules directory...
    mkdir modules
)

REM Count modules (simplified for Windows)
set MODULE_COUNT=0
for /d %%d in (modules\*) do set /a MODULE_COUNT+=1

if %MODULE_COUNT%==0 (
    echo ⚠️  No game modules found in modules\ directory
    echo    You can add game modules to test them with the GDK
)

echo 📦 Found %MODULE_COUNT% game module(s)

REM Run the GDK
echo.
echo 🚀 Starting GDK...
echo ==========================

REM Use Maven to run the application
mvn clean javafx:run

echo.
echo �� GDK closed
pause 