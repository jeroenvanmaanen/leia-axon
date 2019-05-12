#!/usr/bin/env bash

BIN="$(cd "$(dirname "$0")" ; pwd)"

declare -a FLAGS_INHERIT
source "${BIN}/lib-init.sh"

"${BIN}/aws.sh" "${FLAGS_INHERIT[@]}" ec2 describe-spot-instance-requests --query 'SpotInstanceRequests[*].{ID:InstanceId}'
