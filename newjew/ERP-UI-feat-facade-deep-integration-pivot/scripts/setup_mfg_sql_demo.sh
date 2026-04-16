#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

SQLITE_JDBC_VERSION="3.46.1.3"
SQLITE_JDBC_JAR="lib/sqlite-jdbc-${SQLITE_JDBC_VERSION}.jar"
SQLITE_JDBC_URL="https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/${SQLITE_JDBC_VERSION}/sqlite-jdbc-${SQLITE_JDBC_VERSION}.jar"

mkdir -p lib out data

if [[ ! -f "$SQLITE_JDBC_JAR" ]]; then
  echo "Downloading sqlite-jdbc ${SQLITE_JDBC_VERSION}..."
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$SQLITE_JDBC_URL" -o "$SQLITE_JDBC_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$SQLITE_JDBC_URL" -O "$SQLITE_JDBC_JAR"
  else
    echo "Neither curl nor wget is available to download sqlite-jdbc."
    exit 1
  fi
fi

find src -name "*.java" | sort > sources.txt
javac -cp "$SQLITE_JDBC_JAR" -d out @sources.txt

java -cp "out:${SQLITE_JDBC_JAR}" com.erp.manufacturing.bootstrap.ManufacturingDbBootstrap data/mfg_demo.db sql/manufacturing_demo.sql

echo "SQL manufacturing demo setup complete."
