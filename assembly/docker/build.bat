@echo off
setlocal enabledelayedexpansion

REM build.bat
REM Build script for the Docker image on Windows.

REM --- Script Setup ---
set "dockerDir=%~dp0"

REM --- Main Logic ---
echo "--- Starting Docker image build for usoft-bi ---"

REM Navigate to the docker directory.
cd /d "%dockerDir%"

REM Build the Docker image using docker-compose.
docker-compose build

IF ERRORLEVEL 1 (
    ECHO --- Docker image build failed ---
    EXIT /B 1
) ELSE (
    ECHO --- Docker image build completed successfully ---
    ECHO You can now run the application using: docker-compose up
)

endlocal
