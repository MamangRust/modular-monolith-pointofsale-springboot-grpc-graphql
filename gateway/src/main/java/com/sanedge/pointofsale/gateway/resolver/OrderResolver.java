package com.sanedge.pointofsale.gateway.resolver;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.google.protobuf.Empty;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.common.grpc.ProtoMappers;
import com.sanedge.pointofsale.gateway.dto.inputs.CreateOrderInput;
import com.sanedge.pointofsale.gateway.dto.inputs.CreateOrderItemInput;
import com.sanedge.pointofsale.gateway.dto.inputs.OrderMerchantYearInput;
import com.sanedge.pointofsale.gateway.dto.inputs.OrderMerchantYearMonthInput;
import com.sanedge.pointofsale.gateway.dto.inputs.OrderMonthTotalRevenueInput;
import com.sanedge.pointofsale.gateway.dto.inputs.OrderYearInput;
import com.sanedge.pointofsale.gateway.dto.inputs.OrderYearMonthInput;
import com.sanedge.pointofsale.gateway.dto.inputs.PageInput;
import com.sanedge.pointofsale.gateway.dto.inputs.UpdateOrderInput;
import com.sanedge.pointofsale.gateway.dto.inputs.UpdateOrderItemInput;
import com.sanedge.pointofsale.gateway.dto.responses.OrderMonthlyResponse;
import com.sanedge.pointofsale.gateway.dto.responses.OrderMonthlyTotalRevenueResponse;
import com.sanedge.pointofsale.gateway.dto.responses.OrderResponse;
import com.sanedge.pointofsale.gateway.dto.responses.OrderResponseDeleteAt;
import com.sanedge.pointofsale.gateway.dto.responses.OrderYearlyResponse;
import com.sanedge.pointofsale.gateway.dto.responses.OrderYearlyTotalRevenueResponse;
import com.sanedge.pointofsale.gateway.mapper.ProtoMapper;
import com.sanedge.pointofsale.pb.order.OrderCommandServiceGrpc;
import com.sanedge.pointofsale.pb.order.OrderQueryServiceGrpc;
import com.sanedge.pointofsale.pb.order.stats.OrderSoldoutServiceGrpc;
import com.sanedge.pointofsale.pb.order.stats.OrderTotalRevenueServiceGrpc;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OrderResolver {

    private final OrderQueryServiceGrpc.OrderQueryServiceBlockingStub orderQueryStub;
    private final OrderCommandServiceGrpc.OrderCommandServiceBlockingStub orderCommandStub;
    private final OrderSoldoutServiceGrpc.OrderSoldoutServiceBlockingStub orderSoldoutStub;
    private final OrderTotalRevenueServiceGrpc.OrderTotalRevenueServiceBlockingStub orderTotalRevenueStub;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<OrderResponse>> orders(@Argument PageInput input) {
        var resp = orderQueryStub.findAll(com.sanedge.pointofsale.pb.order.FindAllOrderRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<OrderResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.orders(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> order(@Argument Integer id) {
        var resp = orderQueryStub.findById(com.sanedge.pointofsale.pb.order.FindByIdOrderRequest.newBuilder()
                .setOrderId(id)
                .build());
        return ApiResponse.<OrderResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.order(resp.getData()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<OrderResponseDeleteAt>> ordersActive(@Argument PageInput input) {
        var resp = orderQueryStub.findByActive(com.sanedge.pointofsale.pb.order.FindAllOrderRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<OrderResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.ordersDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<OrderResponseDeleteAt>> ordersTrashed(@Argument PageInput input) {
        var resp = orderQueryStub.findByTrashed(com.sanedge.pointofsale.pb.order.FindAllOrderRequest.newBuilder()
                .setPage(nvl(input != null ? input.page() : null))
                .setPageSize(nvl(input != null ? input.pageSize() : null))
                .setSearch(nvl(input != null ? input.search() : null))
                .build());
        return ApiResponsePagination.<List<OrderResponseDeleteAt>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.ordersDeleteAt(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponsePagination<List<OrderResponse>> ordersByMerchant(@Argument Integer merchantId,
            @Argument PageInput input) {
        var resp = orderQueryStub.findByMerchant(
                com.sanedge.pointofsale.pb.order.FindByMerchantOrderRequest.newBuilder()
                        .setMerchantId(nvl(merchantId))
                        .setPage(nvl(input != null ? input.page() : null))
                        .setPageSize(nvl(input != null ? input.pageSize() : null))
                        .setSearch(nvl(input != null ? input.search() : null))
                        .build());
        return ApiResponsePagination.<List<OrderResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.orders(resp.getDataList()))
                .pagination(ProtoMappers.fromProtoPagination(resp.getPaginationMeta()))
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<OrderMonthlyResponse>> orderMonthlyRevenue(@Argument OrderYearMonthInput input) {
        var resp = orderSoldoutStub.findMonthlyOrders(com.sanedge.pointofsale.pb.order.FindYearMonthOrder.newBuilder()
                .setYearMonth(nvl(input != null ? input.yearMonth() : null))
                .build());
        return ApiResponse.<List<OrderMonthlyResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::orderMonthly).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<OrderYearlyResponse>> orderYearlyRevenue(@Argument OrderYearInput input) {
        var resp = orderSoldoutStub.findYearlyOrders(com.sanedge.pointofsale.pb.order.FindYearOrder.newBuilder()
                .setYear(nvl(input != null ? input.year() : null))
                .build());
        return ApiResponse.<List<OrderYearlyResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::orderYearly).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<OrderMonthlyTotalRevenueResponse>> orderMonthlyTotalRevenue(
            @Argument OrderMonthTotalRevenueInput input) {
        var resp = orderTotalRevenueStub.findMonthlyTotalRevenue(
                com.sanedge.pointofsale.pb.order.FindYearMonthTotalRevenue.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<OrderMonthlyTotalRevenueResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::orderMonthlyTotalRevenue).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<OrderYearlyTotalRevenueResponse>> orderYearlyTotalRevenue(
            @Argument OrderYearInput input) {
        var resp = orderTotalRevenueStub.findYearlyTotalRevenue(
                com.sanedge.pointofsale.pb.order.FindYearOrder.newBuilder()
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<OrderYearlyTotalRevenueResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::orderYearlyTotalRevenue).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<OrderMonthlyResponse>> orderMerchantMonthlyRevenue(
            @Argument OrderMerchantYearInput input) {
        var resp = orderSoldoutStub.findMonthlyOrdersByMerchant(
                com.sanedge.pointofsale.pb.order.FindYearMonthOrderByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYearMonth(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<OrderMonthlyResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::orderMonthly).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<OrderYearlyResponse>> orderMerchantYearlyRevenue(
            @Argument OrderMerchantYearInput input) {
        var resp = orderSoldoutStub.findYearlyOrdersByMerchant(
                com.sanedge.pointofsale.pb.order.FindYearOrderByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<OrderYearlyResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::orderYearly).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<OrderMonthlyTotalRevenueResponse>> orderMerchantMonthlyTotalRevenue(
            @Argument OrderMerchantYearMonthInput input) {
        var resp = orderTotalRevenueStub.findMonthlyTotalRevenueByMerchant(
                com.sanedge.pointofsale.pb.order.FindYearMonthTotalRevenueByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .setMonth(nvl(input != null ? input.month() : null))
                        .build());
        return ApiResponse.<List<OrderMonthlyTotalRevenueResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::orderMonthlyTotalRevenue).toList())
                .build();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<OrderYearlyTotalRevenueResponse>> orderMerchantYearlyTotalRevenue(
            @Argument OrderMerchantYearInput input) {
        var resp = orderTotalRevenueStub.findYearlyTotalRevenueByMerchant(
                com.sanedge.pointofsale.pb.order.FindYearOrderByMerchant.newBuilder()
                        .setMerchantId(nvl(input != null ? input.merchantId() : null))
                        .setYear(nvl(input != null ? input.year() : null))
                        .build());
        return ApiResponse.<List<OrderYearlyTotalRevenueResponse>>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getDataList().stream().map(ProtoMapper::orderYearlyTotalRevenue).toList())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<OrderResponse> createOrder(@Argument CreateOrderInput input) {
        var builder = com.sanedge.pointofsale.pb.order.CreateOrderRequest.newBuilder()
                .setMerchantId(nvl(input.merchantId()))
                .setCashierId(nvl(input.cashierId()));
        if (input.items() != null) {
            for (CreateOrderItemInput item : input.items()) {
                builder.addItems(com.sanedge.pointofsale.pb.order.CreateOrderItemRequest.newBuilder()
                        .setProductId(nvl(item.productId()))
                        .setQuantity(nvl(item.quantity()))
                        .setPrice(nvl(item.price()))
                        .build());
            }
        }
        var resp = orderCommandStub.createOrder(builder.build());
        return ApiResponse.<OrderResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.order(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<OrderResponse> updateOrder(@Argument UpdateOrderInput input) {
        var builder = com.sanedge.pointofsale.pb.order.UpdateOrderRequest.newBuilder()
                .setOrderId(nvl(input.orderId()))
                .setCashierId(nvl(input.cashierId()));
        if (input.items() != null) {
            for (UpdateOrderItemInput item : input.items()) {
                builder.addItems(com.sanedge.pointofsale.pb.order.UpdateOrderItemRequest.newBuilder()
                        .setOrderItemId(nvl(item.orderItemId()))
                        .setProductId(nvl(item.productId()))
                        .setQuantity(nvl(item.quantity()))
                        .setPrice(nvl(item.price()))
                        .build());
            }
        }
        var resp = orderCommandStub.updateOrder(builder.build());
        return ApiResponse.<OrderResponse>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.order(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<OrderResponseDeleteAt> trashedOrder(@Argument Integer id) {
        var resp = orderCommandStub.trashedOrder(com.sanedge.pointofsale.pb.order.FindByIdOrderRequest.newBuilder()
                .setOrderId(id)
                .build());
        return ApiResponse.<OrderResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.orderDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<OrderResponseDeleteAt> restoreOrder(@Argument Integer id) {
        var resp = orderCommandStub.restoreOrder(com.sanedge.pointofsale.pb.order.FindByIdOrderRequest.newBuilder()
                .setOrderId(id)
                .build());
        return ApiResponse.<OrderResponseDeleteAt>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(ProtoMapper.orderDeleteAt(resp.getData()))
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteOrderPermanent(@Argument Integer id) {
        var resp = orderCommandStub.deleteOrderPermanent(
                com.sanedge.pointofsale.pb.order.FindByIdOrderRequest.newBuilder()
                        .setOrderId(id)
                        .build());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> restoreAllOrders() {
        var resp = orderCommandStub.restoreAllOrder(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Boolean> deleteAllOrdersPermanent() {
        var resp = orderCommandStub.deleteAllOrderPermanent(Empty.getDefaultInstance());
        return ApiResponse.<Boolean>builder()
                .status(resp.getStatus())
                .message(resp.getMessage())
                .data(resp.getSuccess())
                .build();
    }

    private static int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
