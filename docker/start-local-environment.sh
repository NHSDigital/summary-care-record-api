#!/usr/bin/env bash
set -e

pushd docker/service && ./build-service-image.sh
popd

docker-compose down
docker-compose up
