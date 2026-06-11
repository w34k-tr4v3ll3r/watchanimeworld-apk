#!/usr/bin/env sh
# gradlew stub for CI
 BASEDIR=$(cd "$(dirname "$0")" && pwd)
 if [ -f "$BASEDIR/gradle/wrapper/gradle-wrapper.jar" ]; then
   java -jar "$BASEDIR/gradle/wrapper/gradle-wrapper.jar" "$@"
 else
   echo "gradle-wrapper.jar is missing. Please add the Gradle wrapper to the repo."
   exit 2
 fi
