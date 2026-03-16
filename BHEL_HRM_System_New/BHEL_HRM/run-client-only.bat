@echo off
REM ============================================
REM BHEL HRM System - CLIENT ONLY (Laptop C)
REM ============================================
REM Connects to the primary server as a plain client.
REM No failover — if primary dies, this client disconnects.
REM
REM BEFORE RUNNING: Edit the SERVER_IP below to match Laptop A's IP address.

setlocal enabledelayedexpansion

REM =============================================
REM  EDIT THIS: Set Laptop A's IP address here
REM =============================================
set SERVER_IP=192.168.100.5
REM =============================================

set SERVER_PORT=1099

echo.
echo ============================================
echo  BHEL HRM - Client Only
echo  Connecting to %SERVER_IP%:%SERVER_PORT%
echo ============================================
echo.

java -Dssl.enabled=true -Djavax.net.ssl.trustStore=certs/client.truststore -Djavax.net.ssl.trustStorePassword=bhel2024 -cp out/ client.ClientMain %SERVER_IP% %SERVER_PORT%

pause
