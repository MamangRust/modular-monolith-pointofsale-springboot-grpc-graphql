package com.sanedge.pointofsale.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sanedge.pointofsale.role.models.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

}
