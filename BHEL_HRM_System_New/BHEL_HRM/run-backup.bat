@echo off
REM ============================================
REM BHEL HRM System - BACKUP LAPTOP (One-Click)
REM ============================================
REM Starts: Backup Database, Backup App Server, Client with failover
REM
REM BEFORE RUNNING: Edit the PRIMARY_IP below to match Laptop A's IP address.
REM Find it by running "ipconfig" on Laptop A.

setlocal enabledelayedexpansion

REM =============================================
REM  EDIT THIS: Set Laptop A's IP address here
REM =============================================
set PRIMARY_IP=192.168.100.5
REM =============================================

set PRIMARY_APP_PORT=1099
set BACKUP_DB_PORT=2098
set BACKUP_APP_PORT=2099

echo.
echo ============================================
echo  BHEL HRM - Backup Laptop Setup
echo ============================================
echo  Primary (Laptop A):  %PRIMARY_IP%:%PRIMARY_APP_PORT%
echo  Backup DB:           localhost:%BACKUP_DB_PORT%
echo  Backup App:          localhost:%BACKUP_APP_PORT%
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

REM Step 2: Start Backup Database Server
echo Step 2: Starting Backup Database Server (port %BACKUP_DB_PORT%)...
start "Backup DB Server" cmd /k "java -cp out/ server.DatabaseServer %BACKUP_DB_PORT% data_backup"
timeout /t 3 /nobreak >nul

REM Step 3: Start Backup Application Server
echo Step 3: Starting Backup App Server (port %BACKUP_APP_PORT%)...
start "Backup App Server" cmd /k "java -cp out/ server.ServerMain %BACKUP_APP_PORT% localhost %BACKUP_DB_PORT%"
timeout /t 2 /nobreak >nul

REM Step 4: Start Client with failover
echo Step 4: Starting Client (failover: primary=%PRIMARY_IP%:%PRIMARY_APP_PORT%, backup=localhost:%BACKUP_APP_PORT%)...
start "BHEL Client (Failover)" cmd /k "java -cp out/ client.ClientMain %PRIMARY_IP% %PRIMARY_APP_PORT% localhost %BACKUP_APP_PORT%"

echo.
echo ============================================
echo  Backup laptop is ready!
echo ============================================
echo  Client connects to Laptop A first.
echo  If Laptop A goes down, client auto-switches
echo  to the local backup server.
echo ============================================
echo.
pause
