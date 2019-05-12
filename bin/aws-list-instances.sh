#!/bin/bash

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

: ${SED_EXT:=-r}
declare -a FLAGS_INHERIT # Assigned in lib-init.sh
source "${BIN}/lib-init.sh"

: ${AWS_REGION='eu-west-1'}
source "${PROJECT}/etc/settings-local.sh"

"${BIN}/aws.sh" "${FLAGS_INHERIT[@]}" configure set region "${AWS_REGION}"

"${BIN}/aws.sh" ec2 describe-instances --query 'Reservations[*].Instances[*].{image:ImageId,id:InstanceId,host:PublicDnsName,state:State.Name} | []'
## "${BIN}/aws.sh" ec2 describe-instances \
##    | docker run -v "${HOME}/src/leia:${HOME}/src/leia" -w ~ -i --rm cfmanteiga/alpine-bash-curl-jq \
##    jq '[.Reservations[] | .OwnerId as $owner | .Instances[] | {id: .InstanceId, owner: $owner, host: .PublicDnsName, image: .ImageId}]'
