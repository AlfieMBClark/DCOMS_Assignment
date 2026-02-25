#!/bin/bash
# ============================================
# BHEL HRM System - Start Client
# ============================================
# Usage: ./run_client.sh [host] [port]
# Default: host=localhost, port=1099

HOST=${1:-localhost}
PORT=${2:-1099}

echo "Connecting to BHEL HRM Server at $HOST:$PORT..."
java -cp out/ client.ClientMain $HOST $PORT
