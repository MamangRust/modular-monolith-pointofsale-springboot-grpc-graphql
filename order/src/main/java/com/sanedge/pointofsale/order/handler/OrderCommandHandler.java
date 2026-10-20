package com.sanedge.pointofsale.order.handler;

import org.springframework.stereotype.Service;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.order.service.OrderCommandService;
import com.sanedge.pointofsale.pb.order.ApiResponseOrder;
import com.sanedge.pointofsale.pb.order.ApiResponseOrderAll;
import com.sanedge.pointofsale.pb.order.ApiResponseOrderDeleteAt;
import com.sanedge.pointofsale.pb.order.FindByIdOrderRequest;
import com.sanedge.pointofsale.pb.order.OrderCommandServiceGrpc;
import com.sanedge.pointofsale.pb.order.OrderResponse;
import com.sanedge.pointofsale.pb.order.OrderResponseDeleteAt;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCommandHandler extends OrderCommandServiceGrpc.OrderCommandServiceImplBase {

    private final OrderCommandService orderCommandService;

    @Override
    public void createOrder(com.sanedge.pointofsale.pb.order.CreateOrderRequest request,
            StreamObserver<ApiResponseOrder> observer) {
        try {
            com.sanedge.pointofsale.order.domain.requests.CreateOrderRequest req = new com.sanedge.pointofsale.order.domain.requests.CreateOrderRequest();
            req.setMerchantId(request.getMerchantId());
            req.setCashierId(request.getCashierId());
            req.setItems(request.getItemsList().stream().map(i -> {
                com.sanedge.pointofsale.order.domain.requests.CreateOrderItemRequest item = new com.sanedge.pointofsale.order.domain.requests.CreateOrderItemRequest();
                item.setProductId(i.getProductId());
                item.setQuantity(i.getQuantity());
                item.setPrice(i.getPrice());
                return item;
            }).toList());

            ApiResponse<com.sanedge.pointofsale.order.domain.responses.OrderResponse> res = orderCommandService
                    .create(req);

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
    public void updateOrder(com.sanedge.pointofsale.pb.order.UpdateOrderRequest request,
            StreamObserver<ApiResponseOrder> observer) {
        try {
            com.sanedge.pointofsale.order.domain.requests.UpdateOrderRequest req = new com.sanedge.pointofsale.order.domain.requests.UpdateOrderRequest();
            req.setOrderId(request.getOrderId());
            req.setCashierId(request.getCashierId());
            req.setItems(request.getItemsList().stream().map(i -> {
                com.sanedge.pointofsale.order.domain.requests.UpdateOrderItemRequest item = new com.sanedge.pointofsale.order.domain.requests.UpdateOrderItemRequest();
                item.setOrderItemId(i.getOrderItemId());
                item.setProductId(i.getProductId());
                item.setQuantity(i.getQuantity());
                item.setPrice(i.getPrice());
                return item;
            }).toList());

            ApiResponse<com.sanedge.pointofsale.order.domain.responses.OrderResponse> res = orderCommandService
                    .update(req);

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
    public void trashedOrder(FindByIdOrderRequest request, StreamObserver<ApiResponseOrderDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.order.domain.responses.OrderResponseDeleteAt> res = orderCommandService
                    .trash(request.getOrderId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreOrder(FindByIdOrderRequest request, StreamObserver<ApiResponseOrderDeleteAt> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.order.domain.responses.OrderResponseDeleteAt> res = orderCommandService
                    .restore(request.getOrderId());
            observer.onNext(toDeleteAt(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteOrderPermanent(FindByIdOrderRequest request, StreamObserver<ApiResponseOrderAll> observer) {
        try {
            ApiResponse<Boolean> res = orderCommandService.delete(request.getOrderId());
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void restoreAllOrder(Empty request, StreamObserver<ApiResponseOrderAll> observer) {
        try {
            ApiResponse<Boolean> res = orderCommandService.restoreAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void deleteAllOrderPermanent(Empty request, StreamObserver<ApiResponseOrderAll> observer) {
        try {
            ApiResponse<Boolean> res = orderCommandService.deleteAll();
            observer.onNext(toAll(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
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

    private ApiResponseOrderDeleteAt toDeleteAt(
            ApiResponse<com.sanedge.pointofsale.order.domain.responses.OrderResponseDeleteAt> res) {
        return ApiResponseOrderDeleteAt.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setData(toProtoDeleteAt(res.getData()))
                .build();
    }

    private ApiResponseOrderAll toAll(ApiResponse<Boolean> res) {
        return ApiResponseOrderAll.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()))
                .setSuccess(Boolean.TRUE.equals(res.getData()))
                .build();
    }
}
