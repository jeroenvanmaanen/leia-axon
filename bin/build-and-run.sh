#!/usr/bin/env bash

set -e

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

source "${BIN}/lib-init.sh"

: ${STACK:=STACK}
source "${PROJECT}/etc/settings-local.sh"

(
    cd "${PROJECT}"
    ./mvnw clean package
    java -jar "core/target/${STACK}-core-0.0.1-SNAPSHOT.jar"
)
