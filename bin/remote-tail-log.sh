#!/bin/bash

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

ssh "${DEPLOY_USER}@${DEPLOY_HOST}" bash -c ": ; set -x ; tail -n +0 -f '/opt/${PROJECT_NAME}/tmp/${STACK}.log'"
