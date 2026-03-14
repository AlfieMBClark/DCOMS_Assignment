#!/bin/bash
# ============================================
# BHEL HRM System - Start Client with SSL/TLS
# ============================================

HOST=${1:-localhost}
PORT=${2:-1099}
STOREPASS="bhel2024"

if [ ! -f "certs/client.truststore" ]; then
    echo "ERROR: SSL certificates not found. Run ./setup_ssl.sh first!"
    exit 1
fi

echo "Connecting to BHEL HRM Server (SSL) at $HOST:$PORT..."
java -cp out/ \
  -Djavax.net.ssl.trustStore=certs/client.truststore \
  -Djavax.net.ssl.trustStorePassword=$STOREPASS \
  client.ClientMain $HOST $PORT
