#!/bin/bash

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

source "${BIN}/verbose.sh"

: ${STACK:=STACK}
source "${PROJECT}/etc/settings-local.sh"

SUFFIX=''
if [[ -n "$1" ]]
then
    SUFFIX="$1"
    shift
else
    SUFFIX="$(date '+%Y%m%dT%H%M')"
fi
info "SUFFIX=[${SUFFIX}]"

docker run --rm -ti \
    -v "${STACK}_axon-data:/opt/axonframework/data" \
    -v "${PROJECT}:${PROJECT}" -w '/opt/axonframework/data' \
    alpine:latest /bin/sh -c "tar -cvzf - . > '${PROJECT}/data/backup-${SUFFIX}.tar.gz'"
