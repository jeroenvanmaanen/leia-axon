#!/usr/bin/false

set -e

: ${BIN=}
: ${NEEDS_INIT:=true} # Script needs initialization of variables
: ${NEEDS_SETUP:=true} # Project needs to be setup.

declare -a FLAGS_INHERIT

if "${NEEDS_INIT}"
then
    NEEDS_INIT='false'
    if [[ -z "${BIN}" ]]
    then
        BIN="$(cd "$(dirname "$0")" ; pwd)"
    fi
    source "${BIN}/verbose.sh"

    SED_EXT=-r
    case $(uname) in
    Darwin*)
            SED_EXT=-E
    esac
    export SED_EXT

    if [[ ".$1" = '.--skip-setup' ]]
    then
        shift
        NEEDS_SETUP='false'
        FLAGS_INHERIT[${#FLAGS_INHERIT[@]}]='--skip-setup'
    elif "${NEEDS_SETUP}"
    then
        "${BIN}/create-local-settings.sh" "${FLAGS_INHERIT[@]}"
        FLAGS_INHERIT[${#FLAGS_INHERIT[@]}]='--skip-setup'
    fi
fi
