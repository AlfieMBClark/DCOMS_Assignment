@echo off
REM ============================================
REM BHEL HRM System - Compile
REM ============================================

echo.
echo ============================================
echo  Building BHEL HRM System...
echo ============================================
echo.

if not exist "out" mkdir out

echo Compiling Java files...
javac -d out ^
  src/common/models/*.java ^
  src/common/interfaces/*.java ^
  src/utils/*.java ^
  src/server/*.java ^
  src/client/*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo  Build SUCCESSFUL!
    echo ============================================
    echo.
    echo Usage:
    echo   run-database.bat [port] [dataDir] [/ssl]
    echo   run-server.bat [port] [dbHost] [dbPort] [/ssl]
    echo   run-client.bat [host] [port] [/ssl]
    echo   run-all.bat [/ssl]
    echo.
    echo Examples:
    echo   run-database.bat
    echo   run-database.bat 1098 data /ssl
    echo   run-server.bat
    echo   run-server.bat 1099 localhost 1098 /ssl
    echo   run-client.bat localhost 1099
    echo   run-all.bat /ssl
    echo ============================================
) else (
    echo.
    echo ============================================
    echo  Compilation FAILED!
    echo ============================================
    pause
    exit /b 1
)
