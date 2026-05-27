@echo off
REM This script sets up common environment variables for build scripts.

REM --- Path Definitions ---

REM The directory containing this script (sbin).
set "sbinDir=%~dp0"
REM Ensure sbinDir does not have a trailing backslash.
if "%sbinDir:~-1%"=="\" set "sbinDir=%sbinDir:~0,-1%"

REM The 'assembly' directory, which is the parent of 'sbin'.
for /f "delims=" %%A in ("%sbinDir%\..") do set "baseDir=%%~fA"

REM The root directory of the entire project.
for /f "delims=" %%A in ("%baseDir%\..") do set "projectDir=%%~fA"

REM Other common directories.
set "runtimeDir=%baseDir%\runtime"
set "buildDir=%baseDir%\build"

REM --- Constants ---

REM The name for the standalone application.
set "STANDALONE_APP_NAME=ubi_standalone"

REM The service name for the standalone build, used in build.bat.
set "standalone_service=standalone"
