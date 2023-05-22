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
NDK=21.1.6352462
API=22
TAG=0.8.0

ARCH_ARM64=arm64-v8a
ARCH_X86_64=x86_64
ARCH_X86=x86

LIB_NSYNC=libnsync.a
LIB_PROTO=libprotobuf.so
LIB_TENSORFLOW=libtensorflow-core.a

LIB_DIR=distribution/tensorflow/lib

ARM64_LIB_NSYNC_URL=https://storage.googleapis.com/tensorio-build/android/release/${RELEASE}/ndk/${NDK}/api/${API}/tag/${TAG}/arch/${ARCH_ARM64}/${LIB_NSYNC}
ARM64_LIB_PROTO_URL=https://storage.googleapis.com/tensorio-build/android/release/${RELEASE}/ndk/${NDK}/api/${API}/tag/${TAG}/arch/${ARCH_ARM64}/${LIB_PROTO}
ARM64_LIB_TENSORFLOW_URL=https://storage.googleapis.com/tensorio-build/android/release/${RELEASE}/ndk/${NDK}/api/${API}/tag/${TAG}/arch/${ARCH_ARM64}/${LIB_TENSORFLOW}

X86_64_LIB_NSYNC_URL=https://storage.googleapis.com/tensorio-build/android/release/${RELEASE}/ndk/${NDK}/api/${API}/tag/${TAG}/arch/${ARCH_X86_64}/${LIB_NSYNC}
X86_64_LIB_PROTO_URL=https://storage.googleapis.com/tensorio-build/android/release/${RELEASE}/ndk/${NDK}/api/${API}/tag/${TAG}/arch/${ARCH_X86_64}/${LIB_PROTO}
X86_64_LIB_TENSORFLOW_URL=https://storage.googleapis.com/tensorio-build/android/release/${RELEASE}/ndk/${NDK}/api/${API}/tag/${TAG}/arch/${ARCH_X86_64}/${LIB_TENSORFLOW}

X86_LIB_NSYNC_URL=https://storage.googleapis.com/tensorio-build/android/release/${RELEASE}/ndk/${NDK}/api/${API}/tag/${TAG}/arch/${ARCH_X86}/${LIB_NSYNC}
X86_LIB_PROTO_URL=https://storage.googleapis.com/tensorio-build/android/release/${RELEASE}/ndk/${NDK}/api/${API}/tag/${TAG}/arch/${ARCH_X86}/${LIB_PROTO}
X86_LIB_TENSORFLOW_URL=https://storage.googleapis.com/tensorio-build/android/release/${RELEASE}/ndk/${NDK}/api/${API}/tag/${TAG}/arch/${ARCH_X86}/${LIB_TENSORFLOW}

echo "Downloading arm64 libs"

wget ${ARM64_LIB_NSYNC_URL} -O ${LIB_DIR}/${ARCH_ARM64}/${LIB_NSYNC}
wget ${ARM64_LIB_PROTO_URL} -O ${LIB_DIR}/${ARCH_ARM64}/${LIB_PROTO}
wget ${ARM64_LIB_TENSORFLOW_URL} -O ${LIB_DIR}/${ARCH_ARM64}/${LIB_TENSORFLOW}

echo "Downloading x86_64 libs"

wget ${X86_64_LIB_NSYNC_URL} -O ${LIB_DIR}/${ARCH_X86_64}/${LIB_NSYNC}
wget ${X86_64_LIB_PROTO_URL} -O ${LIB_DIR}/${ARCH_X86_64}/${LIB_PROTO}
wget ${X86_64_LIB_TENSORFLOW_URL} -O ${LIB_DIR}/${ARCH_X86_64}/${LIB_TENSORFLOW}

echo "Downloading x86 libs"

wget ${X86_LIB_NSYNC_URL} -O ${LIB_DIR}/${ARCH_X86}/${LIB_NSYNC}
wget ${X86_LIB_PROTO_URL} -O ${LIB_DIR}/${ARCH_X86}/${LIB_PROTO}
wget ${X86_LIB_TENSORFLOW_URL} -O ${LIB_DIR}/${ARCH_X86}/${LIB_TENSORFLOW}
