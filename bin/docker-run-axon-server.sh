#!/bin/bash

BIN="$(cd "$(dirname "$0")" ; pwd)"

: ${STACK:=STACK}
source "${BIN}/settings-local.sh"

docker run -d --name "${STACK}-axon-server" -p 8024:8024 -p 8124:8124 --hostname axonserver -e AXONSERVER_HOSTNAME=axonserver axoniq/axonserver
