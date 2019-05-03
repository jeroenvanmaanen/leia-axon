#!/bin/bash

set -e

: "${FLAGS_INHERIT:=}" # Assigned in VERBOSE_SCRIPT
source ~/src/scripts/verbose.sh

aws.sh configure set region eu-west-1

aws.sh "${FLAGS_INHERIT[@]}" ec2 describe-instances --filters Name=instance-state-name,Values=running \
	| sed -E \
		-e 's///g' \
		-e 's/^        {/|/' \
		-e '/^                     /d' \
		-e '/[|]|PublicDnsName|InstanceId|ImageId/!d' \
		-e 's/^ *//' \
		-e 's/,? *$//' \
		-e 's/": "/:/' \
		-e 's/"//g' \
	| tr '\012|' '|\012' \
	| sed -e '/^$/d'

