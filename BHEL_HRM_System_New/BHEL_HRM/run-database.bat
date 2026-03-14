@echo off
REM ============================================
REM BHEL HRM System - Run Database Server
REM ============================================
REM Usage: run-database.bat [port] [dataDir] [/ssl]
REM Examples:
REM   run-database.bat
REM   run-database.bat 1098 data /ssl

setlocal enabledelayedexpansion

set PORT=%1
set DATA_DIR=%2
set SSL_MODE=%3

if "%PORT%"=="" set PORT=1098
if "%DATA_DIR%"=="" set DATA_DIR=data

set TITLE=BHEL Database Server - Port %PORT%
if "%SSL_MODE%"=="/ssl" (
    set TITLE=!TITLE! [SSL]
)

title !TITLE!

set JAVA_ARGS=-cp out/ server.DatabaseServer %PORT% %DATA_DIR%

if "%SSL_MODE%"=="/ssl" (
    set JAVA_ARGS=-Dssl.enabled=true ^
                  -Djavax.net.ssl.keyStore=certs/server.keystore ^
                  -Djavax.net.ssl.keyStorePassword=bhel2024 ^
                  -Djavax.net.ssl.trustStore=certs/client.truststore ^
                  -Djavax.net.ssl.trustStorePassword=bhel2024 ^
                  %JAVA_ARGS%
)

echo Starting Database Server on port %PORT%...
if "%SSL_MODE%"=="/ssl" echo SSL Mode: ENABLED

java %JAVA_ARGS%

pause
