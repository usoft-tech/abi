#!/usr/bin/env bash

# build.sh
# Main build script for the project, compatible with Linux/macOS.

# --- Script Setup ---
# Stop on any error, treat unset variables as an error.
set -euo pipefail

# Source the common environment variables.
# The directory of this script.
sbinDir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
# shellcheck source=./common.sh
source "$sbinDir/common.sh"

# --- Argument Parsing ---
# Use the first argument as the service, or default to standalone_service from common.sh.
service=${1:-$standalone_service}

# --- Maven Version ---
# Get the project version from Maven and store it in MVN_VERSION.
echo "Retrieving Maven project version..."
MVN_VERSION=$(mvn -f "$projectDir" -q help:evaluate -Dexpression=project.version -DforceStdout)
if [[ -z "$MVN_VERSION" ]]; then
    echo "Error: Failed to retrieve Maven project version." >&2
    exit 1
fi
echo "Maven Project Version: $MVN_VERSION"

# --- Function Definitions ---

# buildJavaService: Builds the backend Java modules.
buildJavaService() {
    echo "--- Starting build of usoft-bi service ---"
    mvn -f "$projectDir" clean package -DskipTests -Dspotless.skip=true
    
    echo "Copying Java service artifacts..."
    # The name of the binary archive produced by maven-assembly-plugin.
    local launcher_archive="usoft-launcher-$MVN_VERSION-bin.tar.gz"
    
    # Copy the main archive to the build directory.
    cp "$projectDir/usoft-launcher/target/$launcher_archive" "$buildDir/"
    
    echo "--- Finished building usoft-bi service ---"
}

# buildWebapp: Builds the frontend webapp.
buildWebapp() {
    echo "--- Starting build of usoft-bi webapp ---"
    (
        cd "$projectDir/webapp"
        # Assuming start-bi-build.sh is the Linux equivalent of start-bi-build.bat
        bash ./start-bi-build.sh
    )
    
    # Copy the webapp archive to the build directory.
    cp "$projectDir/webapp/webapp.tar.gz" "$buildDir/"
    
    echo "--- Finished building usoft-bi webapp ---"
}

# packageRelease: Packages the final release archive.
packageRelease() {
    echo "--- Starting packaging of usoft-bi release ---"
    
    local release_dir="usoft-bi-$MVN_VERSION"
    local service_archive="usoft-launcher-$MVN_VERSION-bin.tar.gz"
    
    cd "$buildDir"
    
    # Cleanup previous builds.
    rm -rf "$release_dir" "${release_dir}.zip"
    
    # Create and unpack the Java service.
    mkdir "$release_dir"
    tar -xzf "$service_archive" -C "$release_dir" --strip-components=1
    
    # Unpack the webapp into the release directory.
    if [[ -f "webapp.tar.gz" ]]; then
        # Create a 'webapp' directory inside the release dir and unpack there.
        # mkdir -p "$release_dir/webapp"
        tar -xzf "webapp.tar.gz" --strip-components=1 && mv dist "$release_dir/webapp"
    fi
    
    # Verify deployment structure.
    if [[ -f "$release_dir/lib/usoft-launcher-$MVN_VERSION.jar" ]]; then
        echo "Deployment structure verified successfully."
    else
        echo "Warning: Main jar file not found in deployment structure." >&2
        echo "Expected: $release_dir/lib/usoft-launcher-$MVN_VERSION.jar" >&2
    fi
    
    # Generate the final zip file.
    zip -r "${release_dir}.zip" "$release_dir"
    if [[ $? -eq 0 ]]; then
        echo "Successfully created release package: ${release_dir}.zip"
    else
        echo "Warning: Failed to create zip package. The release directory is still available: $release_dir" >&2
    fi
    
    # Cleanup intermediate files.
    rm -f "$service_archive" "webapp.tar.gz"
    
    echo "--- Finished packaging usoft-bi release ---"
}

# --- Main Logic ---

main() {
    # Create the build directory if it doesn't exist.
    mkdir -p "$buildDir"

    cd "$baseDir"

    if [[ "$service" == "webapp" ]]; then
        buildWebapp
        # The logic to move webapp to target/classes is specific and might be for development.
        # Replicating it here.
        cd "$buildDir"
        tar -xzf webapp.tar.gz
        # Assuming the output of webapp build is a 'dist' directory.
        rm -rf "$projectDir/usoft-launcher/target/classes/webapp"
        mv dist "$projectDir/usoft-launcher/target/classes/webapp"
        echo "Webapp build complete and moved to target/classes."
    else
        buildJavaService
        buildWebapp
        packageRelease
        echo "Full build and packaging complete."
    fi
}

# Execute the main function.
main
