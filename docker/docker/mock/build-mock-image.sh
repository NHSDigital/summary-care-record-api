#!/usr/bin/env bash
set -e

TAG=${1:-uk.nhs/spine-mock:0.0.1-SNAPSHOT}

pushd ../../mock-spine-service/ && ./gradlew --build-cache bootJar && popd

cp ../../mock-spine-service/build/libs/mock-spine-service.jar mock-spine-service.jar

docker build -t $TAG .

rm mock-spine-service.jar
