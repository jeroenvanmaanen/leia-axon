#!/usr/bin/env bash

aws.sh ec2 describe-spot-instance-requests --query 'SpotInstanceRequests[*].{ID:InstanceId}'
