#!/usr/bin/env bash

## ~/src/leia/bin/clobber-build-run-and-import.sh -v --tee ~/src/leia/tmp/leia.log --skip-build --dev

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

declare -a FLAGS_INHERIT
source "${BIN}/lib-init.sh"

test -x "${BIN}/create-local-settings.sh" && "${BIN}/create-local-settings.sh"

: ${STACK:=STACK}
: ${AXON_SERVER_PORT=8024}
: ${API_SERVER_PORT=8080}
source "${PROJECT}/etc/settings-local.sh"

if [[ ".$1" = '.--help' ]]
then
    echo "Usage: $(basename "$0") [ -v [ -v ] ] [ --tee <file> ] [ --skip-build ] [ --dev ]" >&2
    echo "       $(basename "$0") --help" >&2
    exit 0
fi

if [[ ".$1" = '.--tee' ]]
then
    exec > >(tee "$2") 2>&1
    shift 2
fi

DO_BUILD='true'
if [[ ".$1" = '.--skip-build' ]]
then
  DO_BUILD='false'
  shift
fi

function waitForServerReady() {
    local URL="$1"
    local N="$2"
    if [[ -z "${N}" ]]
    then
        N=120
    fi
    while [[ "${N}" -gt 0 ]]
    do
        N=$[$N - 1]
        sleep 1
        if curl -sS "${URL}" >/dev/null 2>&1
        then
            break
        fi
    done
}

function countRunningContainers() {
    local HASH
    for HASH in $(docker-compose ps -q 2>/dev/null)
    do
        docker inspect -f '{{.State.Status}}' "${HASH}"
    done | grep -c running
}

function waitForDockerComposeReady() {
    (
        cd "${PROJECT}/${STACK}"
        while [[ "$(countRunningContainers)" -gt 0 ]]
        do
            sleep 0.5
        done
    )
}

sleep 5 # Wait for Axon Server to start

(
    cd "${PROJECT}"

    if "${DO_BUILD}"
    then
##        "${BIN}/swagger-yaml-to-json.sh"

        docker rm -f "${STACK}-axon-server" || true
        docker rm -f "${STACK}-mongodb" || true
        "${BIN}/docker-run-axon-server.sh"
        "${BIN}/docker-run-mongodb.sh"

        docker run --rm \
            --link leia-mongodb:leia-mongodb \
            --link leia-axon-server:leia-axon-server \
            -v '/var/run/docker.sock:/var/run/docker.sock' \
            -v "${HOME}/.m2:/root/.m2" \
            -v "${PROJECT}:${PROJECT}" -w "${PROJECT}" \
            jeroenvm/leia-build:0.2 ./mvnw -Djansi.force=true clean package

        docker stop "${STACK}-axon-server"
        docker stop "${STACK}-mongodb"
    fi

    docker-compose --project-name "${STACK}" --file "${PROJECT}/compose/docker-compose-local.yml" rm --stop --force
    docker volume rm -f "${STACK}_mongo-data"
    docker volume rm -f "${STACK}_axon-data"
    "${BIN}/docker-compose-up.sh" "${FLAGS_INHERIT[@]}" "$@" &
    PID_STACK="$!"
    trap "echo ; kill '${PID_STACK}' ; waitForDockerComposeReady" EXIT

    AXON_SERVER_URL="http://localhost:${AXON_SERVER_PORT}"
    waitForServerReady "${AXON_SERVER_URL}/actuator/health"
    STACK_API_URL="http://localhost:${API_SERVER_PORT}"
    waitForServerReady "${STACK_API_URL}/actuator/health"
    sleep 5

    cd "${PROJECT}/etc"
    echo 'Importing logging levels' >&2
    curl -sS -X POST "${STACK_API_URL}/api/admin/logger/all" -H "accept: application/json" -H "Content-Type: multipart/form-data" -F "file=@logging-levels-local.yaml"
    cd "${PROJECT}/data"
    echo 'Importing vocabularies' >&2
    curl -sS -X POST "${STACK_API_URL}/api/vocabulary-upload" -H "accept: application/json" -H "Content-Type: multipart/form-data" -F "file=@action-local.yaml"
    curl -sS -X POST "${STACK_API_URL}/api/vocabulary-upload" -H "accept: application/json" -H "Content-Type: multipart/form-data" -F "file=@response-local.yaml"
    curl -sS -X POST "${STACK_API_URL}/api/vocabulary-upload" -H "accept: application/json" -H "Content-Type: multipart/form-data" -F "file=@english-local.yaml"
    echo 'Importing interaction' >&2
    curl -sS -X POST "${STACK_API_URL}/api/interaction/upload" -H "accept: application/json" -H "Content-Type: multipart/form-data" -F "file=@interaction-local.txt"
    echo 'Imported all' >&2

    wait "${PID_STACK}"
)
