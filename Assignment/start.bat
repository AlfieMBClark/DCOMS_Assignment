@echo off
TITLE BHEL HR Management System Setup

echo ========================================
echo  BHEL HR MANAGEMENT SYSTEM
echo  Automatic Startup Script
echo ========================================
echo.

REM Automatically compile if not done
if not exist "bin\server\DatabaseServer.class" (
    echo Compiling project first...
    call compile.bat
)

echo Starting Database Server...
start "Database Server" cmd /k "cd bin && title Database Server && java server.DatabaseServer"

REM Give DB Server time to bind port
timeout /t 2 /nobreak > nul

echo Starting Application Server...
start "Application Server" cmd /k "cd bin && title Application Server && java server.ApplicationServer"

REM Give App Server time to bind port
timeout /t 2 /nobreak > nul

echo Starting HR Client...
start "HR Client" cmd /k "cd bin && title HR Client && java client.HRClient"

echo Starting Employee Client...
start "Employee Client" cmd /k "cd bin && title Employee Client && java client.EmployeeClient"

echo.
echo ========================================
echo  All components started successfully!
echo ========================================
echo Close the respective Command Prompt windows to stop the servers/clients.
pause
