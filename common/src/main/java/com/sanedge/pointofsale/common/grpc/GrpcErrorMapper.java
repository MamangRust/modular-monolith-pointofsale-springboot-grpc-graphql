package com.sanedge.pointofsale.common.grpc;

import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/** Maps domain exceptions onto gRPC statuses for server-side error handling. */
public final class GrpcErrorMapper {

    private GrpcErrorMapper() {
    }

    public static StatusRuntimeException toStatusRuntimeException(Throwable t) {
        if (t instanceof StatusRuntimeException sre) {
            return sre;
        }
        Status status;
        if (t instanceof ResourceNotFoundException) {
            status = Status.NOT_FOUND.withDescription(t.getMessage());
        } else if (t instanceof IllegalArgumentException) {
            status = Status.INVALID_ARGUMENT.withDescription(t.getMessage());
        } else {
            status = Status.INTERNAL.withDescription("Internal server error");
        }
        return status.asRuntimeException();
    }
}
