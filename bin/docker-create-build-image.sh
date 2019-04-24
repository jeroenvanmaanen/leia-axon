#!/usr/bin/env bash

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

cd "${PROJECT}/build"

docker build -t jeroenvm/leia-build:0.2 .
