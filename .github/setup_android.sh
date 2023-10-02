#!/bin/bash

# Set the versions
ANDROID_HOME="$HOME/Android"
ANDROID_SDK_TOOLS_VERSION="4333796"
ANDROID_NDK_VERSION="r21d"
ANDROID_SDK_HOME="$ANDROID_HOME"
ANDROID_NDK_HOME="$ANDROID_NDK/android-ndk-$ANDROID_NDK_VERSION"

# Download and install Android SDK Tools
wget  --output-document=sdk-tools.zip \
    "https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_TOOLS_VERSION}.zip"
mkdir --parents "$ANDROID_HOME"
unzip -q sdk-tools.zip -d "$ANDROID_HOME"
rm --force sdk-tools.zip

# Download and install Android NDK
wget  --output-document=android-ndk.zip \
    "http://dl.google.com/android/repository/android-ndk-${ANDROID_NDK_VERSION}-linux-x86_64.zip"
mkdir --parents "$ANDROID_NDK_HOME"
unzip -q android-ndk.zip -d "$ANDROID_NDK"
rm --force android-ndk.zip

# Accept Android SDK licenses
mkdir --parents "$HOME/.android/"
echo '### User Sources for Android SDK Manager' > "$HOME/.android/repositories.cfg"

# Download and install Android platforms
wget --output-document=platforms.zip \
    "https://dl.google.com/android/repository/platforms;android-30"
unzip -q platforms.zip -d "$ANDROID_HOME"
rm --force platforms.zip

wget --output-document=platforms.zip \
    "https://dl.google.com/android/repository/platforms;android-29"
unzip -q platforms.zip -d "$ANDROID_HOME"
rm --force platforms.zip

# Download and install Android platform tools
wget --output-document=platform-tools.zip \
    "https://dl.google.com/android/repository/platform-tools"
unzip -q platform-tools.zip -d "$ANDROID_HOME"
rm --force platform-tools.zip

# Download and install Android build tools
wget --output-document=build-tools.zip \
    "https://dl.google.com/android/repository/build-tools;30.0.0"
unzip -q build-tools.zip -d "$ANDROID_HOME"
rm --force build-tools.zip

wget --output-document=build-tools.zip \
    "https://dl.google.com/android/repository/build-tools;29.0.3"
unzip -q build-tools.zip -d "$ANDROID_HOME"
rm --force build-tools.zip

wget --output-document=build-tools.zip \
    "https://dl.google.com/android/repository/build-tools;29.0.2"
unzip -q build-tools.zip -d "$ANDROID_HOME"
rm --force build-tools.zip

wget --output-document=build-tools.zip \
    "https://dl.google.com/android/repository/build-tools;28.0.3"
unzip -q build-tools.zip -d "$ANDROID_HOME"
rm --force build-tools.zip

wget --output-document=build-tools.zip \
    "https://dl.google.com/android/repository/build-tools;28.0.2"
unzip -q build-tools.zip -d "$ANDROID_HOME"
rm --force build-tools.zip

# Download and install Android emulator
wget --output-document=emulator.zip \
    "https://dl.google.com/android/repository/emulator"
unzip -q emulator.zip -d "$ANDROID_HOME"
rm --force emulator.zip

# Add the bin directories to the PATH
echo 'export ANDROID_HOME=$HOME/Android/sdk' >> ~/.bashrc
echo 'export ANDROID_NDK_HOME=$HOME/Android/ndk/$NDK_VERSION' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$ANDROID_NDK_HOME' >> ~/.bashrc

# Source the bashrc file
source ~/.bashrc