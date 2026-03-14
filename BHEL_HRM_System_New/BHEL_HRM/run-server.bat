@echo off
REM ============================================
REM BHEL HRM System - Run Application Server
REM ============================================
REM Usage: run-server.bat [port] [dbHost] [dbPort] [/ssl]
REM Examples:
REM   run-server.bat
REM   run-server.bat 1099 localhost 1098
REM   run-server.bat 1099 localhost 1098 /ssl

setlocal enabledelayedexpansion

set PORT=%1
set DB_HOST=%2
set DB_PORT=%3
set SSL_MODE=%4

if "%PORT%"=="" set PORT=1099
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=1098

set TITLE=BHEL Application Server - Port %PORT%
if "%SSL_MODE%"=="/ssl" (
    set TITLE=!TITLE! [SSL]
)

title !TITLE!

set JAVA_ARGS=-cp out/ server.ServerMain %PORT% %DB_HOST% %DB_PORT%

if "%SSL_MODE%"=="/ssl" (
    set JAVA_ARGS=-Dssl.enabled=true ^
                  -Djavax.net.ssl.keyStore=certs/server.keystore ^
                  -Djavax.net.ssl.keyStorePassword=bhel2024 ^
                  -Djavax.net.ssl.trustStore=certs/client.truststore ^
                  -Djavax.net.ssl.trustStorePassword=bhel2024 ^
                  %JAVA_ARGS%
)

echo Starting Application Server on port %PORT%...
echo Connecting to Database Server at %DB_HOST%:%DB_PORT%...
if "%SSL_MODE%"=="/ssl" echo SSL Mode: ENABLED

java %JAVA_ARGS%

pause

