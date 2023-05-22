#!/bin/bash

# See https://github.com/broadinstitute/gatk/pull/5056/files

# This script's purpose is for use with jitpack.io - a repository to publish snapshot automatically
# This script downloads git-lfs and pull needed sources to build GATK in the jitpack environment

# git lfs: doesn't seem to work

# GIT_LFS_VERSION="2.12.0"
# GIT_LFS_LINK=https://github.com/git-lfs/git-lfs/releases/download/v${GIT_LFS_VERSION}/git-lfs-linux-amd64-v${GIT_LFS_VERSION}.tar.gz
#            # https://github.com/git-lfs/git-lfs/releases/download/v2.12.0/git-lfs-linux-amd64-v2.12.0.tar.gz

# echo "Downloading and untarring git-lfs binary"
# wget -qO- $GIT_LFS_LINK | tar xvz git-lfs

# echo "Installing git-lfs"
# PATH+=:$(pwd)
# git lfs install

# echo "Fetching LFS files."
# git lfs pull --include distribution/lib

# wget

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