#!/usr/bin/env bash

BIN="$(cd "$(dirname "$0")" ; pwd)"

source "${BIN}/verbose.sh"

F=20
if [[ ".$1" = '.--frequency' ]]
then
    F="$2"
    shift 2
fi

if [[ ".$1" = '.--' ]]
then
    shift
fi

(
    echo '!slow 200'
    cat "$@" \
        | sed \
            -e '/./s/$/ ./' \
        | tr ' ' '\012' \
        | sed -e '/./s/^/<en:/' \
        | awk '{print} NR % '"${F}"' == 0 { print "!markExtensible"; }'
)