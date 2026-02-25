#!/bin/bash
# ============================================
# BHEL HRM System - Build Script
# ============================================

echo "============================================"
echo "  Building BHEL HRM System..."
echo "============================================"

# Create output directory
mkdir -p out

# Compile all Java files
javac -d out \
  src/common/models/*.java \
  src/common/interfaces/*.java \
  src/utils/*.java \
  src/server/*.java \
  src/client/*.java \
  2>&1

if [ $? -eq 0 ]; then
    echo "============================================"
    echo "  Build SUCCESSFUL!"
    echo "============================================"
    echo ""
    echo "  To run the server:"
    echo "    ./run_server.sh"
    echo ""
    echo "  To run the client:"
    echo "    ./run_client.sh"
    echo "============================================"
else
    echo "============================================"
    echo "  Build FAILED! Check errors above."
    echo "============================================"
    exit 1
fi
