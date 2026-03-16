@echo off
REM ============================================
REM BHEL HRM System - Run Client
REM ============================================
REM Usage: run-client.bat [host] [port] [/ssl]
REM Examples:
REM   run-client.bat localhost 1099
REM   run-client.bat 192.168.1.100 1099 /ssl

setlocal enabledelayedexpansion

set HOST=%1
set PORT=%2
set SSL_MODE=%3

if "%HOST%"=="" set HOST=localhost
if "%PORT%"=="" set PORT=1099

set TITLE=BHEL HRM Client - %HOST%:%PORT%
if "%SSL_MODE%"=="/ssl" (
    set TITLE=!TITLE! [SSL]
)

title !TITLE!

set JAVA_ARGS=-cp out/ client.ClientMain %HOST% %PORT%

if "%SSL_MODE%"=="/ssl" (
    set JAVA_ARGS=-Dssl.enabled=true ^
                  -Djavax.net.ssl.trustStore=certs/client.truststore ^
                  -Djavax.net.ssl.trustStorePassword=bhel2024 ^
                  %JAVA_ARGS%
)

echo Connecting to server at %HOST%:%PORT%...
if "%SSL_MODE%"=="/ssl" echo SSL Mode: ENABLED

java %JAVA_ARGS%

pause
