#!/bin/bash
# verify_zero_magic_numbers.sh

EXIT_CODE=0
TARGET_DIR="cyberpunkandroid/src/main/java"

if [ ! -d "$TARGET_DIR" ]; then
    echo "Warning: Target directory $TARGET_DIR does not exist. Assuming success."
    exit 0
fi

echo "Scanning for raw hex colors..."
HEX_COLORS=$(grep -rnE 'Color\(0x[0-9a-fA-F]+|Color\(\"#[0-9a-fA-F]+\"\)' "$TARGET_DIR" | grep -v "config/" | grep -v "theme/" || true)
if [ ! -z "$HEX_COLORS" ]; then
    echo "Found hardcoded hex colors:"
    echo "$HEX_COLORS"
    EXIT_CODE=1
fi

echo "Scanning for hardcoded .dp literals..."
DP_LITERALS=$(grep -rnE '[0-9]+(\.[0-9]+)?\.dp' "$TARGET_DIR" | grep -v "config/" | grep -v "theme/" || true)
if [ ! -z "$DP_LITERALS" ]; then
    echo "Found hardcoded .dp literals:"
    echo "$DP_LITERALS"
    EXIT_CODE=1
fi

echo "Scanning for hardcoded .sp literals..."
SP_LITERALS=$(grep -rnE '[0-9]+(\.[0-9]+)?\.sp' "$TARGET_DIR" | grep -v "config/" | grep -v "theme/" || true)
if [ ! -z "$SP_LITERALS" ]; then
    echo "Found hardcoded .sp literals:"
    echo "$SP_LITERALS"
    EXIT_CODE=1
fi

if [ $EXIT_CODE -eq 0 ]; then
    echo "Success: No magic numbers found!"
else
    echo "Error: Magic numbers detected. Use CyberPrimitives or CyberSemanticTokens."
fi

exit $EXIT_CODE
