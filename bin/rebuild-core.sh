#!/usr/bin/env bash

set -e

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

: ${STACK:=STACK}
source "${PROJECT}/etc/settings-local.sh"

(
    cd "${PROJECT}/core"

    docker rm -f "${STACK}-axon-server" || true
    docker rm -f "${STACK}-mongodb" || true
    "${BIN}/docker-run-axon-server.sh"
    "${BIN}/docker-run-mongodb.sh"

    docker run --rm \
        --link leia-mongodb:leia-mongodb \
        --link leia-axon-server:leia-axon-server \
        -v '/var/run/docker.sock:/var/run/docker.sock' \
        -v "${HOME}/.m2:/root/.m2" \
        -v "${PROJECT}:${PROJECT}" -w "${PROJECT}/core" \
        jeroenvm/leia-build:0.2 ../mvnw -Djansi.force=true clean package

    docker stop "${STACK}-axon-server"
    docker stop "${STACK}-mongodb"
)
