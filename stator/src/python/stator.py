from concurrent import futures
import time
import logging

import grpc

from envoy.api.v2 import discovery_pb2
from envoy.api.v2 import eds_pb2
from envoy.api.v2 import cds_pb2
from envoy.api.v2 import cds_pb2_grpc

_ONE_DAY_IN_SECONDS = 60 * 60 * 24


class ClusterDiscoveryService(cds_pb2_grpc.ClusterDiscoveryServiceServicer):

    def FetchClusters(self, request, context):
        return cds_pb2.Cluster()


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    cds_pb2_grpc.add_ClusterDiscoveryServiceServicer_to_server(ClusterDiscoveryService(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    try:
        while True:
            time.sleep(_ONE_DAY_IN_SECONDS)
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    logging.basicConfig()
    serve()
