#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

"$ROOT_DIR/scripts/setup_mfg_sql_demo.sh"

SQLITE_JDBC_VERSION="3.46.1.3"
SQLITE_JDBC_JAR="lib/sqlite-jdbc-${SQLITE_JDBC_VERSION}.jar"

java \
  -Dcom.erp.mfg.sql=true \
  -Dcom.erp.mfg.db=data/mfg_demo.db \
  -cp "out:${SQLITE_JDBC_JAR}" \
  com.erp.ERPApplication
