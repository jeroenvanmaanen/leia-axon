#!/usr/bin/env bash

set -e

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

PROJECT_NAME="$(cd "${PROJECT}" ; git remote get-url origin | sed -e 's:.*/::' -e 's/[.]git$//')"

declare -a FLAGS_INHERIT
source "${BIN}/verbose.sh"

"${BIN}/create-local-settings.sh"

: ${DEPLOY_HOST:=localhost}
: ${DEPLOY_USER:=guest}
: ${STACK:=STACK}
source "${BIN}/settings-local.sh"

ssh "${DEPLOY_USER}@${DEPLOY_HOST}" bash -c ": ; cd '/opt/${PROJECT_NAME}/${STACK}' ; sudo docker-compose rm -f --stop ; sudo docker-compose pull" || true

declare -a DEPLOY_FLAGS
if [[ ".$1" = '.--clean' ]]
then
    ssh "${DEPLOY_USER}@${DEPLOY_HOST}" sudo rm -rf "/opt/${PROJECT_NAME}"
    DEPLOY_FLAGS[${#DEPLOY_FLAGS[@]}]='--clean'
    shift
fi

ssh "${DEPLOY_USER}@${DEPLOY_HOST}" bash -c ": ; set -x ; sudo mkdir -p '/opt/${PROJECT_NAME}' ; sudo chown '${DEPLOY_USER}' '/opt/${PROJECT_NAME}'"
(
    cd "${PROJECT}"
    rsync -r 'bin' 'data' 'leia' "${DEPLOY_USER}@${DEPLOY_HOST}:/opt/${PROJECT_NAME}"
)
ssh "${DEPLOY_USER}@${DEPLOY_HOST}" bash -c ": ; set -x ; chmod a+x /opt/${PROJECT_NAME}/bin/remote-deploy-delegate.sh ; sudo /opt/${PROJECT_NAME}/bin/remote-deploy-delegate.sh ${FLAGS_INHERIT[@]} ${DEPLOY_FLAGS[@]} '${PROJECT_NAME}'"

sleep 2
"${BIN}/remote-tail-log.sh"
