package com.sanedge.pointofsale.order.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.order.service.OrderQueryService;
import com.sanedge.pointofsale.pb.order.ApiResponseOrder;
import com.sanedge.pointofsale.pb.order.ApiResponsePaginationOrder;
import com.sanedge.pointofsale.pb.order.ApiResponsePaginationOrderDeleteAt;
import com.sanedge.pointofsale.pb.order.FindAllOrderRequest;
import com.sanedge.pointofsale.pb.order.FindByIdOrderRequest;
import com.sanedge.pointofsale.pb.order.FindByMerchantOrderRequest;
import com.sanedge.pointofsale.pb.order.OrderQueryServiceGrpc;
import com.sanedge.pointofsale.pb.order.OrderResponse;
import com.sanedge.pointofsale.pb.order.OrderResponseDeleteAt;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderQueryHandler extends OrderQueryServiceGrpc.OrderQueryServiceImplBase {

    private final OrderQueryService orderQueryService;

    @Override
    public void findAll(FindAllOrderRequest request, StreamObserver<ApiResponsePaginationOrder> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.order.domain.responses.OrderResponse>> res = orderQueryService
                    .findAll(toFindAll(request));

            ApiResponsePaginationOrder.Builder builder = ApiResponsePaginationOrder.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProto(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findById(FindByIdOrderRequest request, StreamObserver<ApiResponseOrder> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.order.domain.responses.OrderResponse> res = orderQueryService
                    .findById(request.getOrderId());

            observer.onNext(ApiResponseOrder.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setData(toProto(res.getData()))
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByMerchant(FindByMerchantOrderRequest request,
            StreamObserver<ApiResponsePaginationOrder> observer) {
        try {
            com.sanedge.pointofsale.order.domain.requests.FindAllOrderByMerchantRequest req = new com.sanedge.pointofsale.order.domain.requests.FindAllOrderByMerchantRequest();
            req.setMerchantId(request.getMerchantId());
            req.setSearch(request.getSearch());
            req.setPage(request.getPage());
            req.setPageSize(request.getPageSize());

            ApiResponsePagination<List<com.sanedge.pointofsale.order.domain.responses.OrderResponse>> res = orderQueryService
                    .findByMerchantId(req);

            ApiResponsePaginationOrder.Builder builder = ApiResponsePaginationOrder.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProto(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByActive(FindAllOrderRequest request,
            StreamObserver<ApiResponsePaginationOrderDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.order.domain.responses.OrderResponseDeleteAt>> res = orderQueryService
                    .findByActive(toFindAll(request));

            ApiResponsePaginationOrderDeleteAt.Builder builder = ApiResponsePaginationOrderDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void findByTrashed(FindAllOrderRequest request,
            StreamObserver<ApiResponsePaginationOrderDeleteAt> observer) {
        try {
            ApiResponsePagination<List<com.sanedge.pointofsale.order.domain.responses.OrderResponseDeleteAt>> res = orderQueryService
                    .findByTrashed(toFindAll(request));

            ApiResponsePaginationOrderDeleteAt.Builder builder = ApiResponsePaginationOrderDeleteAt.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()))
                    .setPaginationMeta(ProtoMappers.toProtoPagination(res.getPagination()));
            if (res.getData() != null) {
                res.getData().forEach(r -> builder.addData(toProtoDeleteAt(r)));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private com.sanedge.pointofsale.order.domain.requests.FindAllOrderRequest toFindAll(FindAllOrderRequest request) {
        com.sanedge.pointofsale.order.domain.requests.FindAllOrderRequest req = new com.sanedge.pointofsale.order.domain.requests.FindAllOrderRequest();
        req.setPage(request.getPage());
        req.setPageSize(request.getPageSize());
        req.setSearch(request.getSearch());
        return req;
    }

    private OrderResponse toProto(com.sanedge.pointofsale.order.domain.responses.OrderResponse r) {
        if (r == null) {
            return OrderResponse.getDefaultInstance();
        }
        return OrderResponse.newBuilder()
                .setOrderId(r.getId() == null ? 0 : r.getId().intValue())
                .setMerchantId(r.getMerchantId() == null ? 0 : r.getMerchantId())
                .setCashierId(r.getCashierId() == null ? 0 : r.getCashierId())
                .setTotalPrice(r.getTotalPrice() == null ? 0L : r.getTotalPrice())
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }

    private OrderResponseDeleteAt toProtoDeleteAt(
            com.sanedge.pointofsale.order.domain.responses.OrderResponseDeleteAt r) {
        if (r == null) {
            return OrderResponseDeleteAt.getDefaultInstance();
        }
        return OrderResponseDeleteAt.newBuilder()
                .setOrderId(r.getId() == null ? 0 : r.getId().intValue())
                .setMerchantId(r.getMerchantId() == null ? 0 : r.getMerchantId())
                .setCashierId(r.getCashierId() == null ? 0 : r.getCashierId())
                .setTotalPrice(r.getTotalPrice() == null ? 0L : r.getTotalPrice())
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .setDeletedAt(ProtoMappers.nvl(r.getDeletedAt()))
                .build();
    }
}
