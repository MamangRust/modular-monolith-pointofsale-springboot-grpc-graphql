package com.sanedge.pointofsale.role.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.role.domain.requests.CreateRoleRequest;
import com.sanedge.pointofsale.role.domain.requests.FindAllRoles;
import com.sanedge.pointofsale.role.domain.requests.UpdateRoleRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.role.domain.responses.RoleResponse;
import com.sanedge.pointofsale.role.domain.responses.RoleResponseDeleteAt;
import com.sanedge.pointofsale.role.service.RoleCommandService;
import com.sanedge.pointofsale.role.service.RoleQueryService;

public class RoleServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RoleCommandService commandService;

    @Autowired
    private RoleQueryService queryService;

    @Test
    void testAllRoleServiceMethods() {
        // 1. Create
        CreateRoleRequest req = new CreateRoleRequest();
        req.setName("ROLE_PARTNER_UNIQUE");

        ApiResponse<RoleResponse> createResp = commandService.create(req);
        assertThat(createResp.getStatus()).isEqualTo("success");
        Integer id = createResp.getData().getId();

        entityManager.flush();
        entityManager.clear();

        // 2. Find All
        FindAllRoles findReq = new FindAllRoles();
        findReq.setSearch("ROLE_PARTNER_UNIQUE");
        findReq.setPage(1);
        findReq.setPageSize(10);

        ApiResponsePagination<List<RoleResponse>> listResp = queryService.findAll(findReq);
        assertThat(listResp.getStatus()).isEqualTo("success");

        // 3. Find By ID
        ApiResponse<RoleResponse> idResp = queryService.findById(id);
        assertThat(idResp.getStatus()).isEqualTo("success");

        // 4. Update
        UpdateRoleRequest updateReq = new UpdateRoleRequest();
        updateReq.setId(id);
        updateReq.setName("ROLE_PARTNER_UPDATED");

        ApiResponse<RoleResponse> updateResp = commandService.update(updateReq);
        assertThat(updateResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // 5. Find By Active
        ApiResponsePagination<List<RoleResponseDeleteAt>> activeResp = queryService.findByActive(findReq);
        assertThat(activeResp.getStatus()).isEqualTo("success");

        // 6. Trash
        ApiResponse<RoleResponseDeleteAt> trashResp = commandService.trash(id);
        assertThat(trashResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // 7. Find By Trashed
        ApiResponsePagination<List<RoleResponseDeleteAt>> trashedResp = queryService.findByTrashed(findReq);
        assertThat(trashedResp.getStatus()).isEqualTo("success");

        // 8. Restore
        ApiResponse<RoleResponseDeleteAt> restoreResp = commandService.restore(id);
        assertThat(restoreResp.getStatus()).isEqualTo("success");

        // Trash again before delete permanent
        ApiResponse<RoleResponseDeleteAt> trashAgainResp = commandService.trash(id);
        assertThat(trashAgainResp.getStatus()).isEqualTo("success");

        // 9. Delete Permanent
        ApiResponse<Boolean> delPermResp = commandService.delete(id);
        assertThat(delPermResp.getStatus()).isEqualTo("success");

        // 10. Restore All
        ApiResponse<Boolean> restoreAllResp = commandService.restoreAll();
        assertThat(restoreAllResp.getStatus()).isEqualTo("success");

        // 11. Delete All Permanent
        ApiResponse<Boolean> delAllResp = commandService.deleteAll();
        assertThat(delAllResp.getStatus()).isEqualTo("success");
    }
}
