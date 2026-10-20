package com.sanedge.pointofsale.merchant.service;

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
import com.sanedge.pointofsale.merchant.domain.requests.CreateMerchantRequest;
import com.sanedge.pointofsale.merchant.domain.requests.FindAllMerchants;
import com.sanedge.pointofsale.merchant.domain.requests.UpdateMerchantRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantResponse;
import com.sanedge.pointofsale.merchant.domain.responses.MerchantResponseDeleteAt;
import com.sanedge.pointofsale.pb.user.ApiResponseUser;
import com.sanedge.pointofsale.pb.user.UserQueryServiceGrpc;
import com.sanedge.pointofsale.pb.user.UserResponse;
import com.sanedge.pointofsale.merchant.service.MerchantCommandService;
import com.sanedge.pointofsale.merchant.service.MerchantQueryService;

import jakarta.validation.Validator;

public class MerchantServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MerchantCommandService commandService;

    @Autowired
    private MerchantQueryService queryService;

    @MockBean
    private Validator validator;

    @MockitoBean
    private UserQueryServiceGrpc.UserQueryServiceBlockingStub userQueryStub;

    @BeforeEach
    void mockUserService() {
        when(userQueryStub.findById(any()))
                .thenReturn(ApiResponseUser.newBuilder()
                        .setStatus("success")
                        .setData(UserResponse.newBuilder().setId(1).build())
                        .build());
    }

    @Test
    void testAllMerchantServiceMethods() {
        // 1. Create
        CreateMerchantRequest req = new CreateMerchantRequest();
        req.setUserId(adminUser.getUserId().intValue());
        req.setName("ServiceUniqueMerchant");
        req.setDescription("Description");
        req.setAddress("Address");
        req.setContactEmail("service@merchant.com");
        req.setContactPhone("0812345678");
        req.setStatus("PENDING");

        ApiResponse<MerchantResponse> createResp = commandService.createMerchant(req);
        assertThat(createResp.getStatus()).isEqualTo("success");
        Long id = createResp.getData().getId();

        entityManager.flush();
        entityManager.clear();

        // 2. Find All
        FindAllMerchants findReq = new FindAllMerchants();
        findReq.setSearch("ServiceUniqueMerchant");
        findReq.setPage(1);
        findReq.setPageSize(10);

        ApiResponsePagination<List<MerchantResponse>> listResp = queryService.findAll(findReq);
        assertThat(listResp.getStatus()).isEqualTo("success");

        // 3. Find By ID
        ApiResponse<MerchantResponse> idResp = queryService.findById(id.intValue());
        assertThat(idResp.getStatus()).isEqualTo("success");

        // 4. Update
        UpdateMerchantRequest updateReq = new UpdateMerchantRequest();
        updateReq.setMerchantId(id.intValue());
        updateReq.setUserId(adminUser.getUserId().intValue());
        updateReq.setName("UpdatedMerchantName");
        updateReq.setDescription("Updated description");
        updateReq.setAddress("Updated Address");
        updateReq.setContactEmail("updated@merchant.com");
        updateReq.setContactPhone("0812345679");
        updateReq.setStatus("ACTIVE");

        ApiResponse<MerchantResponse> updateResp = commandService.updateMerchant(updateReq);
        assertThat(updateResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // 5. Find By Active
        ApiResponsePagination<List<MerchantResponseDeleteAt>> activeResp = queryService.findByActive(findReq);
        assertThat(activeResp.getStatus()).isEqualTo("success");

        // 6. Trash
        ApiResponse<MerchantResponseDeleteAt> trashResp = commandService.trashedMerchant(id.intValue());
        assertThat(trashResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // 7. Find By Trashed
        ApiResponsePagination<List<MerchantResponseDeleteAt>> trashedResp = queryService.findByTrashed(findReq);
        assertThat(trashedResp.getStatus()).isEqualTo("success");

        // 8. Restore
        ApiResponse<MerchantResponseDeleteAt> restoreResp = commandService.restoreMerchant(id.intValue());
        assertThat(restoreResp.getStatus()).isEqualTo("success");

        // Trash again before delete permanent
        ApiResponse<MerchantResponseDeleteAt> trashAgainResp = commandService.trashedMerchant(id.intValue());
        assertThat(trashAgainResp.getStatus()).isEqualTo("success");

        // 9. Delete Permanent
        ApiResponse<Boolean> delPermResp = commandService.deleteMerchantPermanent(id.intValue());
        assertThat(delPermResp.getStatus()).isEqualTo("success");

        // 10. Restore All
        ApiResponse<Boolean> restoreAllResp = commandService.restoreAllMerchant();
        assertThat(restoreAllResp.getStatus()).isEqualTo("success");

        // 11. Delete All Permanent
        ApiResponse<Boolean> delAllResp = commandService.deleteAllMerchantPermanent();
        assertThat(delAllResp.getStatus()).isEqualTo("success");
    }
}
