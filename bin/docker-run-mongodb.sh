#!/bin/bash

BIN="$(cd "$(dirname "$0")" ; pwd)"

: ${STACK:=STACK}
source "${BIN}/settings-local.sh"

docker run -d --name "${STACK}-mongodb" --expose 27017 -p 27717:27017 --hostname mongodb mongo:3.6
