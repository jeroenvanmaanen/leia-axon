#!/bin/bash

set -e

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"
PRESENT="${PROJECT}/present"

: ${SILENT:=true}
: ${SED_EXT:=-r}
source "${BIN}/lib-init.sh"

: ${STACK:=STACK}
: ${MONGO_SERVER_PORT:=27017}
: ${EXTRA_VOLUMES:=}
source "${PROJECT}/etc/settings-local.sh"

DOCKER_REPOSITORY="${DOCKER_REPOSITORY:=}"
if [[ -z "${DOCKER_REPOSITORY}" ]]
then
    DOCKER_REPOSITORY="${STACK}"
fi

VOLUMES=''
if [[ -n "${EXTRA_VOLUMES}" ]]
then
    VOLUMES="
    volumes:${EXTRA_VOLUMES}"
fi

MONGO_PORTS=' Do not expose Mongo DB port'
PRESENT_SUFFIX=''
PRESENT_VOLUMES=' No volumes'
if [[ ".$1" = '.--dev' ]]
then
    shift
    MONGO_PORTS="
    ports:
    - \"127.0.0.1:${MONGO_SERVER_PORT}:27017\""
    PRESENT_SUFFIX='-dev'
    PRESENT_VOLUMES=" Mount local volume for development
    volumes:
    -
      type: bind
      source: ${PRESENT}
      target: ${PRESENT}
    working_dir: ${PRESENT}"
fi

COMPOSE="${PROJECT}/compose"

: ${QUALIFIER:=default}
if [[ -n "$1" ]]
then
    QUALIFIER="$1"
fi
SUFFIX=''
if [[ ".${QUALIFIER}" != '.default' ]]
then
    SUFFIX="-${QUALIFIER}"
    EXTRA_SETTINGS="${PROJECT}/etc/settings${SUFFIX}-local.sh"
    if [[ -f "${EXTRA_SETTINGS}" ]]
    then
        source "${EXTRA_SETTINGS}"
    fi
fi
STACK_NAME="leia${SUFFIX}"
TEMPLATE="${COMPOSE}/docker-compose${SUFFIX}-template.yml"
TARGET="${COMPOSE}/docker-compose${SUFFIX}-local.yml"

VARIABLES="$(tr '$\012' '\012$' < "${TEMPLATE}" | sed -e '/^[{][A-Za-z_][A-Za-z0-9_]*[}]/!d' -e 's/^[{]//' -e 's/[}].*//')"

function re-protect() {
    sed "${SED_EXT}" -e 's/([[]|[]]|[|*?^$()/])/\\\1/g' -e 's/$/\\/g' -e '$s/\\$//'
}

function substitute() {
    local VARIABLE="$1"
    local VALUE="$(eval "echo \"\${${VARIABLE}}\"" | re-protect)"
    log "VALUE=[${VALUE}]"
    if [[ -n "$(eval "echo \"\${${VARIABLE}+true}\"")" ]]
    then
        sed "${SED_EXT}" -e "s/[\$][{]${VARIABLE}[}]/${VALUE}/g" "${TARGET}" > "${TARGET}~"
        mv "${TARGET}~" "${TARGET}"
    fi
}

cp "${TEMPLATE}" "${TARGET}"
for VARIABLE in ${VARIABLES}
do
    log "VARIABLE=[${VARIABLE}]"
    substitute "${VARIABLE}"
done
"${SILENT}" || diff -u "${TEMPLATE}" "${TARGET}" || true

(
    cd "${COMPOSE}"
    docker-compose --project-name "${STACK_NAME}" --file "${TARGET}" up
)
