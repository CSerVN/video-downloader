#!/bin/bash
OS=$(uname -s)
ARCH=$(uname -m)

if [ "$OS" = "Darwin" ]; then
    # Luồng xử lý cho macOS
    if [ "$ARCH" = "arm64" ]; then
        echo "[System] Detected macOS ARM64 (Apple Silicon). Launching..."
        ./jre-mac-arm/bin/java -jar VideoDownloader.jar &
    else
        echo "[System] Detected macOS x64 (Intel). Launching..."
        ./jre-mac-x64/bin/java -jar VideoDownloader.jar &
    fi
else
    # Luồng xử lý cho Linux
    if [[ "$ARCH" == "aarch64" || "$ARCH" == "arm"* ]]; then
        echo "[System] Detected Linux ARM64. Launching..."
        ./jre-linux-arm/bin/java -jar VideoDownloader.jar &
    else
        echo "[System] Detected Linux x64. Launching..."
        ./jre-linux-x64/bin/java -jar VideoDownloader.jar &
    fi
fi