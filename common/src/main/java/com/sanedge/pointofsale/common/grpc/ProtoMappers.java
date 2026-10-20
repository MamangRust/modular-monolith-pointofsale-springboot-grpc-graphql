package com.sanedge.pointofsale.common.grpc;

import com.sanedge.pointofsale.common.domain.responses.api.PaginationMeta;

/**
 * Small helpers shared by every module's gRPC handler when translating the
 * domain DTOs onto protobuf messages.
 */
public final class ProtoMappers {

    private ProtoMappers() {
    }

    /** Null-safe string: proto scalar fields cannot hold {@code null}. */
    public static String nvl(String value) {
        return value == null ? "" : value;
    }

    /** Domain pagination meta -> protobuf pagination meta. */
    public static com.sanedge.pointofsale.pb.common.PaginationMeta toProtoPagination(PaginationMeta meta) {
        if (meta == null) {
            return com.sanedge.pointofsale.pb.common.PaginationMeta.getDefaultInstance();
        }
        return com.sanedge.pointofsale.pb.common.PaginationMeta.newBuilder()
                .setCurrentPage(meta.getPage())
                .setPageSize(meta.getPageSize())
                .setTotalPages(meta.getTotalPages())
                .setTotalRecords((int) meta.getTotalItems())
                .build();
    }

    /** Protobuf pagination meta -> domain pagination meta (used by the gateway). */
    public static PaginationMeta fromProtoPagination(com.sanedge.pointofsale.pb.common.PaginationMeta proto) {
        if (proto == null) {
            return null;
        }
        return new PaginationMeta(
                proto.getCurrentPage(),
                proto.getPageSize(),
                proto.getTotalRecords(),
                proto.getTotalPages());
    }
}
