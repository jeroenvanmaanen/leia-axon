#!/usr/bin/env bash

set -e

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

: ${STACK:=STACK}
source "${BIN}/settings-local.sh"

(
    cd "${PROJECT}/core"

    docker rm -f "${STACK}-axon-server" || true
    docker rm -f "${STACK}-mongodb" || true
    "${BIN}/docker-run-axon-server.sh"
    "${BIN}/docker-run-mongodb.sh"

    ../mvnw -Djansi.force=true clean package

    docker stop "${STACK}-axon-server"
    docker stop "${STACK}-mongodb"
)
