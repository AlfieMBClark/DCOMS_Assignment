@echo off
REM Compile Script for BHEL HR Management System
REM This script compiles all Java source files

echo ========================================
echo  BHEL HR Management System - Compiler
echo ========================================
echo.

REM Check if src directory exists
if not exist "src" (
    echo ERROR: src directory not found!
    echo Please run this script from the Assignment directory.
    pause
    exit /b 1
)

REM Create bin directory if it doesn't exist
if not exist "bin" (
    echo Creating bin directory...
    mkdir bin
)

echo Compiling Java source files...
echo.

REM Compile all Java files
javac -d bin -sourcepath src src\models\*.java src\interfaces\*.java src\utils\*.java src\server\*.java src\client\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo  Compilation Successful!
    echo ========================================
    echo.
    echo Compiled files are in the 'bin' directory.
    echo.
    echo Next steps:
    echo 1. Run start.bat on Database Server laptop
    echo 2. Run  on Application Server laptop
    echo 3. Run  or  on Client laptop
    echo.
) else (
    echo.
    echo ========================================
    echo  Compilation Failed!
    echo ========================================
    echo.
    echo Please check the error messages above.
    echo.
)

pause
