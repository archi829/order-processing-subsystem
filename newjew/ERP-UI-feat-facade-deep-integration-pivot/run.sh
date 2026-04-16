#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

mkdir -p out
find src -name "*.java" | sort > sources.txt
javac -d out @sources.txt

java -cp out com.erp.ERPApplication
