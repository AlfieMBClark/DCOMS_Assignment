@echo off
REM ============================================
REM BHEL HRM System - Setup SSL Certificates
REM ============================================
REM Run this ONCE before using SSL mode

setlocal enabledelayedexpansion

set CERT_DIR=certs
set KEYSTORE=%CERT_DIR%\server.keystore
set TRUSTSTORE=%CERT_DIR%\client.truststore
set CERT_FILE=%CERT_DIR%\server.cer
set STOREPASS=bhel2024
set ALIAS=bhelserver
set VALIDITY=365

echo.
echo ============================================
echo  BHEL HRM - SSL Certificate Generator
echo ============================================
echo.

if not exist "%CERT_DIR%" mkdir "%CERT_DIR%"

REM Step 1: Generate server keystore
echo [1/3] Generating server keystore...
keytool -genkeypair ^
  -alias %ALIAS% ^
  -keyalg RSA ^
  -keysize 2048 ^
  -keystore "%KEYSTORE%" ^
  -storepass %STOREPASS% ^
  -validity %VALIDITY% ^
  -dname "CN=BHEL HRM Server, OU=IT, O=BHEL, L=KL, ST=WP, C=MY" ^
  -keypass %STOREPASS%

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to generate keystore. Is keytool available?
    pause
    exit /b 1
)
echo  Created: %KEYSTORE%

REM Step 2: Export certificate
echo [2/3] Exporting server certificate...
keytool -exportcert ^
  -alias %ALIAS% ^
  -keystore "%KEYSTORE%" ^
  -file "%CERT_FILE%" ^
  -storepass %STOREPASS%
echo  Created: %CERT_FILE%

REM Step 3: Import into truststore
echo [3/3] Importing certificate to client truststore...
keytool -importcert ^
  -alias %ALIAS% ^
  -file "%CERT_FILE%" ^
  -keystore "%TRUSTSTORE%" ^
  -storepass %STOREPASS% ^
  -noprompt
echo  Created: %TRUSTSTORE%

echo.
echo ============================================
echo  SSL Setup Complete!
echo ============================================
echo.
echo Now run with /ssl flag:
echo   run-server.bat database 1098 /ssl
echo   run-client.bat localhost 1098 /ssl
echo.
pause
