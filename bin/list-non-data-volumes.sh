#!/usr/bin/env bash

docker volume ls | sed -e 1d -e 's/^.* //' | grep -v '[-]data'