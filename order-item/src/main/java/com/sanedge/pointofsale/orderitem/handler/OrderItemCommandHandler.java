package com.sanedge.pointofsale.orderitem.handler;

import org.springframework.stereotype.Service;

import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.grpc.GrpcErrorMapper;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.orderitem.domain.requests.CreateOrderItemRequest;
import com.sanedge.pointofsale.orderitem.domain.requests.UpdateOrderItemRequest;
import com.sanedge.pointofsale.orderitem.service.OrderItemCommandService;
import com.sanedge.pointofsale.pb.order_item.ApiResponseOrderItem;
import com.sanedge.pointofsale.pb.order_item.OrderItemCommandServiceGrpc;
import com.sanedge.pointofsale.pb.order_item.OrderItemResponse;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderItemCommandHandler extends OrderItemCommandServiceGrpc.OrderItemCommandServiceImplBase {

    private final OrderItemCommandService orderItemCommandService;

    @Override
    public void createOrderItem(com.sanedge.pointofsale.pb.order_item.CreateOrderItemRequest request,
            StreamObserver<ApiResponseOrderItem> observer) {
        try {
            CreateOrderItemRequest req = new CreateOrderItemRequest();
            req.setOrderId(request.getOrderId());
            req.setProductId(request.getProductId());
            req.setQuantity(request.getQuantity());
            req.setPrice(request.getPrice());

            ApiResponse<com.sanedge.pointofsale.orderitem.domain.responses.OrderItemResponse> res = orderItemCommandService
                    .create(req);

            observer.onNext(toApiResponse(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    @Override
    public void updateOrderItem(com.sanedge.pointofsale.pb.order_item.UpdateOrderItemRequest request,
            StreamObserver<ApiResponseOrderItem> observer) {
        try {
            UpdateOrderItemRequest req = new UpdateOrderItemRequest();
            req.setOrderItemId(request.getOrderItemId());
            req.setOrderId(request.getOrderId());
            req.setProductId(request.getProductId());
            req.setQuantity(request.getQuantity());
            req.setPrice(request.getPrice());

            ApiResponse<com.sanedge.pointofsale.orderitem.domain.responses.OrderItemResponse> res = orderItemCommandService
                    .update(req);

            observer.onNext(toApiResponse(res));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcErrorMapper.toStatusRuntimeException(e));
        }
    }

    private ApiResponseOrderItem toApiResponse(
            ApiResponse<com.sanedge.pointofsale.orderitem.domain.responses.OrderItemResponse> res) {
        ApiResponseOrderItem.Builder builder = ApiResponseOrderItem.newBuilder()
                .setStatus(ProtoMappers.nvl(res.getStatus()))
                .setMessage(ProtoMappers.nvl(res.getMessage()));
        if (res.getData() != null) {
            builder.setData(toProto(res.getData()));
        }
        return builder.build();
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
