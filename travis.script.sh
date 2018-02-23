#!/bin/bash

#exit script on any error
set -e

#TODO: Update this build file to support CRON jobs.
./gradlew -Pversion=$TRAVIS_TAG clean build shadowJar

if [ -n "$TRAVIS_TAG" ]; then
    echo "Travis Tag is Set, Sending to Docker"

    # Conditionally tag git and build docker image.
    # The username and password are configured in the travis gui
    docker login -u="$DOCKER_USERNAME" -p="$DOCKER_PASSWORD"

    ANNOTATIONS_SERVICE_TAG="gchq/stroom-annotations-service:${TRAVIS_TAG}"
    echo "Building stroom-annotations-service with tag ${ANNOTATIONS_SERVICE_TAG}"
    docker build --tag=${ANNOTATIONS_SERVICE_TAG} stroom-annotations-svc/.
    echo "Pushing ${ANNOTATIONS_SERVICE_TAG}"
    docker push ${ANNOTATIONS_SERVICE_TAG}

    ANNOTATIONS_UI_TAG="gchq/stroom-annotations-ui:${TRAVIS_TAG}"
    echo "Building stroom-annotations-ui with tag ${ANNOTATIONS_UI_TAG}"
    ./stroom-annotations-ui/docker/build.sh ${TRAVIS_TAG}
    echo "Pushing ${ANNOTATIONS_UI_TAG}"
    docker push ${ANNOTATIONS_UI_TAG}
else
    echo "Skipping Docker Push - No Tag Set"
fi