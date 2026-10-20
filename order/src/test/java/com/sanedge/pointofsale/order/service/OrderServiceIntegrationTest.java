package com.sanedge.pointofsale.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.order.domain.requests.CreateOrderItemRequest;
import com.sanedge.pointofsale.order.domain.requests.CreateOrderRequest;
import com.sanedge.pointofsale.order.domain.requests.FindAllOrderRequest;
import com.sanedge.pointofsale.order.domain.requests.UpdateOrderItemRequest;
import com.sanedge.pointofsale.order.domain.requests.UpdateOrderRequest;
import com.sanedge.pointofsale.order.domain.responses.OrderResponse;
import com.sanedge.pointofsale.order.domain.responses.OrderResponseDeleteAt;
import com.sanedge.pointofsale.pb.cashier.ApiResponseCashier;
import com.sanedge.pointofsale.pb.cashier.CashierQueryServiceGrpc;
import com.sanedge.pointofsale.pb.cashier.CashierResponse;
import com.sanedge.pointofsale.pb.merchant.ApiResponseMerchant;
import com.sanedge.pointofsale.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.pointofsale.pb.merchant.MerchantResponse;
import com.sanedge.pointofsale.pb.order_item.ApiResponseOrderItem;
import com.sanedge.pointofsale.pb.order_item.OrderItemCommandServiceGrpc;
import com.sanedge.pointofsale.pb.order_item.OrderItemResponse;
import com.sanedge.pointofsale.pb.product.ApiResponseProduct;
import com.sanedge.pointofsale.pb.product.ProductCommandServiceGrpc;
import com.sanedge.pointofsale.pb.product.ProductQueryServiceGrpc;
import com.sanedge.pointofsale.pb.product.ProductResponse;

import jakarta.validation.Validator;

public class OrderServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderCommandService commandService;

    @Autowired
    private OrderQueryService queryService;

    @MockitoBean
    private Validator validator;

    @MockitoBean
    private MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub;

    @MockitoBean
    private CashierQueryServiceGrpc.CashierQueryServiceBlockingStub cashierQueryStub;

    @MockitoBean
    private ProductQueryServiceGrpc.ProductQueryServiceBlockingStub productQueryStub;

    @MockitoBean
    private ProductCommandServiceGrpc.ProductCommandServiceBlockingStub productCommandStub;

    @MockitoBean
    private OrderItemCommandServiceGrpc.OrderItemCommandServiceBlockingStub orderItemCommandStub;

    @BeforeEach
    void setupMocks() {
        when(merchantQueryStub.findById(any()))
                .thenReturn(ApiResponseMerchant.newBuilder()
                        .setStatus("success")
                        .setMessage("ok")
                        .setData(MerchantResponse.newBuilder().setMerchantId(1).build())
                        .build());

        when(cashierQueryStub.findById(any()))
                .thenReturn(ApiResponseCashier.newBuilder()
                        .setStatus("success")
                        .setMessage("ok")
                        .setData(CashierResponse.newBuilder().setCashierId(1).build())
                        .build());

        when(productQueryStub.findById(any()))
                .thenReturn(ApiResponseProduct.newBuilder()
                        .setStatus("success")
                        .setMessage("ok")
                        .setData(ProductResponse.newBuilder()
                                .setProductId(101)
                                .setMerchantId(1)
                                .setCategoryId(1)
                                .setName("Order Product")
                                .setPrice(15000)
                                .setCountInStock(10)
                                .build())
                        .build());

        when(orderItemCommandStub.createOrderItem(any()))
                .thenReturn(ApiResponseOrderItem.newBuilder()
                        .setStatus("success")
                        .setMessage("ok")
                        .setData(OrderItemResponse.newBuilder().setOrderItemId(1).build())
                        .build());

        when(orderItemCommandStub.updateOrderItem(any()))
                .thenReturn(ApiResponseOrderItem.newBuilder()
                        .setStatus("success")
                        .setMessage("ok")
                        .setData(OrderItemResponse.newBuilder().setOrderItemId(1).build())
                        .build());

        when(productCommandStub.updateProduct(any()))
                .thenReturn(ApiResponseProduct.newBuilder()
                        .setStatus("success")
                        .setMessage("ok")
                        .setData(ProductResponse.newBuilder()
                                .setProductId(101)
                                .setMerchantId(1)
                                .setCategoryId(1)
                                .setName("Order Product")
                                .setPrice(15000)
                                .setCountInStock(8)
                                .build())
                        .build());
    }

    @Test
    void testAllOrderServiceMethods() {
        // 1. Create
        CreateOrderRequest req = new CreateOrderRequest();
        req.setMerchantId(1);
        req.setCashierId(1);

        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setProductId(101);
        item.setQuantity(2);
        item.setPrice(15000);

        List<CreateOrderItemRequest> items = new ArrayList<>();
        items.add(item);
        req.setItems(items);

        ApiResponse<OrderResponse> createResp = commandService.create(req);
        assertThat(createResp.getStatus()).isEqualTo("success");
        Long id = createResp.getData().getId();

        entityManager.flush();
        entityManager.clear();

        // 2. Find All
        FindAllOrderRequest findReq = new FindAllOrderRequest();
        findReq.setSearch(null);
        findReq.setPage(1);
        findReq.setPageSize(10);

        ApiResponsePagination<List<OrderResponse>> listResp = queryService.findAll(findReq);
        assertThat(listResp.getStatus()).isEqualTo("success");

        // 3. Find By ID
        ApiResponse<OrderResponse> idResp = queryService.findById(id.intValue());
        assertThat(idResp.getStatus()).isEqualTo("success");

        // 4. Update
        UpdateOrderRequest updateReq = new UpdateOrderRequest();
        updateReq.setOrderId(id.intValue());
        updateReq.setCashierId(1);

        UpdateOrderItemRequest upItem = new UpdateOrderItemRequest();
        upItem.setProductId(101);
        upItem.setQuantity(3);
        upItem.setPrice(15000);
        updateReq.setItems(List.of(upItem));

        ApiResponse<OrderResponse> updateResp = commandService.update(updateReq);
        assertThat(updateResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // 5. Find By Active
        ApiResponsePagination<List<OrderResponseDeleteAt>> activeResp = queryService.findByActive(findReq);
        assertThat(activeResp.getStatus()).isEqualTo("success");

        // 6. Trash
        ApiResponse<OrderResponseDeleteAt> trashResp = commandService.trash(id.intValue());
        assertThat(trashResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // 7. Find By Trashed
        ApiResponsePagination<List<OrderResponseDeleteAt>> trashedResp = queryService.findByTrashed(findReq);
        assertThat(trashedResp.getStatus()).isEqualTo("success");

        // 8. Restore
        ApiResponse<OrderResponseDeleteAt> restoreResp = commandService.restore(id.intValue());
        assertThat(restoreResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // Trash again before delete permanent
        ApiResponse<OrderResponseDeleteAt> trashAgainResp = commandService.trash(id.intValue());
        assertThat(trashAgainResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // 9. Delete Permanent
        ApiResponse<Boolean> delPermResp = commandService.delete(id.intValue());
        assertThat(delPermResp.getStatus()).isEqualTo("success");

        // 10. Restore All
        ApiResponse<Boolean> restoreAllResp = commandService.restoreAll();
        assertThat(restoreAllResp.getStatus()).isEqualTo("success");

        // 11. Delete All Permanent
        ApiResponse<Boolean> delAllResp = commandService.deleteAll();
        assertThat(delAllResp.getStatus()).isEqualTo("success");
    }
}
