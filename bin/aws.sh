#!/bin/bash

set -e

BIN="$(cd "$(dirname "$0")" ; pwd)"

source "${BIN}/lib-init.sh"

mkdir -p "${HOME}/.aws"

CWD="$(pwd)"

docker run --rm -ti -v "${HOME}/.aws:/home/aws/.aws" -v "${CWD}:${CWD}" -w "${CWD}" 'fstab/aws-cli' /home/aws/aws/env/bin/aws "$@"
