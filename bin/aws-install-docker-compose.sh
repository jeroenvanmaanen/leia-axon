#!/bin/bash

set -e

yum update -y
yum install -y docker
curl -L -sS -D - https://github.com/docker/compose/releases/download/1.21.2/docker-compose-Linux-x86_64 -o /usr/bin/docker-compose
chmod a+x /usr/bin/docker-compose

service docker start
docker swarm init
