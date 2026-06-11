@echo off
setlocal
if exist "%~dp0\gradle\wrapper\gradle-wrapper.jar" (
  java -jar "%~dp0\gradle\wrapper\gradle-wrapper.jar" %*
) else (
  echo gradle-wrapper.jar is missing. Please add the Gradle wrapper to the repo.
  exit /b 2
)
