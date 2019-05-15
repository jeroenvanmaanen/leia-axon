#!/usr/bin/env bash

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

PROJECT_NAME="$(cd "${PROJECT}" ; git remote get-url origin | sed -e 's:.*/::' -e 's/[.]git$//')"

declare -a FLAGS_INHERIT
source "${BIN}/lib-init.sh"

: ${DEPLOY_HOST:=localhost}
: ${DEPLOY_USER:=guest}
: ${STACK:=STACK}
source "${PROJECT}/etc/settings-local.sh"

CLEAN='false'
declare -a DEPLOY_FLAGS
if [[ ".$1" = '.--clean' ]]
then
    CLEAN='true'
    shift
fi

QUALIFIER_FLAG=''
if [[ -n "$1" ]]
then
    QUALIFIER_FLAG="--qualifier '$1'"
    EXTRA_SETTINGS="${PROJECT}/etc/settings-$1-local.sh"
    shift
    if [[ -f "${EXTRA_SETTINGS}" ]]
    then
        source "${EXTRA_SETTINGS}"
    fi
fi

log "DEPLOY_HOST=[${DEPLOY_HOST}]"

if "${CLEAN}"
then
    ssh "${DEPLOY_USER}@${DEPLOY_HOST}" sudo rm -rf "/opt/${PROJECT_NAME}"
    DEPLOY_FLAGS[${#DEPLOY_FLAGS[@]}]='--clean'
fi

ssh "${DEPLOY_USER}@${DEPLOY_HOST}" bash -c ": ; set -x ; sudo mkdir -p '/opt/${PROJECT_NAME}' ; sudo chown '${DEPLOY_USER}' '/opt/${PROJECT_NAME}'"
(
    cd "${PROJECT}"
    rsync -r 'bin' 'etc' 'data' 'compose' "${DEPLOY_USER}@${DEPLOY_HOST}:/opt/${PROJECT_NAME}"
)

ssh "${DEPLOY_USER}@${DEPLOY_HOST}" bash -c ": ; set -x ; chmod a+x /opt/${PROJECT_NAME}/bin/remote-deploy-delegate.sh ; sudo /opt/${PROJECT_NAME}/bin/remote-deploy-delegate.sh ${FLAGS_INHERIT[@]} ${DEPLOY_FLAGS[@]} '${PROJECT_NAME}' ${QUALIFIER_FLAG}"

sleep 2
"${BIN}/remote-tail-log.sh" ${QUALIFIER_FLAG}
