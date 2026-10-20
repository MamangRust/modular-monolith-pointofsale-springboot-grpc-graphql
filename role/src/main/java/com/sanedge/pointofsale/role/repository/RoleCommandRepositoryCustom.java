package com.sanedge.pointofsale.role.repository;

import com.sanedge.pointofsale.role.models.Role;

public interface RoleCommandRepositoryCustom {
    Role trashed(Long roleId);

    Role restore(Long roleId);

    Role deletePermanent(Long roleId);

    boolean restoreAllDeleted();

    boolean deleteAllDeleted();
}
