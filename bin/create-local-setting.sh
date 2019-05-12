#!/bin/bash

BIN="$(cd "$(dirname "$0")" ; pwd)"

: ${SED_EXT:=-r}
NEEDS_SETUP='false'
source "${BIN}/lib-init.sh"

SAMPLE="$1"
LOCAL="$(echo "${SAMPLE}" | sed "${SED_EXT}" -e 's/-sample([+][^.]*)?/-local/')"

if [[ -e "${LOCAL}" ]]
then
    log "Skip: ${LOCAL}"
else
    info "Create: [${LOCAL}]"
    cp "${SAMPLE}" "${LOCAL}"
fi
