#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

mkdir -p out

# 1. Find only the UI's Java files
find src -name "*.java" | sort > sources.txt

# 2. Compile the UI, telling it to use your JARs in the libs folder
# Linux/macOS uses ':' to separate classpath items. Windows uses ';'
javac -cp "libs/*" -d out @sources.txt

# 3. Run the application with both the compiled UI and the libs
case "$(uname -s)" in
	MINGW*|MSYS*|CYGWIN*) CP_SEP=';' ;;
	*) CP_SEP=':' ;;
esac

java -cp "out${CP_SEP}libs/*" com.erp.ERPApplication