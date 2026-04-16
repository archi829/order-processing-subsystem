#!/usr/bin/env python3
"""
Unified Python launcher for the ERP Swing app.

Default behavior runs full app mode with SQL-backed Manufacturing and mock
fallback for other modules. It replaces these shell scripts:
- run.sh
- scripts/setup_mfg_sql_demo.sh
- scripts/run_mfg_sql_demo.sh
"""

from __future__ import annotations

import argparse
import os
import subprocess
import sys
import urllib.request
from pathlib import Path
from typing import List


SQLITE_JDBC_VERSION = "3.46.1.3"
SQLITE_JDBC_FILE = f"sqlite-jdbc-{SQLITE_JDBC_VERSION}.jar"
SQLITE_JDBC_URL = (
    "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/"
    f"{SQLITE_JDBC_VERSION}/{SQLITE_JDBC_FILE}"
)


def run_cmd(cmd: List[str], cwd: Path) -> None:
    pretty = " ".join(cmd)
    print(f"\n>> {pretty}")
    subprocess.run(cmd, cwd=str(cwd), check=True)


def collect_java_sources(src_root: Path) -> List[Path]:
    return sorted(src_root.rglob("*.java"))


def write_sources_file(root: Path, sources: List[Path]) -> Path:
    sources_file = root / "sources.txt"
    lines = [str(p.relative_to(root)).replace("\\", "/") for p in sources]
    sources_file.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return sources_file


def ensure_sqlite_jar(root: Path) -> Path:
    lib_dir = root / "lib"
    lib_dir.mkdir(parents=True, exist_ok=True)
    jar = lib_dir / SQLITE_JDBC_FILE

    if jar.exists():
        return jar

    print(f"Downloading {SQLITE_JDBC_FILE}...")
    tmp = jar.with_suffix(".jar.part")
    urllib.request.urlretrieve(SQLITE_JDBC_URL, str(tmp))
    tmp.replace(jar)
    return jar


def compile_sources(root: Path, out_dir: Path, classpath: List[Path] | None = None) -> None:
    src_root = root / "src"
    if not src_root.exists():
        raise RuntimeError(f"Missing source folder: {src_root}")

    sources = collect_java_sources(src_root)
    if not sources:
        raise RuntimeError("No Java source files found under src/")

    out_dir.mkdir(parents=True, exist_ok=True)
    sources_file = write_sources_file(root, sources)

    cmd = ["javac", "-d", str(out_dir)]
    if classpath:
        cp = os.pathsep.join(str(p) for p in classpath)
        cmd.extend(["-cp", cp])
    cmd.append(f"@{sources_file}")
    run_cmd(cmd, root)


def run_sql_bootstrap(root: Path, out_dir: Path, sqlite_jar: Path, db_path: str, sql_script: str) -> None:
    cp = os.pathsep.join([str(out_dir), str(sqlite_jar)])
    cmd = [
        "java",
        "-cp",
        cp,
        "com.erp.manufacturing.bootstrap.ManufacturingDbBootstrap",
        db_path,
        sql_script,
    ]
    run_cmd(cmd, root)


def launch_app(root: Path, out_dir: Path, mode: str, sqlite_jar: Path | None, db_path: str) -> None:
    cp_entries = [str(out_dir)]
    if sqlite_jar is not None:
        cp_entries.append(str(sqlite_jar))

    cmd = ["java"]
    if mode == "sql":
        cmd.extend([
            "-Dcom.erp.mfg.sql=true",
            f"-Dcom.erp.mfg.db={db_path}",
        ])

    cmd.extend([
        "-cp",
        os.pathsep.join(cp_entries),
        "com.erp.ERPApplication",
    ])
    run_cmd(cmd, root)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Launch ERP Swing app from Python (mock or SQL manufacturing mode)."
    )
    parser.add_argument(
        "--mode",
        choices=["sql", "mock"],
        default="sql",
        help="Launch mode. 'sql' is full app with SQL-backed Manufacturing. Default: sql",
    )
    parser.add_argument(
        "--db",
        default="data/mfg_demo.db",
        help="SQLite DB path for SQL mode. Default: data/mfg_demo.db",
    )
    parser.add_argument(
        "--sql-script",
        default="sql/manufacturing_demo.sql",
        help="SQL seed script path for SQL mode. Default: sql/manufacturing_demo.sql",
    )
    parser.add_argument(
        "--skip-bootstrap",
        action="store_true",
        help="Skip SQL bootstrap step (SQL mode only).",
    )
    parser.add_argument(
        "--compile-only",
        action="store_true",
        help="Compile (and optionally bootstrap) but do not launch the UI.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    root = Path(__file__).resolve().parent
    out_dir = root / "out"

    try:
        sqlite_jar = None
        classpath = None

        if args.mode == "sql":
            sqlite_jar = ensure_sqlite_jar(root)
            classpath = [sqlite_jar]

        compile_sources(root, out_dir, classpath)

        if args.mode == "sql" and not args.skip_bootstrap:
            run_sql_bootstrap(root, out_dir, sqlite_jar, args.db, args.sql_script)

        if not args.compile_only:
            launch_app(root, out_dir, args.mode, sqlite_jar, args.db)

        print("\nDone.")
        return 0
    except subprocess.CalledProcessError as exc:
        print(f"\nCommand failed with exit code {exc.returncode}: {exc.cmd}", file=sys.stderr)
        return exc.returncode
    except Exception as exc:  # pylint: disable=broad-except
        print(f"\nError: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
