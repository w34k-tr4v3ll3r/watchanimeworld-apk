#!/usr/bin/env sh
set -euo pipefail

# Gradle wrapper launcher with corruption fallback.
# If gradle/wrapper/gradle-wrapper.jar is present and valid, run it.
# Otherwise download a Gradle distribution and run that.

BASEDIR=$(cd "$(dirname "$0")" && pwd)
JAR="$BASEDIR/gradle/wrapper/gradle-wrapper.jar"
GRADLE_VERSION="8.4.1"

# If wrapper JAR exists and looks like a valid zip/jar, run it.
if [ -f "$JAR" ]; then
  if command -v unzip >/dev/null 2>&1; then
    if unzip -t "$JAR" >/dev/null 2>&1; then
      exec java -jar "$JAR" "$@"
    else
      echo "gradle-wrapper.jar is invalid or corrupt; falling back to downloading Gradle ${GRADLE_VERSION}."
    fi
  else
    echo "unzip not available to validate gradle-wrapper.jar; attempting to run it anyway."
    exec java -jar "$JAR" "$@" || true
  fi
fi

# Fallback: download Gradle distribution and run it directly
TMP_ZIP="$BASEDIR/gradle-${GRADLE_VERSION}.zip"
INSTALL_DIR="/opt/gradle/gradle-${GRADLE_VERSION}"

if [ ! -x "$INSTALL_DIR/bin/gradle" ]; then
  echo "Downloading Gradle ${GRADLE_VERSION}..."
  curl -L -o "$TMP_ZIP" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  mkdir -p /opt/gradle
  unzip -q "$TMP_ZIP" -d /opt/gradle
  rm -f "$TMP_ZIP"
fi

exec "$INSTALL_DIR/bin/gradle" "$@"
