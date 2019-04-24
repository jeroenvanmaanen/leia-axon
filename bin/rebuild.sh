#!/usr/bin/env bash

set -e

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

source "${BIN}/verbose.sh"

: ${STACK:=STACK}
source "${BIN}/settings-local.sh"

MODULE="$1" ; shift || true

(
    cd "${PROJECT}/${MODULE}"

    docker rm -f "${STACK}-axon-server" || true
    docker rm -f "${STACK}-mongodb" || true
    "${BIN}/docker-run-axon-server.sh"
    "${BIN}/docker-run-mongodb.sh"

    MVNW='../mvnw'
    if [[ -z "${MODULE}" ]]
    then
        MVNW='./mvnw'
    fi

    docker run --rm \
        --link leia-mongodb:leia-mongodb \
        --link leia-axon-server:leia-axon-server \
        -v '/var/run/docker.sock:/var/run/docker.sock' \
        -v "${HOME}/.m2:/root/.m2" \
        -v "${PROJECT}:${PROJECT}" -w "${PROJECT}/$MODULE" \
        jeroenvm/leia-build:0.2 "${MVNW}" -Djansi.force=true clean package

    docker stop "${STACK}-axon-server"
    docker stop "${STACK}-mongodb"
)
