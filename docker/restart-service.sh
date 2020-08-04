#!/usr/bin/env bash
set -e

pushd docker && ./build-image.sh
popd

docker-compose up -d --no-deps integration-adaptor-scr