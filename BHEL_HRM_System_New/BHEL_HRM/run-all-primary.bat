@echo off
REM ============================================
REM BHEL HRM System - PRIMARY LAPTOP (One-Click)
REM ============================================
REM Starts: Primary Database (with replication), Primary App Server, Client
REM
REM BEFORE RUNNING: Edit the BACKUP_IP below to match Laptop B's IP address.
REM Find it by running "ipconfig" on Laptop B.

setlocal enabledelayedexpansion

REM =============================================
REM  EDIT THIS: Set Laptop B's IP address here
REM =============================================
set BACKUP_IP=192.168.100.10
REM =============================================

set PRIMARY_DB_PORT=1098
set PRIMARY_APP_PORT=1099
set BACKUP_DB_PORT=2098

echo.
echo ============================================
echo  BHEL HRM - Primary Laptop Setup
echo ============================================
echo  Primary DB:    localhost:%PRIMARY_DB_PORT%
echo  Primary App:   localhost:%PRIMARY_APP_PORT%
echo  Backup DB:     %BACKUP_IP%:%BACKUP_DB_PORT%
echo ============================================
echo.

REM Step 1: Compile
echo Step 1: Compiling...
call compile.bat
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

REM Step 2: Start Primary Database Server (with replication to backup)
echo Step 2: Starting Primary Database Server (port %PRIMARY_DB_PORT%)...
start "Primary DB Server" cmd /k "java -cp out/ server.DatabaseServer %PRIMARY_DB_PORT% data --backup %BACKUP_IP%:%BACKUP_DB_PORT%"
timeout /t 3 /nobreak >nul

REM Step 3: Start Primary Application Server
echo Step 3: Starting Primary App Server (port %PRIMARY_APP_PORT%)...
start "Primary App Server" cmd /k "java -cp out/ server.ServerMain %PRIMARY_APP_PORT% localhost %PRIMARY_DB_PORT%"
timeout /t 2 /nobreak >nul

REM Step 4: Start Client (optional - you can also run from Laptop B or C)
echo Step 4: Starting Client...
start "BHEL Client" cmd /k "java -cp out/ client.ClientMain localhost %PRIMARY_APP_PORT%"
echo.
echo ============================================
echo  Primary laptop is ready!
echo ============================================
echo  Data is being replicated to %BACKUP_IP%
echo  Start run-backup.bat on Laptop B now.
echo ============================================
echo.
pause
