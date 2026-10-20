package com.sanedge.pointofsale.orderitem.handler;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.orderitem.service.OrderItemQueryService;
import com.sanedge.pointofsale.pb.order_item.ApiOrderItemsResponse;
import com.sanedge.pointofsale.pb.order_item.ApiResponseOrderItem;
import com.sanedge.pointofsale.pb.order_item.FindByIdOrderItemRequest;
import com.sanedge.pointofsale.pb.order_item.FindByOrderOrderItemRequest;
import com.sanedge.pointofsale.pb.order_item.OrderItemQueryServiceGrpc;
import com.sanedge.pointofsale.pb.order_item.OrderItemResponse;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderItemQueryHandler extends OrderItemQueryServiceGrpc.OrderItemQueryServiceImplBase {

    private final OrderItemQueryService orderItemQueryService;

    @Override
    public void findByOrder(FindByOrderOrderItemRequest request, StreamObserver<ApiOrderItemsResponse> observer) {
        try {
            ApiResponse<List<com.sanedge.pointofsale.orderitem.domain.responses.OrderItemResponse>> res = orderItemQueryService
                    .findByOrder(request.getOrderId());

            ApiOrderItemsResponse.Builder builder = ApiOrderItemsResponse.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
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
    public void findById(FindByIdOrderItemRequest request, StreamObserver<ApiResponseOrderItem> observer) {
        try {
            ApiResponse<com.sanedge.pointofsale.orderitem.domain.responses.OrderItemResponse> res = orderItemQueryService
                    .findById(request.getOrderItemId());

            ApiResponseOrderItem.Builder builder = ApiResponseOrderItem.newBuilder()
                    .setStatus(ProtoMappers.nvl(res.getStatus()))
                    .setMessage(ProtoMappers.nvl(res.getMessage()));
            if (res.getData() != null) {
                builder.setData(toProto(res.getData()));
            }

            observer.onNext(builder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private OrderItemResponse toProto(com.sanedge.pointofsale.orderitem.domain.responses.OrderItemResponse r) {
        return OrderItemResponse.newBuilder()
                .setOrderItemId(r.getId() == null ? 0 : r.getId().intValue())
                .setOrderId(r.getOrderId() == null ? 0 : r.getOrderId())
                .setProductId(r.getProductId() == null ? 0 : r.getProductId())
                .setQuantity(r.getQuantity() == null ? 0 : r.getQuantity())
                .setPrice(r.getPrice() == null ? 0 : r.getPrice())
                .setCreatedAt(ProtoMappers.nvl(r.getCreatedAt()))
                .setUpdatedAt(ProtoMappers.nvl(r.getUpdatedAt()))
                .build();
    }
}
