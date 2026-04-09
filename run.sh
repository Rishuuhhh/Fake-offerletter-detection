#!/bin/bash
# Fake Offer Detector — build and run script
# Usage: ./run.sh

set -e

echo "Compiling..."
mkdir -p out
javac -d out $(find src -name "*.java")

echo "Launching..."
java -cp out main.AppLauncher
