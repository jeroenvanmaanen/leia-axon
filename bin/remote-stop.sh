#!/usr/bin/env bash
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
source "${PROJECT}/etc/settings-local.sh"

ssh "${DEPLOY_USER}@${DEPLOY_HOST}" bash -c ": ; set -x ; cd '/opt/${PROJECT_NAME}/${STACK}' ; sudo docker-compose stop"
