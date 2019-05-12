#!/bin/bash

set -e -x

BIN="$(cd "$(dirname "$0")" ; pwd)"
PROJECT="$(dirname "${BIN}")"

SPOT_PRICE=0.1
if [[ ".$1" = '.--spot-price' ]]
then
    SPOT_PRICE="$2" ; shift 2
fi

QUALIFIER="$1"
SUFFIX=''
if test -n "${QUALIFIER}"
then
    SUFFIX="+${QUALIFIER}"
fi

aws.sh ec2 request-spot-instances --spot-price "${SPOT_PRICE}" --launch-specification "$(cat "${PROJECT}/aws/launch-specification${SUFFIX}-local.json")"
