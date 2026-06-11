#!/usr/bin/env bash
set -euo pipefail

# Gradle wrapper launcher with corruption fallback and additional apt fallback.
# Behaviour:
# 1) If gradle/wrapper/gradle-wrapper.jar exists and validates as a zip, run it.
# 2) If invalid or missing, try downloading the Gradle distribution and run that.
# 3) If download/unzip fails (network or integrity), install system Gradle via apt and run it.

BASEDIR=$(cd "$(dirname "$0")" && pwd)
JAR="$BASEDIR/gradle/wrapper/gradle-wrapper.jar"
GRADLE_VERSION="8.4.1"

run_wrapper() {
  exec java -jar "$JAR" "$@"
}

run_downloaded_gradle() {
  TMP_ZIP="$BASEDIR/gradle-${GRADLE_VERSION}.zip"
  INSTALL_DIR="/opt/gradle/gradle-${GRADLE_VERSION}"

  echo "Downloading Gradle ${GRADLE_VERSION}..."
  if ! curl -fL -o "$TMP_ZIP" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"; then
    echo "Gradle download failed (curl)."
    return 1
  fi

  mkdir -p /opt/gradle
  if ! unzip -q "$TMP_ZIP" -d /opt/gradle; then
    echo "Unzip failed for downloaded Gradle distribution."
    rm -f "$TMP_ZIP"
    return 1
  fi
  rm -f "$TMP_ZIP"

  exec "$INSTALL_DIR/bin/gradle" "$@"
}

run_apt_gradle() {
  echo "Attempting to install Gradle via apt..."
  sudo apt-get update -qq || true
  if sudo apt-get install -y gradle; then
    exec gradle "$@"
  else
    echo "apt-get install gradle failed."
    return 1
  fi
}

# 1) If wrapper JAR is present, validate and run
if [ -f "$JAR" ]; then
  if command -v unzip >/dev/null 2>&1; then
    if unzip -t "$JAR" >/dev/null 2>&1; then
      run_wrapper "$@"
    else
      echo "gradle-wrapper.jar is invalid or corrupt; attempting fallback methods."
    fi
  else
    echo "unzip not available to validate gradle-wrapper.jar; attempting to run it anyway."
    if run_wrapper "$@"; then
      exit 0
    else
      echo "Running wrapper failed; attempting fallback methods."
    fi
  fi
fi

# 2) Try downloading Gradle and running it
if run_downloaded_gradle "$@"; then
  exit 0
fi

# 3) Final fallback: apt install gradle
if run_apt_gradle "$@"; then
  exit 0
fi

echo "All Gradle bootstrap methods failed. Please ensure the wrapper or network access is available."
exit 2
