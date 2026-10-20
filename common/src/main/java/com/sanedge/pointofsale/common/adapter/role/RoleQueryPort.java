package com.sanedge.pointofsale.common.adapter.role;

import com.sanedge.pointofsale.common.adapter.model.Role;

/**
 * Port for the role query service, replacing direct
 * {@code @GrpcClient("role")} usage in consumer services.
 */
public interface RoleQueryPort {

    Role findByName(String name);
}
