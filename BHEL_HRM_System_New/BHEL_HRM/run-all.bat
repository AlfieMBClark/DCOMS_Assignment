@echo off
REM ============================================
REM BHEL HRM System - Run All Components
REM ============================================
REM Usage: run-all.bat [/ssl]

setlocal enabledelayedexpansion

set SSL_FLAG=%1

echo.
echo ============================================
echo  Starting BHEL HRM System...
if "%SSL_FLAG%"=="/ssl" (
    echo  Mode: SSL ENABLED
)
echo ============================================
echo.

REM Build first
echo Step 1: Compiling project...
call compile.bat
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo Step 2: Starting Database Server (port 1098)...
start "Database Server" cmd /k "call run-database.bat 1098 data %SSL_FLAG%"
timeout /t 3 /nobreak

echo Step 3: Starting Application Server (port 1099)...
start "Application Server" cmd /k "call run-server.bat 1099 localhost 1098 %SSL_FLAG%"
timeout /t 2 /nobreak

echo Step 4: Starting Client...
start "BHEL Client" cmd /k "call run-client.bat localhost 1099 %SSL_FLAG%"

echo.
echo ============================================
echo  All components started successfully!
echo ============================================
echo.
echo Check the new Command Prompt windows for output.
echo Close any window to stop that component.
echo.
pause
