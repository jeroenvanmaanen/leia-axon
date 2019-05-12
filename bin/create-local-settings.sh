#!/bin/bash

set -e

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

declare -a FLAGS_INHERIT
NEEDS_SETUP='false'
source "${BIN}/lib-init.sh"

log "PROJECT=[${PROJECT}]"

find "${PROJECT}" \( -type d \( -name target -o -name tmp -o -name node_modules -o -name '.*' \) -prune -type f \) -o -type f -name '*-sample.*' -print0 \
    | xargs -0 -n 1 "${BIN}/create-local-setting.sh" "${FLAGS_INHERIT[@]}" "$@"
