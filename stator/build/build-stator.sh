#!/usr/bin/env sh

set -e

BUILD="$(cd "$(dirname "$0")" ; pwd)"
STATOR="$(dirname "${BUILD}")"
TARGET="${STATOR}/target"

function extract_proto() {
    local TAR_FILE="$1"
    tar -tzf "${TAR_FILE}" | grep '[.]proto$' | tar -C include -xvzf "${TAR_FILE}" -T -
}

function download_package() {
    local URL="$1"
    local FILE="$2"
    if [ \! -f "${FILE}" ]
    then
        curl -L -sS -D - "${URL}" -o "${FILE}" || true
    fi
}

(
    mkdir -p "${TARGET}"
    cd "${TARGET}"
    ## download_package https://github.com/gogo/protobuf/archive/v1.2.1.tar.gz './gogo.tar.gz'
    download_package https://github.com/googleapis/googleapis/archive/common-protos-1_3_1.tar.gz './common-protos.tar.gz'
    download_package https://github.com/envoyproxy/protoc-gen-validate/archive/v0.1.0.tar.gz './validate.tar.gz'
    download_package https://github.com/envoyproxy/data-plane-api/archive/master.tar.gz './data-plane-api.tar.gz'
    mkdir -p include
    ## extract_proto './gogo.tar.gz'
    extract_proto './common-protos.tar.gz'
    extract_proto './validate.tar.gz'
    extract_proto './data-plane-api.tar.gz'
    mkdir -p python
    python -m grpc_tools.protoc \
        -Iinclude/data-plane-api-master \
        -Iinclude/protoc-gen-validate-0.1.0 \
        -Iinclude/googleapis-common-protos-1_3_1 \
        --python_out=python \
        --grpc_python_out=python \
        include/googleapis-common-protos-1_3_1/google/api/annotations.proto \
        include/googleapis-common-protos-1_3_1/google/api/http.proto \
        include/googleapis-common-protos-1_3_1/google/rpc/status.proto \
        include/protoc-gen-validate-0.1.0/gogoproto/gogo.proto \
        include/protoc-gen-validate-0.1.0/validate/validate.proto \
        include/data-plane-api-master/envoy/type/percent.proto \
        include/data-plane-api-master/envoy/type/range.proto \
        include/data-plane-api-master/envoy/api/v2/core/base.proto \
        include/data-plane-api-master/envoy/api/v2/core/address.proto \
        include/data-plane-api-master/envoy/api/v2/core/health_check.proto \
        include/data-plane-api-master/envoy/api/v2/core/config_source.proto \
        include/data-plane-api-master/envoy/api/v2/core/grpc_service.proto \
        include/data-plane-api-master/envoy/api/v2/core/protocol.proto \
        include/data-plane-api-master/envoy/api/v2/auth/cert.proto \
        include/data-plane-api-master/envoy/api/v2/endpoint/endpoint.proto \
        include/data-plane-api-master/envoy/api/v2/discovery.proto \
        include/data-plane-api-master/envoy/api/v2/cluster/circuit_breaker.proto \
        include/data-plane-api-master/envoy/api/v2/cluster/outlier_detection.proto \
        include/data-plane-api-master/envoy/api/v2/cds.proto
)
