package com.sanedge.pointofsale.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.sanedge.pointofsale.BaseIntegrationTest;
import com.sanedge.pointofsale.category.domain.requests.CreateCategoryRequest;
import com.sanedge.pointofsale.category.domain.requests.FindAllCategory;
import com.sanedge.pointofsale.category.domain.requests.UpdateCategoryRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.category.domain.responses.CategoryResponse;
import com.sanedge.pointofsale.category.domain.responses.CategoryResponseDeleteAt;
import com.sanedge.pointofsale.category.service.CategoryCommandService;
import com.sanedge.pointofsale.category.service.CategoryQueryService;

import jakarta.validation.Validator;

public class CategoryServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CategoryCommandService commandService;

    @Autowired
    private CategoryQueryService queryService;

    @MockBean
    private Validator validator;

    @Test
    void testAllCategoryServiceMethods() {
        // 1. Create
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setName("Gadgets");
        req.setDescription("All kinds of gadgets");
        req.setSlugCategory("gadgets");

        ApiResponse<CategoryResponse> createResp = commandService.createCategory(req);
        assertThat(createResp.getStatus()).isEqualTo("success");
        Long id = createResp.getData().getId();

        entityManager.flush();
        entityManager.clear();

        // 2. Find All
        FindAllCategory findReq = new FindAllCategory();
        findReq.setSearch("Gadgets");
        findReq.setPage(1);
        findReq.setPageSize(10);

        ApiResponsePagination<List<CategoryResponse>> listResp = queryService.findAll(findReq);
        assertThat(listResp.getStatus()).isEqualTo("success");

        // 3. Find By ID
        ApiResponse<CategoryResponse> idResp = queryService.findById(id.intValue());
        assertThat(idResp.getStatus()).isEqualTo("success");

        // 4. Update
        UpdateCategoryRequest updateReq = new UpdateCategoryRequest();
        updateReq.setCategoryId(id.intValue());
        updateReq.setName("UpdatedGadgets");
        updateReq.setDescription("Updated gadgets description");
        updateReq.setSlugCategory("updated-gadgets");

        ApiResponse<CategoryResponse> updateResp = commandService.updateCategory(updateReq);
        assertThat(updateResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // 5. Find By Active
        ApiResponsePagination<List<CategoryResponseDeleteAt>> activeResp = queryService.findByActive(findReq);
        assertThat(activeResp.getStatus()).isEqualTo("success");

        // 6. Trash
        ApiResponse<CategoryResponseDeleteAt> trashResp = commandService.trashedCategory(id.intValue());
        assertThat(trashResp.getStatus()).isEqualTo("success");

        entityManager.flush();
        entityManager.clear();

        // 7. Find By Trashed
        ApiResponsePagination<List<CategoryResponseDeleteAt>> trashedResp = queryService.findByTrashed(findReq);
        assertThat(trashedResp.getStatus()).isEqualTo("success");

        // 8. Restore
        ApiResponse<CategoryResponseDeleteAt> restoreResp = commandService.restoreCategory(id.intValue());
        assertThat(restoreResp.getStatus()).isEqualTo("success");

        // Trash again before permanent delete
        ApiResponse<CategoryResponseDeleteAt> trashAgainResp = commandService.trashedCategory(id.intValue());
        assertThat(trashAgainResp.getStatus()).isEqualTo("success");

        // 9. Delete Permanent
        ApiResponse<Boolean> delPermResp = commandService.deleteCategoryPermanent(id.intValue());
        assertThat(delPermResp.getStatus()).isEqualTo("success");

        // 10. Restore All
        ApiResponse<Boolean> restoreAllResp = commandService.restoreAllCategories();
        assertThat(restoreAllResp.getStatus()).isEqualTo("success");

        // 11. Delete All Permanent
        ApiResponse<Boolean> delAllResp = commandService.deleteAllCategoriesPermanent();
        assertThat(delAllResp.getStatus()).isEqualTo("success");
    }
}
