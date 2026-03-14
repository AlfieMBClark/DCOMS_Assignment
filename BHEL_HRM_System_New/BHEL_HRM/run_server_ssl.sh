#!/bin/bash
# ============================================
# BHEL HRM System - Start Server with SSL/TLS
# ============================================
# Run setup_ssl.sh first to generate certificates!

PORT=${1:-1099}
DATA_DIR=${2:-data}
STOREPASS="bhel2024"

if [ ! -f "certs/server.keystore" ]; then
    echo "ERROR: SSL certificates not found. Run ./setup_ssl.sh first!"
    exit 1
fi

echo "Starting BHEL HRM Server (SSL) on port $PORT..."
java -cp out/ \
  -Djavax.net.ssl.keyStore=certs/server.keystore \
  -Djavax.net.ssl.keyStorePassword=$STOREPASS \
  -Djavax.net.ssl.trustStore=certs/client.truststore \
  -Djavax.net.ssl.trustStorePassword=$STOREPASS \
  -Dssl.enabled=true \
  server.ServerMain $PORT $DATA_DIR
