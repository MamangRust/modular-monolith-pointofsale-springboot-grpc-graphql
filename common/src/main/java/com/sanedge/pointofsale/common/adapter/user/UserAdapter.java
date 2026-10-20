package com.sanedge.pointofsale.common.adapter.user;

import com.sanedge.pointofsale.common.adapter.model.CreateUserCmd;
import com.sanedge.pointofsale.common.adapter.model.UpdateUserCmd;
import com.sanedge.pointofsale.common.adapter.model.User;
import com.sanedge.pointofsale.common.adapter.model.VerifyPasswordResult;
import com.sanedge.pointofsale.common.adapter.support.AdapterException;
import com.sanedge.pointofsale.common.adapter.support.ProtoTime;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.user.CreateUserRequest;
import com.sanedge.pointofsale.pb.user.FindAllUserRequest;
import com.sanedge.pointofsale.pb.user.FindByIdUserRequest;
import com.sanedge.pointofsale.pb.user.FindByUsernameUserRequest;
import com.sanedge.pointofsale.pb.user.UpdateUserRequest;
import com.sanedge.pointofsale.pb.user.UserCommandServiceGrpc.UserCommandServiceBlockingStub;
import com.sanedge.pointofsale.pb.user.UserQueryServiceGrpc.UserQueryServiceBlockingStub;
import com.sanedge.pointofsale.pb.user.UserResponse;
import com.sanedge.pointofsale.pb.user.VerifyPasswordRequest;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class UserAdapter implements UserQueryPort, UserCommandPort {

    private final UserQueryServiceBlockingStub query;

    private final UserCommandServiceBlockingStub command;

    public UserAdapter(UserQueryServiceBlockingStub query, UserCommandServiceBlockingStub command) {
        this.query = query;
        this.command = command;
    }

    @Override
    public User findById(int id) {
        try {
            var resp = query.findById(FindByIdUserRequest.newBuilder().setId(id).build());
            if (resp == null || !resp.hasData()) {
                throw new ResourceNotFoundException("User not found: " + id);
            }
            return toUser(resp.getData());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new ResourceNotFoundException("User not found: " + id);
            }
            throw e;
        }
    }

    @Override
    public User findByEmail(String email) {
        var resp = query.findAll(FindAllUserRequest.newBuilder().setSearch(email).setPage(1).setPageSize(1).build());
        if (resp == null) {
            return null;
        }
        for (UserResponse u : resp.getDataList()) {
            if (u != null && email.equalsIgnoreCase(u.getEmail())) {
                return toUser(u);
            }
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        try {
            var resp = query.findByUsername(FindByUsernameUserRequest.newBuilder().setUsername(username).build());
            if (resp == null || !resp.hasData()) {
                throw new ResourceNotFoundException("User not found: " + username);
            }
            return toUser(resp.getData());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new ResourceNotFoundException("User not found: " + username);
            }
            throw e;
        }
    }

    @Override
    public VerifyPasswordResult verifyPassword(String email, String password) {
        var resp = command.verifyPassword(VerifyPasswordRequest.newBuilder()
                .setUsername(email)
                .setPassword(password)
                .build());
        if (resp == null) {
            throw new AdapterException("verifyPassword returned no response");
        }
        return new VerifyPasswordResult(resp.getValid(), resp.getUserId(), resp.getUsername(), resp.getMessage());
    }

    @Override
    public User create(CreateUserCmd cmd) {
        var resp = command.create(CreateUserRequest.newBuilder()
                .setUsername(cmd.username())
                .setFirstname(cmd.firstname())
                .setLastname(cmd.lastname())
                .setEmail(cmd.email())
                .setPassword(cmd.password())
                .setConfirmPassword(cmd.confirmPassword())
                .build());
        if (resp == null || !resp.hasData()
                || !"success".equalsIgnoreCase(resp.getStatus())) {
            throw new AdapterException("Failed to create user: "
                    + (resp == null ? "no response" : resp.getMessage()));
        }
        return toUser(resp.getData());
    }

    @Override
    public User update(UpdateUserCmd cmd) {
        var resp = command.update(UpdateUserRequest.newBuilder()
                .setId(cmd.id())
                .setUsername(cmd.username())
                .setFirstname(cmd.firstname())
                .setLastname(cmd.lastname())
                .setEmail(cmd.email())
                .setPassword(cmd.password())
                .setConfirmPassword(cmd.confirmPassword())
                .build());
        if (resp == null || !resp.hasData()
                || !"success".equalsIgnoreCase(resp.getStatus())) {
            throw new AdapterException("Failed to update user: "
                    + (resp == null ? "no response" : resp.getMessage()));
        }
        return toUser(resp.getData());
    }

    private static User toUser(UserResponse u) {
        if (u == null) {
            return null;
        }
        return new User(u.getId(), u.getUsername(), u.getFirstname(), u.getLastname(), u.getEmail(),
                ProtoTime.parse(u.getCreatedAt()), ProtoTime.parse(u.getUpdatedAt()));
    }
}
