#!/bin/bash

set -e

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

PROJECT_NAME="$(cd "${PROJECT}" ; git remote get-url origin | sed -e 's:.*/::' -e 's/[.]git$//')"

declare -a FLAGS_INHERIT
source "${BIN}/lib-init.sh"

"${BIN}/create-local-settings.sh"

: ${DEPLOY_HOST:=localhost}
: ${DEPLOY_USER:=guest}
: ${STACK:=STACK}
source "${PROJECT}/etc/settings-local.sh"

SUFFIX=''
if [[ ".$1" = '.--qualifier' ]]
then
    SUFFIX="-$2"
    shift 2
    EXTRA_SETTINGS="${PROJECT}/etc/settings${SUFFIX}-local.sh"
    if [[ -f "${EXTRA_SETTINGS}" ]]
    then
        source "${EXTRA_SETTINGS}"
    fi
fi
STACK_NAME="${STACK}${SUFFIX}"

ssh "${DEPLOY_USER}@${DEPLOY_HOST}" bash -c ": ; set -x ; sudo docker-compose --file '/opt/${PROJECT_NAME}/compose/docker-compose${SUFFIX}-local.yml' --project-name '${STACK_NAME}' logs" > /dev/null 2>&1

sleep 1

ssh "${DEPLOY_USER}@${DEPLOY_HOST}" bash -c ": ; set -x ; sudo docker-compose --file '/opt/${PROJECT_NAME}/compose/docker-compose${SUFFIX}-local.yml' --project-name '${STACK_NAME}' logs --follow"
