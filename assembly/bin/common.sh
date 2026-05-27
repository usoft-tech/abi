#!/usr/bin/env bash

# common.sh
# This script sets up common environment variables for build scripts.
# It is intended to be sourced by other scripts, not executed directly.

# --- Script Setup ---
# Stop on any error, treat unset variables as an error.
set -euo pipefail

# --- Path Definitions ---

# The absolute path to the directory containing this script (e.g., .../assembly/bin).
sbinDir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

# The absolute path to the 'assembly' directory.
baseDir=$(cd "$sbinDir/.." && pwd -P)

# The absolute path to the root directory of the entire project.
projectDir=$(cd "$baseDir/.." && pwd -P)

# Other common directories.
runtimeDir="$baseDir/runtime"
buildDir="$baseDir/build"

# --- Constants ---

# The name for the standalone application.
readonly STANDALONE_APP_NAME="ubi_standalone"

# The service name for the standalone build, used as a default in build.sh.
readonly standalone_service="standalone"
