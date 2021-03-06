#!/usr/bin/env bash

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

declare -a FLAGS_INHERIT
: ${SILENT=true}
source "${BIN}/lib-init.sh"

: ${STACK:=STACK}
source "${PROJECT}/etc/settings-local.sh"

log "BIN=[${BIN}]"

CLEAN='false'
if [[ ".$1" = '.--clean' ]]
then
    CLEAN='true'
    shift
fi

PROJECT_NAME="$1" ; shift
log "PROJECT_NAME=[${PROJECT_NAME}]"

if type docker-compose > /dev/null 2>&1
then
    :
else
    chmod a+x "${BIN}/aws-install-docker-compose.sh"
    "${BIN}/aws-install-docker-compose.sh"
fi

mkdir -p "${PROJECT}/tmp"

"${SILENT}" || find "${PROJECT}" -ls

## echo nohup "${PROJECT}/bin/clobber-build-run-and-import.sh" -v --tee "${PROJECT}/tmp/${STACK}.log" --skip-build
## nohup "${PROJECT}/bin/clobber-build-run-and-import.sh" -v --tee "${PROJECT}/tmp/${STACK}.log" --skip-build >/dev/null 2>&1 &
echo nohup "${PROJECT}/bin/docker-compose-up.sh" ${FLAGS_INHERIT[@]} --rm "$@" '--detach' "> ${PROJECT}/tmp/${STACK}.log" '2>&1 &'
nohup      "${PROJECT}/bin/docker-compose-up.sh" ${FLAGS_INHERIT[@]} --rm "$@" '--detach' > "${PROJECT}/tmp/${STACK}.log"  2>&1 &

sleep 2

ls -ld "${PROJECT}/tmp/${STACK}.log"