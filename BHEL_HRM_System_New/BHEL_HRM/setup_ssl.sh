#!/bin/bash
# ============================================
# BHEL HRM System - SSL Certificate Setup
# ============================================
# Generates self-signed certificates for secure RMI communication.
# Run this ONCE before using SSL mode.

CERT_DIR="certs"
KEYSTORE="$CERT_DIR/server.keystore"
TRUSTSTORE="$CERT_DIR/client.truststore"
CERT_FILE="$CERT_DIR/server.cer"
STOREPASS="bhel2024"
ALIAS="bhelserver"
VALIDITY=365

echo "============================================"
echo "  BHEL HRM - SSL Certificate Generator"
echo "============================================"

mkdir -p $CERT_DIR

# Step 1: Generate server keystore with RSA key pair
echo "[1/3] Generating server keystore..."
keytool -genkeypair \
  -alias $ALIAS \
  -keyalg RSA \
  -keysize 2048 \
  -keystore $KEYSTORE \
  -storepass $STOREPASS \
  -validity $VALIDITY \
  -dname "CN=BHEL HRM Server, OU=IT, O=BHEL, L=KL, ST=WP, C=MY" \
  -keypass $STOREPASS \
  2>/dev/null

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to generate keystore. Is 'keytool' available (JDK required)?"
    exit 1
fi
echo "  Created: $KEYSTORE"

# Step 2: Export server certificate
echo "[2/3] Exporting server certificate..."
keytool -exportcert \
  -alias $ALIAS \
  -keystore $KEYSTORE \
  -file $CERT_FILE \
  -storepass $STOREPASS \
  2>/dev/null
echo "  Created: $CERT_FILE"

# Step 3: Import certificate into client truststore
echo "[3/3] Creating client truststore..."
keytool -importcert \
  -alias $ALIAS \
  -keystore $TRUSTSTORE \
  -file $CERT_FILE \
  -storepass $STOREPASS \
  -noprompt \
  2>/dev/null
echo "  Created: $TRUSTSTORE"

echo ""
echo "============================================"
echo "  SSL Certificates generated successfully!"
echo "============================================"
echo "  Keystore:   $KEYSTORE"
echo "  Truststore: $TRUSTSTORE"
echo "  Password:   $STOREPASS"
echo ""
echo "  Now use the SSL run scripts:"
echo "    ./run_server_ssl.sh"
echo "    ./run_client_ssl.sh"
echo "============================================"
