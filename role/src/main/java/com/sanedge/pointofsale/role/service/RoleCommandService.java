package com.sanedge.pointofsale.role.service;

import com.sanedge.pointofsale.role.domain.requests.CreateRoleRequest;
import com.sanedge.pointofsale.role.domain.requests.UpdateRoleRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.role.domain.responses.RoleResponse;
import com.sanedge.pointofsale.role.domain.responses.RoleResponseDeleteAt;

public interface RoleCommandService {
    ApiResponse<RoleResponse> create(CreateRoleRequest request);

    ApiResponse<RoleResponse> update(UpdateRoleRequest request);

    ApiResponse<RoleResponseDeleteAt> trash(Integer id);

    ApiResponse<RoleResponseDeleteAt> restore(Integer id);

    ApiResponse<Boolean> delete(Integer id);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();
}
