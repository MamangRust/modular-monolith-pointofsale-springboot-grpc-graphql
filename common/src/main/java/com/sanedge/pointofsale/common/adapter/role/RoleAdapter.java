package com.sanedge.pointofsale.common.adapter.role;

import com.sanedge.pointofsale.common.adapter.model.Role;
import com.sanedge.pointofsale.common.adapter.support.ProtoTime;
import com.sanedge.pointofsale.common.exception.ResourceNotFoundException;
import com.sanedge.pointofsale.pb.role.FindByNameRoleRequest;
import com.sanedge.pointofsale.pb.role.RoleQueryServiceGrpc.RoleQueryServiceBlockingStub;
import com.sanedge.pointofsale.pb.role.RoleResponse;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RoleAdapter implements RoleQueryPort {

    private final RoleQueryServiceBlockingStub query;

    public RoleAdapter(RoleQueryServiceBlockingStub query) {
        this.query = query;
    }

    @Override
    public Role findByName(String name) {
        try {
            var resp = query.findByName(FindByNameRoleRequest.newBuilder().setName(name).build());
            if (resp == null || !resp.hasData()) {
                throw new ResourceNotFoundException("Role not found: " + name);
            }
            return toRole(resp.getData());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new ResourceNotFoundException("Role not found: " + name);
            }
            throw e;
        }
    }

    private static Role toRole(RoleResponse r) {
        if (r == null) {
            return null;
        }
        return new Role(r.getRoleId(), r.getName(), ProtoTime.parse(r.getCreatedAt()), ProtoTime.parse(r.getUpdatedAt()));
    }
}
