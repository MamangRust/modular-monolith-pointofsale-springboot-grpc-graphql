package com.sanedge.pointofsale.user.service;

import java.util.List;

import com.sanedge.pointofsale.user.domain.requests.FindAllUserRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponsePagination;
import com.sanedge.pointofsale.user.domain.responses.UserResponse;
import com.sanedge.pointofsale.user.domain.responses.UserResponseDeleteAt;

public interface UserQueryService {

    ApiResponsePagination<List<UserResponse>> findAll(FindAllUserRequest req);

    ApiResponse<UserResponse> findById(Integer userId);

    ApiResponse<UserResponse> findByEmail(String email);

    ApiResponse<UserResponse> findByUsername(String username);

    ApiResponsePagination<List<UserResponseDeleteAt>> findByActive(FindAllUserRequest req);

    ApiResponsePagination<List<UserResponseDeleteAt>> findByTrashed(FindAllUserRequest req);
}
