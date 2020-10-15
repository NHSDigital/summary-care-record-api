#!/usr/bin/env bash
set -e

TAG=${1:-uk.nhs/summary-care-record-api:0.0.1-SNAPSHOT}

pushd ../../service/ && ./gradlew --build-cache bootJar && popd

cp ../../service/build/libs/integration-adaptor-scr-0.0.1-SNAPSHOT.jar integration-adaptor-scr.jar

docker build -t $TAG .

rm integration-adaptor-scr.jar
