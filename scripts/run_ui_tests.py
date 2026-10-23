#!/usr/bin/env python3
"""Run the diploma UI suite on API 36 and export its actual Allure results."""

import argparse
from collections import Counter
from datetime import datetime, timezone
import io
import html
import json
import os
from pathlib import Path, PurePosixPath
import shutil
import re
import subprocess
import sys
import tarfile
import tempfile
import time
import zipfile


ROOT = Path(__file__).resolve().parents[1]
PACKAGE = "ru.edu.qamid"
# Allure Kotlin Android 2.4 uses the target application's Context.filesDir.
DEVICE_RESULTS = "files/allure-results"


class RunError(Exception):
    pass


def find_adb():
    name = "adb.exe" if os.name == "nt" else "adb"
    for variable in ("ANDROID_HOME", "ANDROID_SDK_ROOT"):
        sdk = os.environ.get(variable)
        if sdk and (Path(sdk) / "platform-tools" / name).is_file():
            return str((Path(sdk) / "platform-tools" / name).resolve())
    found = shutil.which(name)
    if found:
        return str(Path(found).resolve())
    raise RunError("adb not found: set ANDROID_HOME or add platform-tools to PATH.")


def adb_text(adb, *args, check=True):
    result = subprocess.run([adb, *args], capture_output=True, text=True,
                            encoding="utf-8", errors="replace", timeout=60)
    if check and result.returncode:
        # Do not echo arbitrary device output, which can contain application data.
        raise RunError("adb operation failed; check the connected device and USB authorization.")
    return result


def choose_device(adb, requested):
    rows = adb_text(adb, "devices").stdout.splitlines()[1:]
    devices = [row.split()[0] for row in rows
               if len(row.split()) >= 2 and row.split()[1] == "device"]
    if requested:
        if requested not in devices:
            raise RunError("The selected device is not connected and authorized.")
        return requested
    if len(devices) != 1:
        raise RunError("Connect exactly one authorized device or use --serial SERIAL.")
    return devices[0]


def check_credentials():
    # Read values only in memory to redact failure traces. Gradle still reads
    # its own configuration; credentials never become command-line arguments.
    properties = {}
    config = ROOT / "qa-local.properties"
    if config.is_file():
        # java.util.Properties.load(InputStream) uses ISO-8859-1 and backslash
        # escapes, including continuation lines and Unicode escapes.
        pending = ""
        for line in config.read_text(encoding="iso-8859-1").splitlines():
            line = pending + line.lstrip() if pending else line.lstrip()
            if len(line) - len(line.rstrip("\\")) & 1:
                pending = line[:-1]
                continue
            pending = ""
            match = re.match(r"^(qaLogin|qaPassword)(?:\s*[:=]\s*|\s+)(.*)$", line)
            if match:
                value = re.sub(r"\\u([0-9a-fA-F]{4})",
                               lambda m: chr(int(m.group(1), 16)), match.group(2))
                properties[match.group(1)] = re.sub(
                    r"\\(.)", lambda m: {"t": "\t", "n": "\n", "r": "\r", "f": "\f"}.get(
                        m.group(1), m.group(1)), value)
    if not (os.environ.get("QA_LOGIN") or properties.get("qaLogin")) or not (
            os.environ.get("QA_PASSWORD") or properties.get("qaPassword")):
        raise RunError("Set QA_LOGIN and QA_PASSWORD, or qaLogin and qaPassword in qa-local.properties.")
    return tuple(sorted({value for value in (
        *properties.values(), os.environ.get("QA_LOGIN"), os.environ.get("QA_PASSWORD"))
        if value}, key=len, reverse=True))


def redact_text(text, secrets):
    for secret in secrets:
        # Plain text, XML entities, and escaped JSON failure attachments.
        variants = {secret, html.escape(secret), html.escape(secret, quote=False),
                    json.dumps(secret, ensure_ascii=True)[1:-1],
                    json.dumps(secret, ensure_ascii=False)[1:-1]}
        for value in sorted(variants, key=len, reverse=True):
            text = text.replace(value, "[REDACTED]")
    return text


def sanitize_report(data, suffix, secrets):
    if suffix.lower() not in {".json", ".txt", ".xml", ".log", ".properties", ".csv"}:
        return data
    text = data.decode("utf-8-sig", errors="replace")
    if suffix.lower() == ".json":
        try:
            def clean(value):
                if isinstance(value, str):
                    return redact_text(value, secrets)
                if isinstance(value, list):
                    return [clean(item) for item in value]
                if isinstance(value, dict):
                    # Keep actual result metadata and numeric timings intact.
                    return {key: (item if key == "status" else clean(item))
                            for key, item in value.items()}
                return value
            return (json.dumps(clean(json.loads(text)), ensure_ascii=False) + "\n").encode("utf-8")
        except (ValueError, TypeError):
            pass
    return redact_text(text, secrets).encode("utf-8")


def clear_previous_results(adb, serial):
    installed = adb_text(adb, "-s", serial, "shell", "pm", "path", PACKAGE, check=False).stdout.strip()
    if not installed:
        return
    # Both the package and deletion path are constants, never caller input.
    if PACKAGE != "ru.edu.qamid" or DEVICE_RESULTS != "files/allure-results":
        raise RunError("Unexpected report deletion target.")
    adb_text(adb, "-s", serial, "shell", "run-as", PACKAGE,
             "rm", "-rf", DEVICE_RESULTS)


