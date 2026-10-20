package com.sanedge.pointofsale.user.service;

import com.sanedge.pointofsale.user.domain.requests.CreateUserRequest;
import com.sanedge.pointofsale.user.domain.requests.UpdateUserRequest;
import com.sanedge.pointofsale.common.domain.responses.api.ApiResponse;
import com.sanedge.pointofsale.user.domain.responses.UserResponse;
import com.sanedge.pointofsale.user.domain.responses.UserResponseDeleteAt;
import com.sanedge.pointofsale.user.domain.responses.VerifyPasswordResponse;

public interface UserCommandService {

    ApiResponse<UserResponse> create(CreateUserRequest req);

    ApiResponse<UserResponse> update(UpdateUserRequest req);

    ApiResponse<UserResponseDeleteAt> trashed(Integer userId);

    ApiResponse<UserResponseDeleteAt> restore(Integer userId);

    ApiResponse<Boolean> deletePermanent(Integer userId);

    ApiResponse<Boolean> restoreAll();

    ApiResponse<Boolean> deleteAll();

    VerifyPasswordResponse verifyPassword(String username, String password);
}