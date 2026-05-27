#!/usr/bin/env bash

# build.sh
# Build script for the Docker image.

# --- Script Setup ---
set -euo pipefail

# The directory of this script.
dockerDir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

# --- Main Logic ---
echo "--- Starting Docker image build for usoft-bi ---"

# Navigate to the docker directory to ensure docker-compose works as expected.
cd "$dockerDir"

# Build the Docker image using docker-compose.
# The context is set in the docker-compose.yaml file to be the project root.
docker-compose build

if [[ $? -eq 0 ]]; then
    echo "--- Docker image build completed successfully ---"
    echo "You can now run the application using: docker-compose up"
else
    echo "--- Docker image build failed ---" >&2
    exit 1
fi
