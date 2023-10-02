#!/bin/bash

# Set the versions
SDK_VERSION="commandlinetools-linux-7583922_latest.zip"
BUILD_TOOLS_VERSION="28.0.3"
NDK_VERSION="r21d"

# Set the download URLs
SDK_URL="https://dl.google.com/android/repository/$SDK_VERSION"
BUILD_TOOLS_URL="https://dl.google.com/android/repository/build-tools_r$BUILD_TOOLS_VERSION-linux.zip"
NDK_URL="http://dl.google.com/android/repository/android-ndk-${NDK_VERSION}-linux-x86_64.zip"


# Set the installation directories
INSTALL_DIR="$HOME/Android"
SDK_DIR="$INSTALL_DIR/sdk"
BUILD_TOOLS_DIR="$SDK_DIR/build-tools/$BUILD_TOOLS_VERSION"
NDK_DIR="$INSTALL_DIR/ndk/$NDK_VERSION"

# Create the installation directories
mkdir -p "$SDK_DIR"
mkdir -p "$BUILD_TOOLS_DIR"
mkdir -p "$NDK_DIR"

# Download and extract the SDK
wget -O "$SDK_DIR/$SDK_VERSION" "$SDK_URL"
unzip -d "$SDK_DIR" "$SDK_DIR/$SDK_VERSION"
rm "$SDK_DIR/$SDK_VERSION"

# Download and extract the build tools
wget -O "$BUILD_TOOLS_DIR.zip" "$BUILD_TOOLS_URL"
unzip -d "$BUILD_TOOLS_DIR" "$BUILD_TOOLS_DIR.zip"
rm "$BUILD_TOOLS_DIR.zip"

# Download and extract the NDK
wget -O "$NDK_DIR.zip" "$NDK_URL"
unzip -d "$NDK_DIR" "$NDK_DIR.zip"
rm "$NDK_DIR.zip"

# Add the bin directories to the PATH
echo 'export ANDROID_HOME=$HOME/Android/sdk' >> "$GITHUB_ENV"
echo 'export ANDROID_NDK_HOME=$HOME/Android/ndk/$NDK_VERSION' >> "$GITHUB_ENV"
echo 'export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$ANDROID_NDK_HOME' >> "$GITHUB_ENV"
