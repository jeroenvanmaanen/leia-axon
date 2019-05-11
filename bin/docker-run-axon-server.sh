#!/bin/bash

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

: ${STACK:=STACK}
source "${PROJECT}/etc/settings-local.sh"

docker run -d --name "${STACK}-axon-server" --expose 8084 -p 8024:8024 --expose 8124 -p 8124:8124 --hostname axonserver -e AXONSERVER_HOSTNAME=axonserver axoniq/axonserver
