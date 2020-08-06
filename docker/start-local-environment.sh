#!/usr/bin/env bash
set -e

pushd docker && ./build-image.sh
popd

docker-compose down
docker-compose up