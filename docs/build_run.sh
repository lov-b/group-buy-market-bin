#!/bin/bash

set -e

echo "=========================================="
echo "[1/4] Starting Maven Build..."
echo "=========================================="
mvn clean install

echo
echo "=========================================="
echo "[2/4] Entering directory..."
echo "=========================================="
cd group-buy-market-bin-app

echo
echo "=========================================="
echo "[3/4] Running build script..."
echo "=========================================="
chmod +x build.sh
./build.sh

echo
echo "=========================================="
echo "[4/4] Starting Docker Compose..."
echo "=========================================="
cd docs/tag/v3.0/
docker-compose -f docker-compose-app-v3.0.yml up -d

echo
echo "=========================================="
echo "[SUCCESS] All tasks completed successfully!"
echo "=========================================="