def export_results(adb, serial, run_dir, elapsed, test_exit_code, secrets=()):
    result = subprocess.run(
        [adb, "-s", serial, "exec-out", "run-as", PACKAGE,
         "tar", "-C", "files", "-cf", "-", "allure-results"],
        capture_output=True, timeout=120)
    if result.returncode:
        raise RunError("No fresh Allure results could be exported; the existing ZIP was preserved.")
    with tempfile.TemporaryDirectory(prefix="allure-export-", dir=run_dir) as staging:
        staging = Path(staging)
        with tarfile.open(fileobj=io.BytesIO(result.stdout), mode="r:") as archive:
            for member in archive.getmembers():
                path = PurePosixPath(member.name)
                if (path.is_absolute() or not path.parts or
                        path.parts[0] != "allure-results" or ".." in path.parts or
                        "\\" in member.name or ":" in member.name or
                        not (member.isfile() or member.isdir())):
                    raise RunError("Unsafe entry in the device report archive; export stopped.")
                target = staging.joinpath(*path.parts)
                if member.isdir():
                    target.mkdir(parents=True, exist_ok=True)
                else:
                    target.parent.mkdir(parents=True, exist_ok=True)
                    with archive.extractfile(member) as source, target.open("wb") as output:
                        output.write(sanitize_report(source.read(), target.suffix, secrets))
        report_dir = staging / "allure-results"
        results = [json.loads(path.read_text(encoding="utf-8"))
                   for path in report_dir.glob("*-result.json")]
        if not results:
            raise RunError("The fresh report contains no test results; the existing ZIP was preserved.")
        counts = Counter(result.get("status", "unknown") for result in results)
        summary = {
            "created_utc": datetime.now(timezone.utc).isoformat(),
            "device_api": 36,
            "gradle_exit_code": test_exit_code,
            "run_wall_seconds": round(elapsed, 3),
            "total": len(results),
            "statuses": dict(counts),
            "tests": [{"name": result.get("name"), "status": result.get("status"),
                       "duration_ms": max(0, result.get("stop", 0) - result.get("start", 0))}
                      for result in results],
        }
        temporary_zip = staging / "allure-results.zip"
        with zipfile.ZipFile(temporary_zip, "w", zipfile.ZIP_DEFLATED) as archive:
            for path in sorted(report_dir.rglob("*")):
                if path.is_file():
                    archive.write(path, path.relative_to(staging).as_posix())
        temporary_zip.replace(ROOT / "allure-results.zip")
        (run_dir / "summary.json").write_text(
            json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print("Exported allure-results.zip; summary: build/qa-run/summary.json")
        print("Test results: " + ", ".join(f"{status}={count}" for status, count in sorted(counts.items())))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", help="ADB serial; optional when exactly one device is connected")
    args = parser.parse_args()
    secrets = check_credentials()
    adb = find_adb()
    serial = choose_device(adb, args.serial or os.environ.get("ANDROID_SERIAL"))
    api = adb_text(adb, "-s", serial, "shell", "getprop", "ro.build.version.sdk").stdout.strip()
    if api != "36":
        raise RunError("This suite must run on Android API 36.")
    wrapper = ROOT / ("gradlew.bat" if os.name == "nt" else "gradlew")
    if not wrapper.is_file():
        raise RunError("Gradle Wrapper is missing from the project root.")
    env = os.environ.copy()
    env["ANDROID_SERIAL"] = serial
    env.setdefault("ANDROID_HOME", str(Path(adb).parent.parent))
    run_dir = ROOT / "build" / "qa-run"
    run_dir.mkdir(parents=True, exist_ok=True)
    # Prevent a stale summary being mistaken for the result of this invocation.
    (run_dir / "summary.json").unlink(missing_ok=True)
    clear_previous_results(adb, serial)
    command = [str(wrapper), "connectedDebugAndroidTest", "--console=plain",
               "-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true",
               "-Pandroid.testInstrumentationRunnerArguments.package=ru.edu.qamid.diploma"]
    # subprocess uses Windows command-line quoting for the .bat wrapper path.
    start = time.monotonic()
    exit_code = 1
    print("Running UI tests on API 36. Gradle log: build/qa-run/gradle.log")
    try:
        with (run_dir / "gradle.log").open("wb") as log:
            with subprocess.Popen(command, cwd=ROOT, env=env, stdout=subprocess.PIPE,
                                  stderr=subprocess.STDOUT) as process:
                for line in process.stdout:
                    log.write(redact_text(line.decode("utf-8", errors="replace"), secrets).encode("utf-8"))
                    log.flush()
                exit_code = process.wait()
    finally:
        try:
            export_results(adb, serial, run_dir, time.monotonic() - start, exit_code, secrets)
        except (RunError, OSError, tarfile.TarError, ValueError, subprocess.TimeoutExpired) as error:
            print(f"Report export failed: {error}", file=sys.stderr)
            if exit_code == 0:
                exit_code = 1
    return exit_code


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (RunError, OSError, subprocess.TimeoutExpired) as error:
        print(f"UI test runner: {error}", file=sys.stderr)
        sys.exit(1)
