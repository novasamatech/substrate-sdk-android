#!/bin/bash

echo "Downloading libs into distribution directory"

RELEASE=2.0
NDK_VERSION=21.1.6352462
API=22
TAG=0.8.0

echo "Installing NDK ${NDK_VERSION}"
yes | $ANDROID_HOME/tools/bin/sdkmanager --install "ndk;${NDK_VERSION}"
# sdkmanager --install "ndk;${NDK_VERSION}"

export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/${NDK_VERSION}
export NDK_HOME=$ANDROID_HOME/ndk/${NDK_VERSION}