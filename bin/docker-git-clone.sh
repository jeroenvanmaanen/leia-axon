#!/usr/bin/env bash

docker run -v "${PWD}:${PWD}" -w "${PWD}" alpine/git clone https://github.com/jeroenvanmaanen/leia-axon.git leia
