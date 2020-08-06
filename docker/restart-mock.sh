#!/usr/bin/env bash
set -e

pushd docker/mock && ./build-mock-image.sh
popd

docker-compose up -d --no-deps mock-spine-service
