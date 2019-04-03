#!/usr/bin/env bash

SED_EXT='-r'
case "$(uname)" in
Darwin*)
        SED_EXT='-E'
esac
export SED_EXT

cat "$@" \
    | tr -dc '\n\t -~' \
    | tr '?!' '.' \
    | sed "${SED_EXT}" \
        -e 's/[[][^]]*[A-Z][A-Z][^]]*[]]//' \
        -e 's/Selah</Selah.</' \
        -e 's/<[^>]*>/ /g' \
        -e 's/([A-Za-z0-9.]*)/ \1/g' \
    | tr -s ' \t\r\n' '\n' \
    | tr -dc '\nA-Za-z0-9.' \
    | grep -v '^$' \
    | tr '\n.' ' \n' \
    | sed 's/^ //'
