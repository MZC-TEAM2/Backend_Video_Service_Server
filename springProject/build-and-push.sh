#!/bin/bash

# Multi-architecture Docker build and push script
# Builds for both AMD64 and ARM64 platforms

set -e

# Configuration
IMAGE_NAME="ddingsh9/mzc-video-server"
TAG="latest"
FULL_IMAGE="${IMAGE_NAME}:${TAG}"
PLATFORMS="linux/amd64,linux/arm64"

echo "=========================================="
echo "Multi-Architecture Docker Build & Push"
echo "=========================================="
echo "Image: ${FULL_IMAGE}"
echo "Platforms: ${PLATFORMS}"
echo "=========================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker first."
    exit 1
fi

# Check if logged in to Docker Hub
if ! docker info 2>/dev/null | grep -q "Username"; then
    echo "Warning: Not logged in to Docker Hub."
    echo "Please run 'docker login' first."
    read -p "Do you want to login now? (y/n): " choice
    if [ "$choice" = "y" ] || [ "$choice" = "Y" ]; then
        docker login
    else
        echo "Aborting. Please login and try again."
        exit 1
    fi
fi

# Create and use buildx builder if not exists
BUILDER_NAME="multiarch-builder"
if ! docker buildx ls | grep -q "${BUILDER_NAME}"; then
    echo "Creating buildx builder: ${BUILDER_NAME}"
    docker buildx create --name ${BUILDER_NAME} --use --bootstrap
else
    echo "Using existing builder: ${BUILDER_NAME}"
    docker buildx use ${BUILDER_NAME}
fi

# Build and push multi-architecture image
echo ""
echo "Building and pushing multi-architecture image..."
echo ""

docker buildx build \
    --platform ${PLATFORMS} \
    --tag ${FULL_IMAGE} \
    --push \
    .

echo ""
echo "=========================================="
echo "Build and push completed successfully!"
echo "=========================================="
echo ""
echo "Image available at: ${FULL_IMAGE}"
echo "Supported platforms: ${PLATFORMS}"
echo ""
echo "Pull command:"
echo "  docker pull ${FULL_IMAGE}"
echo ""
