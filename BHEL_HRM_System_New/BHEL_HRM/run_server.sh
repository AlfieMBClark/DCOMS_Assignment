#!/bin/bash
# ============================================
# BHEL HRM System - Start Server
# ============================================
# Usage: ./run_server.sh [port] [dataDir]
# Default: port=1099, dataDir=data

PORT=${1:-1099}
DATA_DIR=${2:-data}

echo "Starting BHEL HRM Server on port $PORT..."
java -cp out/ server.ServerMain $PORT $DATA_DIR
