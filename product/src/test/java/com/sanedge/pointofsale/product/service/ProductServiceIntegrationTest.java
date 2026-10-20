package com.sanedge.pointofsale.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.product.domain.requests.CreateProductRequest;
import com.sanedge.pointofsale.product.domain.requests.FindAllProductRequest;
import com.sanedge.pointofsale.product.domain.requests.UpdateProductRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.pb.merchant.ApiResponseMerchant;
import com.sanedge.pointofsale.pb.merchant.MerchantQueryServiceGrpc;
import com.sanedge.pointofsale.pb.merchant.MerchantResponse;
import com.sanedge.pointofsale.product.domain.responses.ProductResponse;
import com.sanedge.pointofsale.product.domain.responses.ProductResponseDeleteAt;
import com.sanedge.pointofsale.product.service.ProductCommandService;
import com.sanedge.pointofsale.product.service.ProductQueryService;

import jakarta.validation.Validator;

public class ProductServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductCommandService commandService;

    @Autowired
    private ProductQueryService queryService;

    @MockBean
    private Validator validator;

    @MockitoBean
    private MerchantQueryServiceGrpc.MerchantQueryServiceBlockingStub merchantQueryStub;

    @BeforeEach
    void mockMerchantService() {
        when(merchantQueryStub.findById(any()))
                .thenReturn(ApiResponseMerchant.newBuilder()
                        .setStatus("success")
                        .setData(MerchantResponse.getDefaultInstance())
                        .build());
    }

    @Test
    void testAllProductServiceMethods() {
        String image = "static/product/test/img.png";

        // 1. Create
        CreateProductRequest req = new CreateProductRequest();
        req.setMerchantId(adminMerchant.getMerchantId().intValue());
        req.setCategoryId(1);
        req.setName("Deluxe Monitor X");
        req.setDescription("Premium screen");
        req.setPrice(1200000);
        req.setCountInStock(15);
        req.setBrand("Monitors");
        req.setWeight(2000);
        req.setRating(5);
        req.setSlugProduct("deluxe-monitor-x");
        req.setImageProduct(image);

        ApiResponse<ProductResponse> createResp = commandService.createProduct(req);
        assertThat(createResp.getStatus()).isEqualTo("success");
        Long id = createResp.getData().getId();

        entityManager.flush();
        entityManager.clear();

        // 2. Find All
        FindAllProductRequest findReq = new FindAllProductRequest();
        findReq.setSearch("Monitor");
        findReq.setPage(1);
        findReq.setPageSize(10);

        ApiResponsePagination<List<ProductResponse>> listResp = queryService.findAll(findReq);
        assertThat(listResp.getStatus()).isEqualTo("success");

        // 3. Find By ID
        ApiResponse<ProductResponse> idResp = queryService.findById(id);
        assertThat(idResp.getStatus()).isEqualTo("success");

        // 4. Update
        UpdateProductRequest updateReq = new UpdateProductRequest();
        updateReq.setProductId(id.intValue());
        updateReq.setMerchantId(adminMerchant.getMerchantId().intValue());
        updateReq.setCategoryId(1);
        updateReq.setName("Deluxe Monitor X Updated");
        updateReq.setDescription("Updated Premium screen");
        updateReq.setPrice(1300000);
        updateReq.setCountInStock(10);
        updateReq.setBrand("Monitors");
        updateReq.setWeight(2000);
        updateReq.setRating(5);
        updateReq.setSlugProduct("deluxe-monitor-x-updated");
        updateReq.setImageProduct(image);

        ApiResponse<ProductResponse> updateResp = commandService.updateProduct(updateReq);
        assertThat(updateResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // 5. Find By Active
        ApiResponsePagination<List<ProductResponseDeleteAt>> activeResp = queryService.findActiveProducts(findReq);
        assertThat(activeResp.getStatus()).isEqualTo("success");

        // 6. Trash
        ApiResponse<ProductResponseDeleteAt> trashResp = commandService.trashedProduct(id.intValue());
        assertThat(trashResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // 7. Find By Trashed
        ApiResponsePagination<List<ProductResponseDeleteAt>> trashedResp = queryService.findTrashedProducts(findReq);
        assertThat(trashedResp.getStatus()).isEqualTo("success");

        // 8. Restore
        ApiResponse<ProductResponseDeleteAt> restoreResp = commandService.restoreProduct(id.intValue());
        assertThat(restoreResp.getStatus()).isEqualTo("success");

        // Trash again before delete permanent
        ApiResponse<ProductResponseDeleteAt> trashAgainResp = commandService.trashedProduct(id.intValue());
        assertThat(trashAgainResp.getStatus()).isEqualTo("success");

        // 9. Delete Permanent
        ApiResponse<Boolean> delPermResp = commandService.deleteProductPermanent(id.intValue());
        assertThat(delPermResp.getStatus()).isEqualTo("success");

        // 10. Restore All
        ApiResponse<Boolean> restoreAllResp = commandService.restoreAllProducts();
        assertThat(restoreAllResp.getStatus()).isEqualTo("success");

        // 11. Delete All Permanent
        ApiResponse<Boolean> delAllResp = commandService.deleteAllProductsPermanent();
        assertThat(delAllResp.getStatus()).isEqualTo("success");
    }
}
