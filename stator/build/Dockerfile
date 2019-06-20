FROM python:3.6-alpine

RUN apk add build-base curl
RUN pip install grpcio-tools

ENTRYPOINT [ "/bin/sh", "-c", "while true ; do sleep 1 ; done" ]
